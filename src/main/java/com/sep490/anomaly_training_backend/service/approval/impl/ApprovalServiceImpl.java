package com.sep490.anomaly_training_backend.service.approval.impl;

import com.sep490.anomaly_training_backend.dto.approval.ApproveRequest;
import com.sep490.anomaly_training_backend.dto.approval.RejectFeedbackJson;
import com.sep490.anomaly_training_backend.dto.approval.RejectRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalAction;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.event.ApprovalEvent;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Approvable;
import com.sep490.anomaly_training_backend.model.ApprovalActionLog;
import com.sep490.anomaly_training_backend.model.ApprovalFlowStep;
import com.sep490.anomaly_training_backend.model.BaseEntity;
import com.sep490.anomaly_training_backend.model.DefectProposalDetail;
import com.sep490.anomaly_training_backend.model.RejectReason;
import com.sep490.anomaly_training_backend.model.RequiredAction;
import com.sep490.anomaly_training_backend.model.Role;
import com.sep490.anomaly_training_backend.model.TrainingPlanDetail;
import com.sep490.anomaly_training_backend.model.TrainingResultDetail;
import com.sep490.anomaly_training_backend.model.TrainingSampleProposalDetail;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalActionRepository;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.DefectProposalDetailRepository;
import com.sep490.anomaly_training_backend.repository.RejectReasonRepository;
import com.sep490.anomaly_training_backend.repository.RequiredActionRepository;
import com.sep490.anomaly_training_backend.repository.TrainingPlanDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingResultDetailRepository;
import com.sep490.anomaly_training_backend.repository.TrainingSampleProposalDetailRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalHandler;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import com.sep490.anomaly_training_backend.service.approval.ApprovalService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    private final DefectProposalDetailRepository defectProposalDetailRepository;
    private final TrainingSampleProposalDetailRepository trainingSampleProposalDetailRepository;
    private final TrainingPlanDetailRepository trainingPlanDetailRepository;
    private final TrainingResultDetailRepository trainingResultDetailRepository;

    @Override
    @Transactional
    public void submit(Approvable entity, User currentUser, HttpServletRequest request) {
        if (entity == null) {
            return;
        }

        ApprovalHandler handler = handlerRegistry.getHandler(entity.getEntityType());

        handler.validateBeforeSubmit(entity);
        handler.prepareForSubmit(entity);

        if (handler.requiresFlowStepOnSubmit()) {
            ApprovalFlowStep firstStep = getFirstStep(entity.getEntityType());
            entity.setStatus(firstStep.getPendingStatus());
        }

        boolean alreadySubmitted = actionRepo.findByEntityTypeAndEntityIdAndEntityVersionAndStepOrder(
                entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), 0).isPresent();

        if (!alreadySubmitted) {
            logAction(entity, ApprovalAction.SUBMIT, 0, "SUBMIT", currentUser, null, null, null, request);
        }
        log.info("Submitted {} id={} version={} by user={}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());

        publishEvent(ApprovalAction.SUBMIT, entity, currentUser);
    }

    @Override
    @Transactional
    public void revise(Approvable entity, User currentUser, HttpServletRequest request) {
        if (entity.getStatus() != ReportStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_ENTITY_STATUS, "Entity can only be revised when in REJECTED status. Current status: " + entity.getStatus());
        }

        entity.setCurrentVersion(entity.getCurrentVersion() + 1);
        entity.setStatus(ReportStatus.REVISING);

        logAction(entity, ApprovalAction.REVISE, -1, "REVISE", currentUser, null, null, null, request);
        log.info("Revised {} id={} newVersion={} by user={}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());
    }

    @Override
    @Transactional
    public void approve(Approvable entity, User currentUser, ApproveRequest req, HttpServletRequest request) {
        ApprovalFlowStep currentStep = getCurrentStep(entity);
        validateApprover(entity, currentUser, currentStep);

        logAction(entity, ApprovalAction.APPROVE, currentStep.getStepOrder(), currentStep.getRequiredPermission(), currentUser, req.getComment(), null, null, request);

        ApprovalHandler handler = handlerRegistry.getHandler(entity.getEntityType());

        if (handler.followsMultiStepFlow()) {
            ApprovalFlowStep nextStep = getNextStep(entity.getEntityType(), currentStep.getStepOrder());

            if (nextStep != null) {
                entity.setStatus(nextStep.getPendingStatus());
                log.info("Approved {} id={} version={} by {} -> next status: {}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername(), nextStep.getPendingStatus());
            } else {
                entity.setStatus(ReportStatus.COMPLETED);
                handler.applyApproval(entity);
                log.info("Final approval for {} id={} version={} by {}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername());
            }
        }

        handler.afterApprove(entity);
        publishEvent(ApprovalAction.APPROVE, entity, currentUser);
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

        logAction(entity, ApprovalAction.REJECT, currentStep.getStepOrder(), currentStep.getRequiredPermission(), currentUser, req.getComment(), new HashSet<>(reasons), requiredActions, request);

        ApprovalHandler handler = handlerRegistry.getHandler(entity.getEntityType());
        handler.afterReject(entity);

        log.info("Rejected {} id={} version={} by {} reasons={} requiredAction={}", entity.getEntityType(), entity.getId(), entity.getCurrentVersion(), currentUser.getUsername(), req.getRejectReasonIds(), req.getRequiredActionId());

        publishEvent(ApprovalAction.REJECT, entity, currentUser);
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
        return flowStepRepo.findByEntityTypeAndPendingStatusAndIsActiveTrue(entity.getEntityType(), status)
                .orElseThrow(() -> new AppException(ErrorCode.APPROVAL_STEP_NOT_FOUND,
                        "No approval step found for entity type " + entity.getEntityType() + " with status " + status));
    }

    private ApprovalFlowStep getNextStep(ApprovalEntityType entityType, int currentStepOrder) {
        List<ApprovalFlowStep> steps = flowStepRepo.findByEntityTypeAndIsActiveTrueOrderByStepOrderAsc(entityType);
        return steps.stream()
                .filter(s -> s.getStepOrder() > currentStepOrder)
                .findFirst()
                .orElse(null);
    }

    private void validateApprover(Approvable entity, User currentUser, ApprovalFlowStep step) {
        if (!currentUser.hasPermission(step.getRequiredPermission())) {
            throw new AppException(ErrorCode.INSUFFICIENT_PERMISSION, "Insufficient permission to approve at this step. Required: " + step.getRequiredPermission());
        }
        if (!routeService.isValidApprover(entity.getGroupId(), step.getRequiredPermission(), currentUser.getId())) {
            throw new AppException(ErrorCode.NOT_DESIGNATED_APPROVER);
        }
    }

    private void logAction(Approvable entity, ApprovalAction action, int stepOrder, String requiredPermission, User performer, String comment, Set<RejectReason> rejectReasons, Set<RequiredAction> requiredActions, HttpServletRequest request) {
        ApprovalActionLog logEntry = ApprovalActionLog.builder()
                .entityType(entity.getEntityType())
                .entityId(entity.getId())
                .entityVersion(entity.getCurrentVersion())
                .stepOrder(stepOrder)
                .requiredPermission(requiredPermission)
                .action(action)
                .performedByUser(performer)
                .performedByUsername(performer.getUsername())
                .performedByFullName(performer.getFullName())
                .performedByRole(performer.getRoles().stream().findFirst().map(Role::getRoleCode).orElse("UNKNOWN"))
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

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        return ip != null ? ip.split(",")[0].trim() : request.getRemoteAddr();
    }

    /**
     * Publish ApprovalEvent cho notification listener xử lý async.
     */
    private void publishEvent(ApprovalAction action, Approvable entity, User performedBy) {
        try {
            String createdByUsername = (entity instanceof BaseEntity base)
                    ? base.getCreatedBy()
                    : null;

            log.info("[ApprovalEvent] Publishing {} for {} id={} performedBy={}",
                    action, entity.getEntityType(), entity.getId(), performedBy.getUsername());
            eventPublisher.publishEvent(new ApprovalEvent(
                    this,
                    action,
                    entity.getEntityType(),
                    entity.getId(),
                    entity.getGroupId(),
                    entity.getEntityLabel(),
                    performedBy,
                    entity.getStatus(),
                    createdByUsername
            ));
        } catch (Exception e) {
            // Không để event publishing lỗi ảnh hưởng approval flow
            log.error("[ApprovalEvent] Failed to publish {} for {} id={}: {}",
                    action, entity.getEntityType(), entity.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void saveFeedback(ApprovalEntityType entityType, Long detailId, RejectRequest request, User currentUser) {
        switch (entityType) {
            case DEFECT_PROPOSAL -> doSaveFeedback(
                    detailId, request, currentUser,
                    defectProposalDetailRepository::findByIdAndDeleteFlagFalse,
                    DefectProposalDetail::setRejectFeedback,
                    defectProposalDetailRepository::save);
            case TRAINING_SAMPLE_PROPOSAL -> doSaveFeedback(
                    detailId, request, currentUser,
                    trainingSampleProposalDetailRepository::findByIdAndDeleteFlagFalse,
                    TrainingSampleProposalDetail::setRejectFeedback,
                    trainingSampleProposalDetailRepository::save);
            case TRAINING_PLAN -> doSaveFeedback(
                    detailId, request, currentUser,
                    trainingPlanDetailRepository::findByIdAndDeleteFlagFalse,
                    TrainingPlanDetail::setRejectFeedback,
                    trainingPlanDetailRepository::save);
            case TRAINING_RESULT -> doSaveFeedback(
                    detailId, request, currentUser,
                    trainingResultDetailRepository::findById,
                    TrainingResultDetail::setRejectFeedback,
                    trainingResultDetailRepository::save);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
    }

    /**
     * Generic feedback handler — chỉ cần truyền vào cách load, set feedback, và save entity.
     */
    private <T> void doSaveFeedback(
            Long detailId,
            RejectRequest request,
            User currentUser,
            java.util.function.Function<Long, java.util.Optional<T>> loader,
            java.util.function.BiConsumer<T, RejectFeedbackJson> feedbackSetter,
            java.util.function.Consumer<T> saver) {

        T detail = loader.apply(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.PROPOSAL_DETAIL_NOT_FOUND));

        if (isEmptyFeedback(request)) {
            feedbackSetter.accept(detail, null);
            saver.accept(detail);
            return;
        }

        feedbackSetter.accept(detail, buildFeedbackJson(request, currentUser));
        saver.accept(detail);
        log.info("[RejectFeedback] detailId={} updated by {}", detailId, currentUser.getUsername());
    }

    private RejectFeedbackJson buildFeedbackJson(RejectRequest request, User currentUser) {
        List<RejectFeedbackJson.RejectReasonSnapshot> reasonSnapshots = List.of();
        if (request.getRejectReasonIds() != null && !request.getRejectReasonIds().isEmpty()) {
            reasonSnapshots = rejectReasonRepo.findAllById(request.getRejectReasonIds()).stream()
                    .map(r -> RejectFeedbackJson.RejectReasonSnapshot.builder()
                            .id(r.getId())
                            .category(r.getCategoryName())
                            .label(r.getReasonName())
                            .build())
                    .toList();
        }

        RejectFeedbackJson.RequiredActionSnapshot actionSnapshot = null;
        if (request.getRequiredActionId() != null) {
            actionSnapshot = requiredActionRepo.findById(request.getRequiredActionId())
                    .map(a -> RejectFeedbackJson.RequiredActionSnapshot.builder()
                            .id(a.getId())
                            .label(a.getActionName())
                            .build())
                    .orElse(null);
        }

        return RejectFeedbackJson.builder()
                .savedAt(Instant.now())
                .savedBy(currentUser.getFullName())
                .rejectReasons(reasonSnapshots.isEmpty() ? null : reasonSnapshots)
                .requiredAction(actionSnapshot)
                .comment(request.getComment())
                .build();
    }

    private boolean isEmptyFeedback(RejectRequest r) {
        return (r.getRejectReasonIds() == null || r.getRejectReasonIds().isEmpty())
                && r.getRequiredActionId() == null
                && (r.getComment() == null || r.getComment().isBlank());
    }
}