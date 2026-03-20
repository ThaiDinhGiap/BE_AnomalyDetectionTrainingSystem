package com.sep490.anomaly_training_backend.enums;

public enum NotificationType {

    // ============================================
    // TRAINING PLAN:  TL(PRO) → SV → Manager
    // ============================================
    PLAN_WAITING_SV,              // TL gửi, thông báo SV cần duyệt
    PLAN_WAITING_MANAGER,         // SV duyệt, thông báo Manager cần duyệt
    PLAN_APPROVED,                // Manager duyệt, thông báo TL kế hoạch đã được duyệt hoàn tất
    PLAN_REJECTED_BY_SV,          // SV từ chối, thông báo TL
    PLAN_REJECTED_BY_MANAGER,     // Manager từ chối, thông báo TL

    // ============================================
    // TRAINING RESULT: TL(PRO) → TL(FI) → SV → Manager
    // ============================================
    RESULT_WAITING_FI,            // TL(PRO) gửi, thông báo TL(FI) cần xác nhận
    RESULT_WAITING_SV,            // TL(FI) duyệt, thông báo SV cần duyệt
    RESULT_WAITING_MANAGER,       // SV duyệt, thông báo Manager cần duyệt
    RESULT_APPROVED,              // Manager duyệt, thông báo TL(PRO) kết quả đã được duyệt hoàn tất
    RESULT_REJECTED_BY_FI,        // TL(FI) từ chối, thông báo TL(PRO)
    RESULT_REJECTED_BY_SV,        // SV từ chối, thông báo TL(PRO)
    RESULT_REJECTED_BY_MANAGER,   // Manager từ chối, thông báo TL(PRO)

    // ============================================
    // DEFECT REPORT: TL → SV → Manager
    // ============================================
    DEFECT_WAITING_SV,             // TL gửi, thông báo SV cần duyệt
    DEFECT_WAITING_MANAGER,        // SV duyệt, thông báo Manager cần duyệt
    DEFECT_APPROVED,               // Manager duyệt, thông báo TL
    DEFECT_REJECTED_BY_SV,         // SV từ chối, thông báo TL
    DEFECT_REJECTED_BY_MANAGER,    // Manager từ chối, thông báo TL

    // ============================================
    // TRAINING SAMPLE: TL → SV → Manager
    // ============================================
    SAMPLE_WAITING_SV,             // TL gửi, thông báo SV cần duyệt
    SAMPLE_WAITING_MANAGER,        // SV duyệt, thông báo Manager cần duyệt
    SAMPLE_APPROVED,               // Manager duyệt, thông báo TL
    SAMPLE_REJECTED_BY_SV,         // SV từ chối, thông báo TL
    SAMPLE_REJECTED_BY_MANAGER,
    SAMPLE_REVIEW_OVERDUE,// Manager từ chối, thông báo TL

    // ============================================
    // REMINDERS (Scheduler tự động gửi)
    // ============================================
    TRAINING_REMINDER_TODAY,      // Nhắc TL:  có lịch kiểm tra hôm nay
    TRAINING_REMINDER_UPCOMING,   // Nhắc TL: có lịch kiểm tra sắp tới (1 ngày trước)
    TRAINING_OVERDUE_WARNING,     // Cảnh báo TL: lịch kiểm tra quá hạn chưa ghi nhận

    // ============================================
    // APPROVAL OVERDUE (Nagging - Use Case 5)
    // ============================================
    APPROVAL_OVERDUE_SV,          // Nhắc SV: có phê duyệt tồn đọng
    APPROVAL_OVERDUE_MANAGER,     // Nhắc Manager: có phê duyệt tồn đọng
    APPROVAL_OVERDUE_FI,           // Nhắc TL(FI): có xác nhận kết quả tồn đọng
    APPROVAL_NUDGE
}