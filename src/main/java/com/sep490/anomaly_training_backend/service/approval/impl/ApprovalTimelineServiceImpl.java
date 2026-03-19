package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse;
import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse.TimelineStep;
import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.StepState;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalTimelineService;
import com.sep490.anomaly_training_backend.service.approval.helper.ApprovableEntityLoader;
import com.sep490.anomaly_training_backend.service.approval.helper.ExpectedApproverResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApprovalTimelineServiceImpl implements ApprovalTimelineService {

    private final ApprovalFlowStepRepository flowStepRepository;
    private final ApprovalActionRepository actionLogRepository;
    private final ExpectedApproverResolver approverResolver;
    private final ApprovableEntityLoader entityLoader;

    @Override
    public ApprovalTimelineResponse getTimeline(ApprovalEntityType entityType, Long entityId) {

        // 1. Flow config
        List<ApprovalFlowStep> flowSteps = flowStepRepository
                .findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType);

        // 2. Logs của version hiện tại
        int latestVersion = actionLogRepository
                .findMaxVersionByEntityTypeAndEntityId(entityType, entityId)
                .orElse(1);

        List<ApprovalActionLog> logs = actionLogRepository
                .findByEntityTypeAndEntityIdAndEntityVersionAndDeleteFlagFalseOrderByStepOrderAsc(
                        entityType, entityId, latestVersion);

        Map<Integer, ApprovalActionLog> logByStep = logs.stream()
                .collect(Collectors.toMap(
                        ApprovalActionLog::getStepOrder,
                        l -> l,
                        (a, b) -> a.getPerformedAt().isAfter(b.getPerformedAt()) ? a : b));

        // 3. Load groupId một lần cho toàn bộ steps chưa có log
        Long groupId = entityLoader.loadGroupId(entityType, entityId).orElse(null);

        boolean rejected = logs.stream().anyMatch(l -> l.getAction() == ApprovalAction.REJECT);
        boolean foundWaiting = false;

        // 4. Build steps
        List<TimelineStep> steps = new ArrayList<>();

        ApprovalActionLog submitLog = logByStep.get(0);
        steps.add(buildSubmitStep(submitLog));

        for (ApprovalFlowStep flowStep : flowSteps) {
            ApprovalActionLog log = logByStep.get(flowStep.getStepOrder());
            StepState state;

            if (log != null) {
                state = (log.getAction() == ApprovalAction.REJECT) ? StepState.REJECTED : StepState.DONE;
            } else if (!foundWaiting && !rejected && submitLog != null) {
                state = StepState.WAITING;
                foundWaiting = true;
            } else {
                state = StepState.PENDING;
            }

            // Resolve expected approver khi bước chưa có action thực tế
            User expectedApprover = null;
            if (log == null && groupId != null) {
                expectedApprover = approverResolver
                        .resolve(groupId, flowStep.getApproverRole())
                        .orElse(null);
            }

            steps.add(buildFlowStep(flowStep, log, state, expectedApprover));
        }

        return ApprovalTimelineResponse.builder()
                .entityType(entityType.name())
                .entityId(entityId)
                .currentStatus(resolveCurrentStatus(logs, submitLog))
                .steps(steps)
                .build();
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private TimelineStep buildSubmitStep(ApprovalActionLog log) {
        if (log == null) {
            return TimelineStep.builder()
                    .stepOrder(0)
                    .stepLabel("NGƯỜI TẠO")
                    .state(StepState.PENDING)
                    .build();
        }
        return TimelineStep.builder()
                .stepOrder(0)
                .stepLabel("NGƯỜI TẠO")
                .state(log.getAction() == ApprovalAction.REVISE ? StepState.REJECTED : StepState.DONE)
                .performerName(log.getPerformedByFullName())
                .performerCode(log.getPerformedByUsername())
                .performedAt(log.getPerformedAt())
                .action(log.getAction().name())
                .comment(log.getComment())
                .build();
    }

    /**
     * @param log              null nếu bước chưa được thực hiện
     * @param expectedApprover User dự kiến tra từ org (chỉ dùng name/code, không có performedAt)
     */
    private TimelineStep buildFlowStep(ApprovalFlowStep flowStep,
                                       ApprovalActionLog log,
                                       StepState state,
                                       User expectedApprover) {

        String label = resolveStepLabel(flowStep.getApproverRole().name());

        if (log != null) {
            // Đã có action thực → dùng snapshot trong log (không ghi đè bằng org)
            return TimelineStep.builder()
                    .stepOrder(flowStep.getStepOrder())
                    .stepLabel(label)
                    .state(state)
                    .performerName(log.getPerformedByFullName())
                    .performerCode(log.getPerformedByUsername())
                    .performedAt(log.getPerformedAt())
                    .action(log.getAction().name())
                    .comment(log.getComment())
                    .build();
        }

        // Chưa có action → hiện người dự kiến nếu có, performedAt để null
        return TimelineStep.builder()
                .stepOrder(flowStep.getStepOrder())
                .stepLabel(label)
                .state(state)
                .performerName(expectedApprover != null ? expectedApprover.getFullName() : null)
                .performerCode(expectedApprover != null ? expectedApprover.getUsername() : null)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String resolveStepLabel(String roleCode) {
        return switch (roleCode) {
            case "ROLE_SUPERVISOR" -> "NGƯỜI KIỂM TRA";
            case "ROLE_MANAGER" -> "NGƯỜI PHÊ DUYỆT";
            default -> roleCode;
        };
    }

    private String resolveCurrentStatus(List<ApprovalActionLog> logs, ApprovalActionLog submitLog) {
        if (submitLog == null) return "DRAFT";
        Optional<ApprovalActionLog> latestFlowLog = logs.stream()
                .filter(l -> l.getStepOrder() > 0)
                .max(Comparator.comparingInt(ApprovalActionLog::getStepOrder));
        if (latestFlowLog.isEmpty()) return "WAITING_SV";
        ApprovalActionLog last = latestFlowLog.get();
        return switch (last.getAction()) {
            case APPROVE -> "APPROVED";
            case REJECT -> "REJECTED_BY_" + last.getPerformedByRole().name().replace("ROLE_", "");
            default -> "WAITING";
        };
    }
}