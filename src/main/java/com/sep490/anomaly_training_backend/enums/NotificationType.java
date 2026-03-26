package com.sep490.anomaly_training_backend.enums;

public enum NotificationType {

    // ============================================
    // TRAINING PLAN:  TL(PRO) → Reviewer → Approver
    // ============================================
    PLAN_PENDING_REVIEW,           // TL gửi, thông báo reviewer cần kiểm duyệt
    PLAN_PENDING_APPROVAL,         // Reviewer duyệt, thông báo approver cần phê duyệt
    PLAN_APPROVED,                 // Approver duyệt, thông báo TL kế hoạch đã được duyệt hoàn tất
    PLAN_REJECTED,                 // Bị từ chối, thông báo TL

    // ============================================
    // TRAINING RESULT: TL(PRO) → TL(FI) → Reviewer → Approver
    // ============================================
    RESULT_WAITING_FI,             // TL(PRO) gửi, thông báo TL(FI) cần xác nhận
    RESULT_PENDING_REVIEW,         // TL(FI) duyệt, thông báo reviewer cần kiểm duyệt
    RESULT_PENDING_APPROVAL,       // Reviewer duyệt, thông báo approver cần phê duyệt
    RESULT_APPROVED,               // Approver duyệt, thông báo TL(PRO) kết quả đã được duyệt hoàn tất
    RESULT_REJECTED_BY_FI,         // TL(FI) từ chối, thông báo TL(PRO)
    RESULT_REJECTED,               // Bị từ chối, thông báo TL(PRO)

    // ============================================
    // DEFECT REPORT: TL → Reviewer → Approver
    // ============================================
    DEFECT_PENDING_REVIEW,         // TL gửi, thông báo reviewer cần kiểm duyệt
    DEFECT_PENDING_APPROVAL,       // Reviewer duyệt, thông báo approver cần phê duyệt
    DEFECT_APPROVED,               // Approver duyệt, thông báo TL
    DEFECT_REJECTED,               // Bị từ chối, thông báo TL

    // ============================================
    // TRAINING SAMPLE: TL → Reviewer → Approver
    // ============================================
    SAMPLE_PENDING_REVIEW,         // TL gửi, thông báo reviewer cần kiểm duyệt
    SAMPLE_PENDING_APPROVAL,       // Reviewer duyệt, thông báo approver cần phê duyệt
    SAMPLE_APPROVED,               // Approver duyệt, thông báo TL
    SAMPLE_REJECTED,               // Bị từ chối, thông báo TL
    SAMPLE_REVIEW_OVERDUE,         // Kiểm tra hàng năm quá hạn

    // ============================================
    // REMINDERS (Scheduler tự động gửi)
    // ============================================
    TRAINING_REMINDER_TODAY,       // Nhắc TL: có lịch kiểm tra hôm nay
    TRAINING_REMINDER_UPCOMING,    // Nhắc TL: có lịch kiểm tra sắp tới (1 ngày trước)
    TRAINING_OVERDUE_WARNING,      // Cảnh báo TL: lịch kiểm tra quá hạn chưa ghi nhận

    // ============================================
    // APPROVAL OVERDUE (Nagging)
    // ============================================
    APPROVAL_OVERDUE_SV,           // Nhắc reviewer: có phê duyệt tồn đọng
    APPROVAL_OVERDUE_MANAGER,      // Nhắc approver: có phê duyệt tồn đọng
    APPROVAL_OVERDUE_FI,           // Nhắc TL(FI): có xác nhận kết quả tồn đọng
    APPROVAL_NUDGE
}