package com.sep490.anomaly_training_backend.event;

import com.sep490.anomaly_training_backend.dto.request.UnifiedNotificationRequest;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.InAppNotificationType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.model.User;
import com.sep490.anomaly_training_backend.repository.ApprovalFlowStepRepository;
import com.sep490.anomaly_training_backend.repository.UserRepository;
import com.sep490.anomaly_training_backend.service.approval.ApprovalRouteService;
import com.sep490.anomaly_training_backend.service.notification.impl.UnifiedNotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Async listener nhận {@link ApprovalEvent} và dispatch notification
 * qua {@link UnifiedNotificationDispatcher} (email + in-app).
 *
 * <p>Luồng notification hoàn toàn tách biệt khỏi business logic —
 * nếu notification lỗi, approval flow không bị ảnh hưởng.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApprovalNotificationListener {

    private final UnifiedNotificationDispatcher dispatcher;
    private final ApprovalRouteService approvalRouteService;
    private final ApprovalFlowStepRepository flowStepRepo;
    private final UserRepository userRepository;

    @Async("notificationExecutor")
    @EventListener
    public void onApprovalEvent(ApprovalEvent event) {
        try {
            switch (event.getAction()) {
                case SUBMIT -> handleSubmit(event);
                case APPROVE -> handleApprove(event);
                case REJECT -> handleReject(event);
            }
        } catch (Exception e) {
            log.error("[ApprovalNotification] Failed to process {} for {} id={}: {}",
                    event.getAction(), event.getEntityType(), event.getEntityId(), e.getMessage(), e);
        }
    }

    // ── SUBMIT: Thông báo người cần ký bước tiếp theo ──────────────────────

    private void handleSubmit(ApprovalEvent event) {
        String label = entityTypeLabel(event.getEntityType());

        log.info("[ApprovalNotification] Handling SUBMIT for {} id={}", event.getEntityType(), event.getEntityId());

        resolveNextApprover(event).ifPresent(approver ->
                dispatch(approver,
                        "Có tài liệu mới cần duyệt",
                        String.format("%s \"%s\" đã được gửi bởi %s, cần bạn kiểm duyệt.",
                                label, event.getEntityLabel(), event.getPerformedBy().getFullName()),
                        InAppNotificationType.ACTION_REQUIRED,
                        event, "SUBMIT"));
    }

    // ── APPROVE: Thông báo người ký tiếp hoặc người tạo (nếu final) ───────

    private void handleApprove(ApprovalEvent event) {
        String label = entityTypeLabel(event.getEntityType());

        log.info("[ApprovalNotification] Handling APPROVE for {} id={}, newStatus={}",
                event.getEntityType(), event.getEntityId(), event.getNewStatus());

        if (event.getNewStatus() == ReportStatus.COMPLETED) {
            // Final approval → thông báo người tạo
            findCreator(event).ifPresent(creator ->
                    dispatch(creator,
                            label + " đã được phê duyệt",
                            String.format("%s \"%s\" đã được %s phê duyệt hoàn tất.",
                                    label, event.getEntityLabel(), event.getPerformedBy().getFullName()),
                            InAppNotificationType.SUCCESS,
                            event, "APPROVE (final)"));
        } else {
            // Intermediate approval → thông báo người ký step kế tiếp
            resolveNextApprover(event).ifPresent(nextApprover ->
                    dispatch(nextApprover,
                            "Tài liệu cần phê duyệt",
                            String.format("%s \"%s\" đã được %s duyệt, cần bạn phê duyệt tiếp.",
                                    label, event.getEntityLabel(), event.getPerformedBy().getFullName()),
                            InAppNotificationType.ACTION_REQUIRED,
                            event, "APPROVE (intermediate)"));
        }
    }

    // ── REJECT: Thông báo người tạo ────────────────────────────────────────

    private void handleReject(ApprovalEvent event) {
        String label = entityTypeLabel(event.getEntityType());

        log.info("[ApprovalNotification] Handling REJECT for {} id={}", event.getEntityType(), event.getEntityId());

        findCreator(event).ifPresent(creator ->
                dispatch(creator,
                        label + " bị từ chối",
                        String.format("%s \"%s\" đã bị %s từ chối. Vui lòng kiểm tra và chỉnh sửa.",
                                label, event.getEntityLabel(), event.getPerformedBy().getFullName()),
                        InAppNotificationType.WARNING,
                        event, "REJECT"));
    }

    // ── Core dispatch ──────────────────────────────────────────────────────

    private void dispatch(User recipient, String title, String message,
                          InAppNotificationType type, ApprovalEvent event, String actionLabel) {
        dispatcher.dispatch(UnifiedNotificationRequest.builder()
                .recipientUserId(recipient.getId())
                .recipientEmail(recipient.getEmail())
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityType(event.getEntityType().name())
                .relatedEntityId(event.getEntityId())
                .actionUrl(buildActionUrl(event.getEntityType(), event.getEntityId()))
                .sendEmail(true)
                .sendInApp(true)
                .build());

        log.info("[ApprovalNotification] {} → notified {} <{}> for {} id={}",
                actionLabel, recipient.getFullName(), recipient.getEmail(),
                event.getEntityType(), event.getEntityId());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Resolve người ký tiếp theo dựa trên newStatus → tìm step → resolve approver trong group.
     */
    private Optional<User> resolveNextApprover(ApprovalEvent event) {
        if (event.getNewStatus() == null || event.getGroupId() == null) {
            return Optional.empty();
        }
        return flowStepRepo
                .findByEntityTypeAndPendingStatusAndIsActiveTrue(event.getEntityType(), event.getNewStatus())
                .flatMap(step -> approvalRouteService.resolveExpectedApprover(event.getGroupId(), step.getRequiredPermission()));
    }

    /**
     * Tìm người tạo entity dựa trên createdByUsername (từ BaseEntity.createdBy audit field).
     */
    private Optional<User> findCreator(ApprovalEvent event) {
        if (!StringUtils.hasText(event.getCreatedByUsername())) {
            log.warn("[ApprovalNotification] No createdByUsername for {} id={}",
                    event.getEntityType(), event.getEntityId());
            return Optional.empty();
        }
        return userRepository.findByUsername(event.getCreatedByUsername());
    }

    private String buildActionUrl(ApprovalEntityType entityType, Long entityId) {
        String path = switch (entityType) {
            case TRAINING_PLAN -> "training-plans";
            case TRAINING_RESULT -> "training-results";
            case DEFECT_PROPOSAL -> "defect-proposals";
            case TRAINING_SAMPLE_PROPOSAL -> "training-sample-proposals";
            case TRAINING_SAMPLE_REVIEW -> "training-sample-reviews";
        };
        return "/" + path + "/" + entityId;
    }

    private String entityTypeLabel(ApprovalEntityType type) {
        return switch (type) {
            case TRAINING_PLAN -> "Kế hoạch huấn luyện";
            case TRAINING_RESULT -> "Kết quả huấn luyện";
            case DEFECT_PROPOSAL -> "Đề xuất chỉnh sửa lỗi quá khứ";
            case TRAINING_SAMPLE_PROPOSAL -> "Đề xuất chỉnh sửa mẫu huấn luyện";
            case TRAINING_SAMPLE_REVIEW -> "Kiểm tra hàng năm";
        };
    }
}
