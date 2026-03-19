package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.enums.UserRole;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.RejectReason;
import com.sep490.anomaly_training_backend.model.RequiredAction;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalFlowStepRepository flowStepRepo;
    private final ApprovalActionRepository actionRepo;
    private final ApprovalRouteService routeService;
    private final RejectReasonRepository rejectReasonRepo;
    private final RequiredActionRepository requiredActionRepo;
    private final ApprovalHandlerRegistry handlerRegistry;

    @Override
    @Transactional
    public void submit(Approvable entity, User currentUser, HttpServletRequest request) {
        if ((entity.getStatus() != ReportStatus.DRAFT) && (entity.getStatus() != ReportStatus.REVISE)) {
            throw new AppException(ErrorCode.INVALID_ENTITY_STATUS, "Entity can only be submitted when in DRAFT/REVISE status");
        }

        ApprovalFlowStep firstStep = getFirstStep(entity.getEntityType());
        ReportStatus pendingStatus = mapRoleToPendingStatus(firstStep.getApproverRole());
        entity.setStatus(pendingStatus);

        logAction(entity, ApprovalAction.SUBMIT, 0, UserRole.ROLE_TEAM_LEADER, currentUser, null, null, null, request);
        log.info("Submitted {} id={} version={} by user={}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());
    }

    @Override
    @Transactional
    public void revise(Approvable entity, User currentUser, HttpServletRequest request) {
        if (!isRejectedStatus(entity.getStatus())) {
            throw new AppException(ErrorCode.INVALID_ENTITY_STATUS, "Entity can only be revised when in a rejected status. Current status: " + entity.getStatus());
        }

        entity.setCurrentVersion(entity.getCurrentVersion() + 1);
        entity.setStatus(ReportStatus.REVISE);

        logAction(entity, ApprovalAction.REVISE, -1, UserRole.ROLE_TEAM_LEADER, currentUser, null, null, null, request);
        log.info("Revised {} id={} newVersion={} by user={}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());
    }

    @Override
    @Transactional
    public void approve(Approvable entity, User currentUser, ApproveRequest req, HttpServletRequest request) {
        ApprovalFlowStep currentStep = getCurrentStep(entity);
        validateApprover(entity, currentUser, currentStep);

        logAction(entity, ApprovalAction.APPROVE, currentStep.getStepOrder(), currentStep.getApproverRole(), currentUser, req.getComment(), null, null, request);

        ApprovalFlowStep nextStep = getNextStep(entity.getEntityType(), currentStep.getStepOrder());

        if (nextStep != null) {
            ReportStatus nextPendingStatus = mapRoleToPendingStatus(nextStep.getApproverRole());
            entity.setStatus(nextPendingStatus);
            log.info("Approved {} id={} version={} by {} -> next status: {}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername(), nextPendingStatus);
        } else {
            entity.setStatus(ReportStatus.APPROVED);
            ApprovalHandler handler = handlerRegistry.getHandler(entity.getEntityType());
            handler.applyApproval(entity);
            log.info("Final approval for {} id={} version={} by {}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());
        }
    }

    @Override
    @Transactional
    public void reject(Approvable entity, User currentUser, RejectRequest req, HttpServletRequest request) {
        if (req.getRejectReasonIds() == null || req.getRejectReasonIds().isEmpty()) {
            throw new AppException(ErrorCode.REJECT_REASON_REQUIRED);
        }

        List<RejectReason> reasons = rejectReasonRepo.findAllById(req.getRejectReasonIds());
        if (reasons.size() != req.getRejectReasonIds().size()) {
            throw new AppException(ErrorCode.INVALID_REJECT_REASON);
        }

        Set<RequiredAction> requiredActions = new HashSet<>();
        if (req.getRequiredActionId() != null) {
            RequiredAction action = requiredActionRepo.findById(req.getRequiredActionId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUIRED_ACTION));
            requiredActions.add(action);
        }

        ApprovalFlowStep currentStep = getCurrentStep(entity);
        validateApprover(entity, currentUser, currentStep);

        logAction(entity, ApprovalAction.REJECT, currentStep.getStepOrder(), currentStep.getApproverRole(), currentUser, req.getComment(), new HashSet<>(reasons), requiredActions, request);

        entity.setStatus(mapRoleToRejectedStatus(currentStep.getApproverRole()));
        log.info("Rejected {} id={} version={} by {} reasons={} requiredAction={}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername(), req.getRejectReasonIds(), req.getRequiredActionId());
    }

    @Override
    public List<ApprovalActionLog> getApprovalHistory(ApprovalEntityType entityType, Long entityId) {
        return actionRepo.findByEntityTypeAndEntityIdOrderByPerformedAtAsc(entityType, entityId);
    }

    @Override
    public List<ApprovalActionLog> getApprovalHistoryByVersion(ApprovalEntityType entityType, Long entityId, Integer version) {
        return actionRepo.findByEntityTypeAndEntityIdAndEntityVersionOrderByPerformedAtAsc(entityType, entityId, version);
    }

    @Override
    public Boolean canApprove(Approvable entity, User user) {
        try {
            ApprovalFlowStep currentStep = getCurrentStep(entity);
            validateApprover(entity, user, currentStep);
            return Boolean.TRUE;
        } catch (AppException e) {
            return Boolean.FALSE;
        }
    }

    private ApprovalFlowStep getFirstStep(ApprovalEntityType entityType) {
        return flowStepRepo.findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType)
                .stream()
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.APPROVAL_WORKFLOW_NOT_FOUND));
    }

    private ApprovalFlowStep getCurrentStep(Approvable entity) {
        ReportStatus status = entity.getStatus();
        if (!isWaitingStatus(status)) {
            throw new AppException(ErrorCode.INVALID_ENTITY_STATUS, "Entity is not in a pending approval status: " + status);
        }
        UserRole requiredRole = mapStatusToRole(status);
        return flowStepRepo.findByEntityTypeAndApproverRoleAndIsActiveTrue(entity.getEntityType(), requiredRole)
                .orElseThrow(() -> new AppException(ErrorCode.APPROVAL_STEP_NOT_FOUND));
    }

    private ApprovalFlowStep getNextStep(ApprovalEntityType entityType, int currentStepOrder) {
        List<ApprovalFlowStep> steps = flowStepRepo.findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType);
        return steps.stream()
                .filter(s -> s.getStepOrder() > currentStepOrder)
                .findFirst()
                .orElse(null);
    }

    private void validateApprover(Approvable entity, User currentUser, ApprovalFlowStep step) {
        if (!currentUser.hasRole(step.getApproverRole().name())) {
            throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION, "Insufficient role to approve at this step. Required role: " + step.getApproverRole());
        }
        if (!routeService.isValidApprover(entity.getGroupId(), step.getApproverRole(), currentUser.getId())) {
            throw new AppException(ErrorCode.NOT_DESIGNATED_APPROVER);
        }
    }

    private void logAction(Approvable entity, ApprovalAction action, int stepOrder, UserRole requiredRole, User performer, String comment, Set<RejectReason> rejectReasons, Set<RequiredAction> requiredActions, HttpServletRequest request) {
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
                .performedByRole(UserRole.valueOf(performer.getRoles().stream().findFirst().map(Role::getRoleCode).orElse("UNKNOWN")))
                .comment(comment)
                .performedAt(Instant.now())
                .ipAddress(getClientIp(request))
                .userAgent(request != null ? request.getHeader("User-Agent") : null)
                .contentHash(entity.computeContentHash())
                .rejectReasons(rejectReasons != null ? rejectReasons : new HashSet<>())
                .requiredActions(requiredActions != null ? requiredActions : new HashSet<>())
                .build();
        actionRepo.save(logEntry);
    }

    private ReportStatus mapRoleToPendingStatus(UserRole role) {
        return switch (role) {
            case ROLE_SUPERVISOR -> ReportStatus.WAITING_SV;
            case ROLE_MANAGER -> ReportStatus.WAITING_MANAGER;
            default -> throw new AppException(ErrorCode.UNSUPPORTED_APPROVER_ROLE);
        };
    }

    private ReportStatus mapRoleToRejectedStatus(UserRole role) {
        return switch (role) {
            case ROLE_SUPERVISOR -> ReportStatus.REJECTED_BY_SV;
            case ROLE_MANAGER -> ReportStatus.REJECTED_BY_MANAGER;
            default -> throw new AppException(ErrorCode.UNSUPPORTED_APPROVER_ROLE);
        };
    }

    private UserRole mapStatusToRole(ReportStatus status) {
        return switch (status) {
            case WAITING_SV -> UserRole.ROLE_SUPERVISOR;
            case WAITING_MANAGER -> UserRole.ROLE_MANAGER;
            default ->
                    throw new AppException(ErrorCode.INVALID_ENTITY_STATUS, "Invalid status for approve/reject operation: " + status);
        };
    }

    private boolean isWaitingStatus(ReportStatus status) {
        return status == ReportStatus.WAITING_SV || status == ReportStatus.WAITING_MANAGER;
    }

    private boolean isRejectedStatus(ReportStatus status) {
        return status == ReportStatus.REJECTED_BY_SV || status == ReportStatus.REJECTED_BY_MANAGER;
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }
}