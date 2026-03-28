package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingApprovalResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
}