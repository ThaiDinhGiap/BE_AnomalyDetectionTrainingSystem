package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse;
import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse.TimelineStep;
import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse.TimelineStep.StepState;
import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalTimelineService;
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

    @Override
    public ApprovalTimelineResponse getTimeline(ApprovalEntityType entityType, Long entityId) {

        // 1. Lấy cấu hình flow (bước SV=1, MG=2) theo thứ tự
        List<ApprovalFlowStep> flowSteps = flowStepRepository
                .findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType);

        // 2. Lấy toàn bộ action log của entity này, phiên bản mới nhất
        //    (nếu entity bị revise nhiều lần, chỉ lấy version hiện tại)
        int latestVersion = actionLogRepository
                .findMaxVersionByEntityTypeAndEntityId(entityType, entityId)
                .orElse(1);

        List<ApprovalActionLog> logs = actionLogRepository
                .findByEntityTypeAndEntityIdAndEntityVersionAndDeleteFlagFalseOrderByStepOrderAsc(
                        entityType, entityId, latestVersion);

        // Map: stepOrder → log (để lookup O(1))
        Map<Integer, ApprovalActionLog> logByStep = logs.stream()
                .collect(Collectors.toMap(ApprovalActionLog::getStepOrder, l -> l,
                        // nếu có duplicate step (hiếm), lấy cái mới nhất
                        (a, b) -> a.getPerformedAt().isAfter(b.getPerformedAt()) ? a : b));

        // 3. Xác định bước đang WAITING (chấm cam)
        //    = bước nhỏ nhất trong flow chưa có log hoặc log là REJECT
        boolean foundWaiting = false;
        boolean rejected = logs.stream()
                .anyMatch(l -> l.getAction() == ApprovalAction.REJECT);

        // 4. Xây dựng danh sách step cho FE
        List<TimelineStep> steps = new ArrayList<>();

        // Bước 0: SUBMIT (người tạo) – luôn có
        ApprovalActionLog submitLog = logByStep.get(0);
        steps.add(buildSubmitStep(submitLog));

        // Bước 1..N: từ ApprovalFlowStep
        for (ApprovalFlowStep flowStep : flowSteps) {
            ApprovalActionLog log = logByStep.get(flowStep.getStepOrder());
            StepState state;

            if (log != null) {
                state = (log.getAction() == ApprovalAction.REJECT) ? StepState.REJECTED : StepState.DONE;
            } else if (!foundWaiting && !rejected && submitLog != null) {
                // Bước đầu tiên chưa có log và flow chưa bị reject → đang chờ
                state = StepState.WAITING;
                foundWaiting = true;
            } else {
                state = StepState.PENDING;
            }

            steps.add(buildFlowStep(flowStep, log, state));
        }

        // 5. Lấy currentStatus từ log gần nhất
        String currentStatus = resolveCurrentStatus(logs, submitLog);

        return ApprovalTimelineResponse.builder()
                .entityType(entityType.name())
                .entityId(entityId)
                .currentStatus(currentStatus)
                .steps(steps)
                .build();
    }

    // ── Builders ──────────────────────────────────────────────────────────────

    private TimelineStep buildSubmitStep(ApprovalActionLog log) {
        if (log == null) {
            // Entity đang ở DRAFT, chưa submit lần nào
            return TimelineStep.builder()
                    .stepOrder(0)
                    .stepLabel("NGƯỜI TẠO")
                    .state(StepState.PENDING)
                    .build();
        }

        StepState state = (log.getAction() == ApprovalAction.REVISE)
                ? StepState.REJECTED   // TL tự revise lại
                : StepState.DONE;

        return TimelineStep.builder()
                .stepOrder(0)
                .stepLabel("NGƯỜI TẠO")
                .state(state)
                .performerName(log.getPerformedByFullName())
                .performerCode(log.getPerformedByUsername())
                .performedAt(log.getPerformedAt())
                .action(log.getAction().name())
                .comment(log.getComment())
                .build();
    }

    private TimelineStep buildFlowStep(ApprovalFlowStep flowStep,
                                       ApprovalActionLog log,
                                       StepState state) {
        String label = resolveStepLabel(flowStep.getStepOrder(), flowStep.getApproverRole().name());

        if (log == null) {
            return TimelineStep.builder()
                    .stepOrder(flowStep.getStepOrder())
                    .stepLabel(label)
                    .state(state)
                    .build();
        }

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

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Nhãn hiển thị trên FE dựa vào step_order và role.
     * Có thể mở rộng thêm label tuỳ business rule.
     */
    private String resolveStepLabel(int stepOrder, String roleCode) {
        return switch (roleCode) {
            case "ROLE_SUPERVISOR" -> "NGƯỜI KIỂM TRA";
            case "ROLE_MANAGER" -> "NGƯỜI PHÊ DUYỆT";
            default -> "BƯỚC " + stepOrder;
        };
    }

    /**
     * Xác định currentStatus dạng chuỗi để FE hiển thị badge tổng quát.
     */
    private String resolveCurrentStatus(List<ApprovalActionLog> logs, ApprovalActionLog submitLog) {
        if (submitLog == null) return "DRAFT";

        // Lấy log có stepOrder cao nhất (gần nhất trong flow)
        Optional<ApprovalActionLog> latestFlowLog = logs.stream()
                .filter(l -> l.getStepOrder() > 0)
                .max(Comparator.comparingInt(ApprovalActionLog::getStepOrder));

        if (latestFlowLog.isEmpty()) return "WAITING_SV";

        ApprovalActionLog last = latestFlowLog.get();
        return switch (last.getAction()) {
            case APPROVE -> "APPROVED";
            case REJECT -> "REJECTED_BY_" + last.getPerformedByRole().name()
                    .replace("ROLE_", "");
            default -> "WAITING";
        };
    }
}