package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse;
import com.sep490.anomaly_training_backend.dto.approval.ApprovalTimelineResponse.TimelineStep;
import com.sep490.anomaly_training_backend.dto.response.PendingApprovalResponse;
import com.sep490.anomaly_training_backend.dto.response.RejectReasonGroupResponse;
import com.sep490.anomaly_training_backend.dto.response.RejectReasonResponse;
import com.sep490.anomaly_training_backend.dto.response.RequiredActionResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.StepState;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.DefectProposal;
import com.sep490.anomaly_training_backend.model.RejectReason;
import com.sep490.anomaly_training_backend.model.TrainingPlan;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposal;
import com.sep490.anomaly_training_backend.model.TrainingSampleReview;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleReviewRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalQueryService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalQueryServiceImpl implements ApprovalQueryService {

    private final DefectProposalRepository defectProposalRepository;
    private final TrainingSampleProposalRepository trainingSampleProposalRepository;
    private final TrainingPlanRepository planRepo;
    private final ApprovalActionRepository actionRepo;
    private final ApprovalFlowStepRepository flowStepRepo;
    private final RejectReasonRepository rejectReasonRepo;
    private final RequiredActionRepository requiredActionRepo;
    private final TrainingSampleReviewRepository trainingSampleReviewRepository;
    private final ApprovalRouteService approvalRouteService;

    @Override
    public List<ApprovalHistoryResponse> getApprovalHistory(ApprovalEntityType entityType, Long entityId) {
        return actionRepo.findByEntityTypeAndEntityIdOrderByPerformedAtAsc(entityType, entityId)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    public List<ApprovalHistoryResponse> getApprovalHistoryByVersion(ApprovalEntityType entityType, Long entityId, Integer version) {
        return actionRepo.findByEntityTypeAndEntityIdAndEntityVersionOrderByPerformedAtAsc(entityType, entityId, version)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    public List<PendingApprovalResponse> getPendingApprovals(User currentUser, ApprovalEntityType entityType) {
        List<PendingApprovalResponse> result = new ArrayList<>();

        // Tìm tất cả pending status mà user có permission tương ứng
        List<ReportStatus> targetStatuses = getTargetStatusesForUser(currentUser);

        if (targetStatuses.isEmpty()) {
            return result;
        }

        for (ReportStatus targetStatus : targetStatuses) {
            if (entityType == null || entityType == ApprovalEntityType.DEFECT_PROPOSAL) {
                result.addAll(getPendingDefectProposals(currentUser, targetStatus));
            }

            if (entityType == null || entityType == ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL) {
                result.addAll(getPendingSampleProposals(currentUser, targetStatus));
            }

            if (entityType == null || entityType == ApprovalEntityType.TRAINING_PLAN) {
                result.addAll(getPendingPlans(currentUser, targetStatus));
            }
        }

        return result;
    }

    @Override
    public Long countPendingApprovals(User currentUser) {
        return (long) getPendingApprovals(currentUser, null).size();
    }

    @Override
    public List<ApprovalHistoryResponse> getActionsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return actionRepo.findByPerformedByUserIdOrderByPerformedAtDesc(userId, pageable)
                .stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    // ==================== PRIVATE HELPERS ====================

    /**
     * Tìm tất cả pending status mà user hiện tại có permission để xử lý.
     * Dựa vào approval_flow_steps, kiểm tra user có permission nào, trả về danh sách pendingStatus tương ứng.
     */
    private List<ReportStatus> getTargetStatusesForUser(User user) {
        // Lấy tất cả flow steps active, lọc theo permission user có
        List<ApprovalFlowStep> allSteps = flowStepRepo.findAll();
        return allSteps.stream()
                .filter(ApprovalFlowStep::getIsActive)
                .filter(step -> user.hasPermission(step.getRequiredPermission()))
                .map(ApprovalFlowStep::getPendingStatus)
                .distinct()
                .toList();
    }

    private List<PendingApprovalResponse> getPendingDefectProposals(User currentUser, ReportStatus status) {
        return defectProposalRepository.findPendingForApprove(status, currentUser.getId())
                .stream()
                .map(report -> PendingApprovalResponse.builder()
                        .entityType(ApprovalEntityType.DEFECT_PROPOSAL)
                        .entityId(report.getId())
                        .entityName("Defect Report #" + report.getId())
                        .status(report.getStatus())
                        .currentVersion(report.getCurrentVersion())
                        .submittedByUsername(report.getCreatedBy())
                        .submittedAt(Instant.from(report.getCreatedAt()))
                        .lineId(report.getProductLine().getId())
                        .lineName(report.getProductLine().getName())
                        .detailCount(report.getDetails().size())
                        .build())
                .toList();
    }

    private List<PendingApprovalResponse> getPendingSampleProposals(User currentUser, ReportStatus status) {
        return trainingSampleProposalRepository.findPendingForApprove(status, currentUser.getId())
                .stream()
                .map(proposal -> PendingApprovalResponse.builder()
                        .entityType(ApprovalEntityType.TRAINING_SAMPLE_PROPOSAL)
                        .entityId(proposal.getId())
                        .entityName("Training Sample Proposal #" + proposal.getId())
                        .status(proposal.getStatus())
                        .currentVersion(proposal.getCurrentVersion())
                        .submittedByUsername(proposal.getCreatedBy())
                        .submittedAt(Instant.from(proposal.getCreatedAt()))
                        .lineId(proposal.getProductLine().getId())
                        .lineName(proposal.getProductLine().getName())
                        .detailCount(proposal.getDetails().size())
                        .build())
                .toList();
    }

    private List<PendingApprovalResponse> getPendingPlans(User currentUser, ReportStatus status) {
        return planRepo.findPendingForApprove(status, currentUser.getId())
                .stream()
                .map(plan -> PendingApprovalResponse.builder()
                        .entityType(ApprovalEntityType.TRAINING_PLAN)
                        .entityId(plan.getId())
                        .entityName(plan.getFormCode())
                        .status(plan.getStatus())
                        .currentVersion(plan.getCurrentVersion())
                        .submittedByUsername(plan.getCreatedBy())
                        .submittedAt(Instant.from(plan.getCreatedAt()))
                        .lineId(plan.getLine().getId())
                        .lineName(plan.getLine().getName())
                        .detailCount(plan.getDetails().size())
                        .build())
                .toList();
    }

    private ApprovalHistoryResponse toHistoryResponse(ApprovalActionLog log) {
        Set<ApprovalHistoryResponse.RejectReasonResponse> rejectReasons =
                log.getRejectReasons() == null ? Set.of() :
                        log.getRejectReasons().stream()
                                .map(r -> ApprovalHistoryResponse.RejectReasonResponse.builder()
                                        .id(r.getId())
                                        .categoryName(r.getCategoryName())
                                        .reasonName(r.getReasonName())
                                        .build())
                                .collect(Collectors.toSet());

        Set<ApprovalHistoryResponse.RequiredActionResponse> requiredActions =
                log.getRequiredActions() == null ? Set.of() :
                        log.getRequiredActions().stream()
                                .map(a -> ApprovalHistoryResponse.RequiredActionResponse.builder()
                                        .id(a.getId())
                                        .actionName(a.getActionName())
                                        .build())
                                .collect(Collectors.toSet());

        return ApprovalHistoryResponse.builder()
                .id(log.getId())
                .entityVersion(log.getEntityVersion())
                .stepOrder(log.getStepOrder())
                .stepName(getStepName(log.getStepOrder()))
                .requiredPermission(log.getRequiredPermission())
                .action(log.getAction())
                .performedByUsername(log.getPerformedByUsername())
                .performedByFullName(log.getPerformedByFullName())
                .performedByRole(log.getPerformedByRole())
                .comment(log.getComment())
                .rejectReasons(rejectReasons)
                .requiredActions(requiredActions)
                .performedAt(log.getPerformedAt())
                .ipAddress(log.getIpAddress())
                .build();
    }

    private String getStepName(int stepOrder) {
        return switch (stepOrder) {
            case -1 -> "Revise";
            case 0 -> "Submit";
            default -> "Step " + stepOrder;
        };
    }

    // ==================== TIMELINE (merged from ApprovalTimelineService) ====================

    @Override
    public ApprovalTimelineResponse getTimeline(ApprovalEntityType entityType, Long entityId) {

        // 1. Flow config
        List<ApprovalFlowStep> flowSteps = flowStepRepo
                .findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType);

        // 2. Logs của version hiện tại
        int latestVersion = actionRepo
                .findMaxVersionByEntityTypeAndEntityId(entityType, entityId)
                .orElse(1);

        List<ApprovalActionLog> logs = actionRepo
                .findByEntityTypeAndEntityIdAndEntityVersionAndDeleteFlagFalseOrderByStepOrderAsc(
                        entityType, entityId, latestVersion);

        Map<Integer, ApprovalActionLog> logByStep = logs.stream()
                .collect(Collectors.toMap(
                        ApprovalActionLog::getStepOrder,
                        l -> l,
                        (a, b) -> a.getPerformedAt().isAfter(b.getPerformedAt()) ? a : b));

        // 3. Load groupId một lần cho toàn bộ steps chưa có log
        Long groupId = loadGroupId(entityType, entityId).orElse(null);

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

            User expectedApprover = null;
            if (log == null && groupId != null) {
                expectedApprover = approvalRouteService
                        .resolveExpectedApprover(groupId, flowStep.getRequiredPermission())
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

    private TimelineStep buildFlowStep(ApprovalFlowStep flowStep,
                                       ApprovalActionLog log,
                                       StepState state,
                                       User expectedApprover) {

        String label = flowStep.getStepLabel() != null ? flowStep.getStepLabel() : "Step " + flowStep.getStepOrder();

        if (log != null) {
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

        return TimelineStep.builder()
                .stepOrder(flowStep.getStepOrder())
                .stepLabel(label)
                .state(state)
                .performerName(expectedApprover != null ? expectedApprover.getFullName() : null)
                .performerCode(expectedApprover != null ? expectedApprover.getUsername() : null)
                .build();
    }

    private String resolveCurrentStatus(List<ApprovalActionLog> logs, ApprovalActionLog submitLog) {
        if (submitLog == null) return "DRAFT";
        Optional<ApprovalActionLog> latestFlowLog = logs.stream()
                .filter(l -> l.getStepOrder() > 0)
                .max(Comparator.comparingInt(ApprovalActionLog::getStepOrder));
        if (latestFlowLog.isEmpty()) return "PENDING_REVIEW";
        ApprovalActionLog last = latestFlowLog.get();
        return switch (last.getAction()) {
            case APPROVE -> "APPROVED";
            case REJECT -> "REJECTED";
            default -> "PENDING";
        };
    }

    private Optional<Long> loadGroupId(ApprovalEntityType entityType, Long entityId) {
        return switch (entityType) {
            case TRAINING_PLAN -> planRepo.findById(entityId).map(TrainingPlan::getGroupId);
            case DEFECT_PROPOSAL -> defectProposalRepository.findById(entityId).map(DefectProposal::getGroupId);
            case TRAINING_SAMPLE_PROPOSAL ->
                    trainingSampleProposalRepository.findById(entityId).map(TrainingSampleProposal::getGroupId);
            case TRAINING_RESULT -> Optional.empty();
            case TRAINING_SAMPLE_REVIEW -> trainingSampleReviewRepository.findById(entityId).map(TrainingSampleReview::getGroupId);
        };
    }

    // ==================== METADATA (merged from ApprovalMetadataService) ====================

    @Override
    public List<RejectReasonGroupResponse> getRejectReasonGroups() {
        List<RejectReason> allReasons = rejectReasonRepo.findAllByOrderByCategoryNameAscIdAsc();

        Map<String, List<RejectReasonResponse>> grouped = new LinkedHashMap<>();
        for (RejectReason r : allReasons) {
            grouped
                    .computeIfAbsent(r.getCategoryName(), k -> new ArrayList<>())
                    .add(RejectReasonResponse.builder()
                            .id(r.getId())
                            .reasonName(r.getReasonName())
                            .build());
        }

        return grouped.entrySet().stream()
                .map(e -> RejectReasonGroupResponse.builder()
                        .categoryName(e.getKey())
                        .reasons(e.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RequiredActionResponse> getRequiredActions() {
        return requiredActionRepo.findAll().stream()
                .map(a -> RequiredActionResponse.builder()
                        .id(a.getId())
                        .actionName(a.getActionName())
                        .build())
                .toList();
    }
}