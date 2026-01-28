package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.response.ApprovalHistoryResponse;
import com.sep490.anomaly_training_backend.dto.response.PendingApprovalResponse;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.DefectReportRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanRepository;
import com.sep490.anomaly_training_backend.repository.TrainingTopicReportRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalQueryServiceImpl implements ApprovalQueryService {

    private final DefectReportRepository defectReportRepo;
    private final TrainingTopicReportRepository topicReportRepo;
    private final TrainingPlanRepository planRepo;
    private final ApprovalActionRepository actionRepo;

    @Override
    public List<PendingApprovalResponse> getPendingApprovals(User currentUser, ApprovalEntityType entityType) {
        List<PendingApprovalResponse> result = new ArrayList<>();

        UserRole role = currentUser.getRole();
        ReportStatus targetStatus = getTargetStatusForRole(role);

        if (targetStatus == null) {
            return result;
        }

        if (entityType == null || entityType == ApprovalEntityType.DEFECT_REPORT) {
            result.addAll(getPendingDefectReports(currentUser, targetStatus));
        }

        if (entityType == null || entityType == ApprovalEntityType.TRAINING_TOPIC_REPORT) {
            result.addAll(getPendingTopicReports(currentUser, targetStatus));
        }

        if (entityType == null || entityType == ApprovalEntityType.TRAINING_PLAN) {
            result.addAll(getPendingPlans(currentUser, targetStatus));
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

    private ReportStatus getTargetStatusForRole(UserRole role) {
        return switch (role) {
            case SUPERVISOR -> ReportStatus.WAITING_SV;
            case MANAGER -> ReportStatus.WAITING_MANAGER;
            default -> null;
        };
    }

    private List<PendingApprovalResponse> getPendingDefectReports(User currentUser, ReportStatus status) {
        return defectReportRepo.findPendingForApprover(status, currentUser.getId(), currentUser.getRole())
                .stream()
                .map(report -> PendingApprovalResponse.builder()
                        .entityType(ApprovalEntityType.DEFECT_REPORT)
                        .entityId(report.getId())
                        .entityName("Defect Report #" + report.getId())
                        .status(report.getStatus())
                        .currentVersion(report.getCurrentVersion())
                        .submittedByUsername(report.getCreatedBy())
                        .submittedAt(Instant.from(report.getCreatedAt()))
                        .groupId(report.getGroup().getId())
                        .groupName(report.getGroup().getName())
                        .detailCount(report.getDetails().size())
                        .build())
                .toList();
    }

    private List<PendingApprovalResponse> getPendingTopicReports(User currentUser, ReportStatus status) {
        return topicReportRepo.findPendingForApprover(status, currentUser.getId(), currentUser.getRole())
                .stream()
                .map(report -> PendingApprovalResponse.builder()
                        .entityType(ApprovalEntityType.TRAINING_TOPIC_REPORT)
                        .entityId(report.getId())
                        .entityName("Training Topic Report #" + report.getId())
                        .status(report.getStatus())
                        .currentVersion(report.getCurrentVersion())
                        .submittedByUsername(report.getCreatedBy())
                        .submittedAt(Instant.from(report.getCreatedAt()))
                        .groupId(report.getGroup().getId())
                        .groupName(report.getGroup().getName())
                        .detailCount(report.getDetails().size())
                        .build())
                .toList();
    }

    private List<PendingApprovalResponse> getPendingPlans(User currentUser, ReportStatus status) {
        return planRepo.findPendingForApprover(status, currentUser.getId(), currentUser.getRole())
                .stream()
                .map(plan -> PendingApprovalResponse.builder()
                        .entityType(ApprovalEntityType.TRAINING_PLAN)
                        .entityId(plan.getId())
                        .entityName(plan.getFormCode())
                        .status(plan.getStatus())
                        .currentVersion(plan.getCurrentVersion())
                        .submittedByUsername(plan.getCreatedBy())
                        .submittedAt(Instant.from(plan.getCreatedAt()))
                        .groupId(plan.getGroup().getId())
                        .groupName(plan.getGroup().getName())
                        .detailCount(plan.getDetails().size())
                        .build())
                .toList();
    }

    private ApprovalHistoryResponse toHistoryResponse(ApprovalActionLog log) {
        return ApprovalHistoryResponse.builder()
                .id(log.getId())
                .entityVersion(log.getEntityVersion())
                .stepOrder(log.getStepOrder())
                .requiredRole(log.getRequiredRole())
                .action(log.getAction())
                .performedByUsername(log.getPerformedByUsername())
                .performedByFullName(log.getPerformedByFullName())
                .performedByRole(log.getPerformedByRole())
                .comment(log.getComment())
                .rejectReason(log.getRejectReason())
                .performedAt(log.getPerformedAt())
                .ipAddress(log.getIpAddress())
                .build();
    }
}