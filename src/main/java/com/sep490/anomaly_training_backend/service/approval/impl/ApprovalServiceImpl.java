package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.request.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.request.RejectRequest;
import com.sep490.anomaly_training_backend.enums.*;
import com.sep490.anomaly_training_backend.exception.BusinessException;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import com.sep490.anomaly_training_backend.service.approval.PinService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalFlowStepRepository flowStepRepo;
    private final ApprovalActionRepository actionRepo;
    private final PinService pinService;
    private final ApprovalRouteService routeService;

    // ==================== SUBMIT ====================

    @Override
    @Transactional
    public void submit(Approvable entity, User currentUser, HttpServletRequest request) {
        // 1. Validate status
        if (entity.getStatus() != ProposalStatus.DRAFT) {
            throw new BusinessException("Chỉ có thể submit khi ở trạng thái DRAFT");
        }

        // 2. Get first step
        ApprovalFlowStep firstStep = getFirstStep(entity.getEntityType());

        // 3. Update entity status
        ReportStatus pendingStatus = mapRoleToPendingStatus(firstStep.getApproverRole());
//        entity.setStatus(pendingStatus);

        // 4. Log action
        logAction(entity, ApprovalAction.SUBMIT, 0, UserRole.TEAM_LEADER,
                currentUser, null, null, request);

        log.info("Submitted {} id={} version={} by user={}",
                entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());
    }

    // ==================== REVISE ====================

    @Override
    @Transactional
    public void revise(Approvable entity, User currentUser, HttpServletRequest request) {
        // 1. Validate status
//        if (!isRejectedStatus(entity.getStatus())) {
//            throw new BusinessException("Chỉ có thể revise khi bị từ chối");
//        }

        // 2. Increment version
        entity.setCurrentVersion(entity.getCurrentVersion() + 1);

        // 3. Set status back to DRAFT
//        entity.setStatus(ReportStatus.DRAFT);

        // 4. Log action (với version mới)
        logAction(entity, ApprovalAction.REVISE, -1, UserRole.TEAM_LEADER,
                currentUser, null, null, request);

        log.info("Revised {} id={} newVersion={} by user={}",
                entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());
    }

    // ==================== APPROVE ====================

    @Override
    @Transactional
    public void approve(Approvable entity, User currentUser, ApproveRequest req, HttpServletRequest request) {
        // 1. Verify PIN
        pinService.verifyPin(currentUser, req.getPin());

        // 2. Get current step from status
        ApprovalFlowStep currentStep = getCurrentStep(entity);

        // 3. Validate approver
        validateApprover(entity, currentUser, currentStep);

        // 4. Log action
        logAction(entity, ApprovalAction.APPROVE, currentStep.getStepOrder(),
                currentStep.getApproverRole(), currentUser, req.getComment(), null, request);

        // 5. Determine next status
        ApprovalFlowStep nextStep = getNextStep(entity.getEntityType(), currentStep.getStepOrder());

        if (nextStep != null) {
            // Còn step tiếp theo
            ReportStatus nextPendingStatus = mapRoleToPendingStatus(nextStep.getApproverRole());
//            entity.setStatus(nextPendingStatus);

            log.info("Approved {} id={} version={} by {} -> next: {}",
                    entity.getEntityType(), entity.getId(), entity.getCurrentVersion(),
                    currentUser.getUsername(), nextPendingStatus);
        } else {
            // Đây là step cuối -> APPROVED
//            entity.setStatus(ReportStatus.APPROVED);

            // Apply report (tạo/update/delete master data)
            entity.applyApproval();

            log.info("Final approved {} id={} version={} by {}",
                    entity.getEntityType(), entity.getId(), entity.getCurrentVersion(),
                    currentUser.getUsername());
        }
    }

    // ==================== REJECT ====================

    @Override
    @Transactional
    public void reject(Approvable entity, User currentUser, RejectRequest req, HttpServletRequest request) {
        // 1. Validate reject reason
        if (req.getRejectReason() == null || req.getRejectReason().isBlank()) {
            throw new BusinessException("Vui lòng nhập lý do từ chối");
        }

        // 2. Verify PIN
        pinService.verifyPin(currentUser, req.getPin());

        // 3. Get current step
        ApprovalFlowStep currentStep = getCurrentStep(entity);

        // 4. Validate approver
        validateApprover(entity, currentUser, currentStep);

        // 5. Log action
        logAction(entity, ApprovalAction.REJECT, currentStep.getStepOrder(),
                currentStep.getApproverRole(), currentUser, req.getComment(), req.getRejectReason(), request);

        // 6. Set rejected status
        ReportStatus rejectedStatus = mapRoleToRejectedStatus(currentStep.getApproverRole());
//        entity.setStatus(rejectedStatus);

        log.info("Rejected {} id={} version={} by {} reason={}",
                entity.getEntityType(), entity.getId(), entity.getCurrentVersion(),
                currentUser.getUsername(), req.getRejectReason());
    }

    // ==================== QUERY ====================

    @Override
    public List<ApprovalActionLog> getApprovalHistory(ApprovalEntityType entityType, Long entityId) {
        return actionRepo.findByEntityTypeAndEntityIdOrderByPerformedAtAsc(entityType, entityId);
    }

    @Override
    public List<ApprovalActionLog> getApprovalHistoryByVersion(ApprovalEntityType entityType, Long entityId, Integer version) {
        return actionRepo.findByEntityTypeAndEntityIdAndEntityVersionOrderByPerformedAtAsc(entityType, entityId, version);
    }

    @Override
    public boolean canApprove(Approvable entity, User user) {
        try {
            ApprovalFlowStep currentStep = getCurrentStep(entity);
            validateApprover(entity, user, currentStep);
            return true;
        } catch (BusinessException e) {
            return false;
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private ApprovalFlowStep getFirstStep(ApprovalEntityType entityType) {
        return flowStepRepo.findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType)
                .stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Không tìm thấy workflow cho " + entityType));
    }

    private ApprovalFlowStep getCurrentStep(Approvable entity) {
        ProposalStatus status = entity.getStatus();

        if (!isWaitingStatus(status)) {
            throw new BusinessException("Report không ở trạng thái chờ duyệt: " + status);
        }

        UserRole requiredRole = mapStatusToRole(status);

        return flowStepRepo.findByEntityTypeAndApproverRoleAndIsActiveTrue(entity.getEntityType(), requiredRole)
                .orElseThrow(() -> new BusinessException("Không tìm thấy step phù hợp với status " + status));
    }

    private ApprovalFlowStep getNextStep(ApprovalEntityType entityType, int currentStepOrder) {
        List<ApprovalFlowStep> steps = flowStepRepo.findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType);
        return steps.stream()
                .filter(s -> s.getStepOrder() > currentStepOrder)
                .findFirst()
                .orElse(null);
    }

    private void validateApprover(Approvable entity, User currentUser, ApprovalFlowStep step) {
        // Check role
        if (currentUser.getRole() != step.getApproverRole()) {
            throw new BusinessException("Bạn không có quyền duyệt ở bước này. Yêu cầu role: " + step.getApproverRole());
        }

        // Check org hierarchy
        if (!routeService.isValidApprover(entity.getGroupId(), step.getApproverRole(), currentUser.getId())) {
            throw new BusinessException("Bạn không phải người phê duyệt được chỉ định cho báo cáo này");
        }
    }

    private void logAction(Approvable entity, ApprovalAction action, int stepOrder, UserRole requiredRole,
                           User performer, String comment, String rejectReason, HttpServletRequest request) {

        ApprovalActionLog logEntry = ApprovalActionLog.builder()
                .entityType(entity.getEntityType())
                .entityId(entity.getId())
                .entityVersion(entity.getCurrentVersion())
                .stepOrder(stepOrder)
                .requiredRole(requiredRole)
                .action(action)
                .performedByUser(performer)
                .performedByUsername(performer.getUsername())
                .performedByFullName(performer.getFullName())
                .performedByRole(performer.getRole())
                .comment(comment)
//                .rejectReason(rejectReason)
                .performedAt(Instant.now())
                .ipAddress(getClientIp(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .contentHash(entity.computeContentHash())
                .build();

        actionRepo.save(logEntry);
    }

    // ==================== STATUS MAPPING ====================

    private ReportStatus mapRoleToPendingStatus(UserRole role) {
        return switch (role) {
            case SUPERVISOR -> ReportStatus.WAITING_SV;
            case MANAGER -> ReportStatus.WAITING_MANAGER;
            default -> throw new BusinessException("Unsupported approver role: " + role);
        };
    }

    private ReportStatus mapRoleToRejectedStatus(UserRole role) {
        return switch (role) {
            case SUPERVISOR -> ReportStatus.REJECTED_BY_SV;
            case MANAGER -> ReportStatus.REJECTED_BY_MANAGER;
            default -> throw new BusinessException("Unsupported approver role: " + role);
        };
    }

    private UserRole mapStatusToRole(ProposalStatus status) {
        return switch (status) {
            case WAITING_SV -> UserRole.SUPERVISOR;
            case WAITING_MANAGER -> UserRole.MANAGER;
            default -> throw new BusinessException("Status không hợp lệ để approve/reject: " + status);
        };
    }

    private boolean isWaitingStatus(ProposalStatus status) {
        return status == ProposalStatus.WAITING_SV || status == ProposalStatus.WAITING_MANAGER;
    }

    private boolean isRejectedStatus(ProposalStatus status) {
        return status == ProposalStatus.REJECTED_BY_SV || status == ProposalStatus.REJECTED_BY_MANAGER;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }
}