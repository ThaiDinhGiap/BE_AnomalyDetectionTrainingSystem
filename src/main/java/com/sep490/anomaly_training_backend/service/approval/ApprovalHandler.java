package com.sep490.anomaly_training_backend.service.approval;

import com.sep490.anomaly_training_backend.dto.approval.OverdueItem;
import com.sep490.anomaly_training_backend.enums.ApprovalEntityType;
import com.sep490.anomaly_training_backend.enums.ReportStatus;
import com.sep490.anomaly_training_backend.exception.AppException;
import com.sep490.anomaly_training_backend.exception.ErrorCode;
import com.sep490.anomaly_training_backend.model.Approvable;

import java.time.LocalDateTime;
import java.util.List;

public interface ApprovalHandler {

    ApprovalEntityType getType();

    // ── Overdue check hooks (used by ApprovalOverdueScheduler) ──

    /**
     * Nhãn hiển thị tiếng Việt cho entity type, dùng trong notification.
     * Default: tên enum.
     */
    default String getDisplayLabel() {
        return getType().name();
    }

    /**
     * Tìm các entity quá hạn phê duyệt theo status + thời gian threshold.
     * Default: empty list (entity type không tham gia overdue check).
     */
    default List<OverdueItem> findOverdueItems(ReportStatus status, LocalDateTime threshold) {
        return List.of();
    }

    /**
     * Validate entity status trước khi submit.
     * Default: chỉ chấp nhận DRAFT, REVISING, hoặc PENDING_REVIEW.
     */
    default void validateBeforeSubmit(Approvable entity) {
        ReportStatus status = entity.getStatus();
        if (status != ReportStatus.DRAFT && status != ReportStatus.REVISING && status != ReportStatus.PENDING_REVIEW) {
            throw new AppException(ErrorCode.INVALID_ENTITY_STATUS,
                    "Entity can only be submitted when in DRAFT/REVISE status");
        }
    }

    /**
     * Chuẩn bị entity trước khi submit (ví dụ: clear feedback cũ).
     * Default: clearRejectFeedback().
     */
    default void prepareForSubmit(Approvable entity) {
        entity.clearRejectFeedback();
    }

    /**
     * Có yêu cầu lookup flow step và set pending status khi submit không?
     * Default: true (multi-step flow).
     */
    default boolean requiresFlowStepOnSubmit() {
        return true;
    }

    /**
     * Có follow multi-step approval flow khi approve không?
     * Nếu false: skip next step lookup, chỉ gọi afterApprove().
     * Default: true.
     */
    default boolean followsMultiStepFlow() {
        return true;
    }

    /**
     * Hook chạy sau mỗi lần approve (bất kỳ step nào, kể cả intermediate).
     * Default: no-op.
     */
    default void afterApprove(Approvable entity) {
        // no-op by default
    }

    /**
     * Hook chạy sau khi bị reject.
     * Default: set status REJECTED.
     */
    default void afterReject(Approvable entity) {
        entity.setStatus(ReportStatus.REJECTED);
    }

    /**
     * Side-effect khi final approval hoàn tất (bước cuối cùng của multi-step flow).
     * Ví dụ: DefectProposal → tạo Defect, TrainingPlan → generate TrainingResult.
     */
    void applyApproval(Approvable entity);
}
