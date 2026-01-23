-- ============================================
-- NOTIFICATION TEMPLATES
-- ============================================

-- TRAINING PLAN
INSERT INTO notification_templates (code, subject_template, body_template, description, delete_flag, created_at)
VALUES ('PLAN_WAITING_SV', '[Training] Kế hoạch huấn luyện cần phê duyệt:  $${planTitle}',
        'email/plan-waiting-approval', 'TL gửi kế hoạch, thông báo SV phê duyệt', false, NOW()),
       ('PLAN_WAITING_MANAGER', '[Training] Kế hoạch huấn luyện cần phê duyệt: $${planTitle}',
        'email/plan-waiting-approval', 'SV duyệt, thông báo Manager phê duyệt', false, NOW()),
       ('PLAN_APPROVED', '[Training] Kế hoạch huấn luyện đã được phê duyệt:  $${planTitle}',
        'email/plan-approved',
        'Kế hoạch đã được duyệt hoàn tất', false, NOW()),
       ('PLAN_REJECTED_BY_SV', '[Training] Kế hoạch huấn luyện bị từ chối: $${planTitle}', 'email/plan-rejected',
        'SV từ chối kế hoạch', false, NOW()),
       ('PLAN_REJECTED_BY_MANAGER', '[Training] Kế hoạch huấn luyện bị từ chối: $${planTitle}',
        'email/plan-rejected', 'Manager từ chối kế hoạch', false, NOW()),

-- TRAINING RESULT
       ('RESULT_WAITING_FI', '[Training] Kết quả huấn luyện cần xác nhận', 'email/result-waiting-fi',
        'TL(PRO) gửi, thông báo TL(FI) xác nhận', false, NOW()),
       ('RESULT_WAITING_SV', '[Training] Kết quả huấn luyện cần phê duyệt', 'email/result-waiting-approval',
        'TL(FI) duyệt, thông báo SV phê duyệt', false, NOW()),
       ('RESULT_WAITING_MANAGER', '[Training] Kết quả huấn luyện cần phê duyệt', 'email/result-waiting-approval',
        'SV duyệt, thông báo Manager phê duyệt', false, NOW()),
       ('RESULT_APPROVED', '[Training] Kết quả huấn luyện đã được phê duyệt', 'email/result-approved',
        'Kết quả đã được duyệt hoàn tất', false, NOW()),
       ('RESULT_REJECTED_BY_FI', '[Training] Kết quả huấn luyện bị từ chối bởi Final Inspection',
        'email/result-rejected', 'TL(FI) từ chối kết quả', false, NOW()),
       ('RESULT_REJECTED_BY_SV', '[Training] Kết quả huấn luyện bị từ chối bởi Supervisor',
        'email/result-rejected', 'SV từ chối kết quả', false, NOW()),
       ('RESULT_REJECTED_BY_MANAGER', '[Training] Kết quả huấn luyện bị từ chối bởi Manager',
        'email/result-rejected', 'Manager từ chối kết quả', false, NOW()),

-- ISSUE REPORT
       ('ISSUE_WAITING_SV', '[Training] Báo cáo sự cố cần xem xét', 'email/issue-waiting-approval',
        'TL gửi báo cáo, thông báo SV xem xét', false, NOW()),
       ('ISSUE_WAITING_MANAGER', '[Training] Báo cáo sự cố cần phê duyệt', 'email/issue-waiting-approval',
        'SV duyệt, thông báo Manager phê duyệt', false, NOW()),
       ('ISSUE_APPROVED', '[Training] Báo cáo sự cố đã được phê duyệt', 'email/issue-approved',
        'Báo cáo đã được duyệt hoàn tất', false, NOW()),
       ('ISSUE_REJECTED_BY_SV', '[Training] Báo cáo sự cố bị từ chối bởi Supervisor', 'email/issue-rejected',
        'SV từ chối báo cáo', false, NOW()),
       ('ISSUE_REJECTED_BY_MANAGER', '[Training] Báo cáo sự cố bị từ chối bởi Manager', 'email/issue-rejected',
        'Manager từ chối báo cáo', false, NOW()),

-- TRAINING TOPIC
       ('TOPIC_WAITING_SV', '[Training] Nội dung huấn luyện cần phê duyệt:  $${topicTitle}',
        'email/topic-waiting-approval', 'TL gửi topic, thông báo SV phê duyệt', false, NOW()),
       ('TOPIC_WAITING_MANAGER', '[Training] Nội dung huấn luyện cần phê duyệt: $${topicTitle}',
        'email/topic-waiting-approval', 'SV duyệt, thông báo Manager phê duyệt', false, NOW()),
       ('TOPIC_APPROVED', '[Training] Nội dung huấn luyện đã được phê duyệt: $${topicTitle}',
        'email/topic-approved', 'Topic đã được duyệt hoàn tất', false, NOW()),
       ('TOPIC_REJECTED_BY_SV', '[Training] Nội dung huấn luyện bị từ chối:  $${topicTitle}',
        'email/topic-rejected', 'SV từ chối topic', false, NOW()),
       ('TOPIC_REJECTED_BY_MANAGER', '[Training] Nội dung huấn luyện bị từ chối: $${topicTitle}',
        'email/topic-rejected', 'Manager từ chối topic', false, NOW()),

-- REMINDERS
       ('TRAINING_REMINDER_TODAY', '[Training] Nhắc nhở:  Lịch kiểm tra huấn luyện hôm nay',
        'email/training-reminder-today', 'Nhắc TL có lịch kiểm tra hôm nay', false, NOW()),
       ('TRAINING_REMINDER_UPCOMING', '[Training] Nhắc nhở: Lịch kiểm tra huấn luyện ngày mai',
        'email/training-reminder-upcoming', 'Nhắc TL có lịch kiểm tra sắp tới', false, NOW()),
       ('TRAINING_OVERDUE_WARNING', '[Training] Cảnh báo: Lịch kiểm tra huấn luyện quá hạn',
        'email/training-overdue-warning', 'Cảnh báo TL lịch kiểm tra quá hạn', false, NOW()),

-- APPROVAL OVERDUE (Nagging)
       ('APPROVAL_OVERDUE_SV', '[Training] Nhắc nhở: Có $${pendingCount} phê duyệt đang chờ xử lý',
        'email/approval-overdue', 'Nhắc SV xử lý phê duyệt tồn đọng', false, NOW()),
       ('APPROVAL_OVERDUE_MANAGER', '[Training] Nhắc nhở: Có $${pendingCount} phê duyệt đang chờ xử lý',
        'email/approval-overdue', 'Nhắc Manager xử lý phê duyệt tồn đọng', false, NOW()),
       ('APPROVAL_OVERDUE_FI', '[Training] Nhắc nhở:  Có $${pendingCount} kết quả cần xác nhận',
        'email/approval-overdue-fi', 'Nhắc TL(FI) xác nhận kết quả tồn đọng', false, NOW());


-- ============================================
-- NOTIFICATION SETTINGS
-- ============================================

INSERT INTO notification_settings (template_code, is_enabled, remind_before_days, is_persistent, remind_interval_hours,
                                   max_reminders, preferred_send_time, escalate_after_days, delete_flag, created_at)
VALUES
-- Plan (gửi 1 lần)
('PLAN_WAITING_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('PLAN_WAITING_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('PLAN_APPROVED', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('PLAN_REJECTED_BY_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('PLAN_REJECTED_BY_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),

-- Result (gửi 1 lần)
('RESULT_WAITING_FI', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('RESULT_WAITING_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('RESULT_WAITING_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('RESULT_APPROVED', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('RESULT_REJECTED_BY_FI', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('RESULT_REJECTED_BY_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('RESULT_REJECTED_BY_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),

-- Issue (gửi 1 lần)
('ISSUE_WAITING_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('ISSUE_WAITING_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('ISSUE_APPROVED', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('ISSUE_REJECTED_BY_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('ISSUE_REJECTED_BY_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),

-- Topic (gửi 1 lần)
('TOPIC_WAITING_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('TOPIC_WAITING_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('TOPIC_APPROVED', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('TOPIC_REJECTED_BY_SV', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),
('TOPIC_REJECTED_BY_MANAGER', true, 0, false, 24, 1, '08:00:00', NULL, false, NOW()),

-- Reminders (scheduled)
('TRAINING_REMINDER_TODAY', true, 0, false, 24, 1, '06:00:00', NULL, false, NOW()),
('TRAINING_REMINDER_UPCOMING', true, 1, false, 24, 1, '06:00:00', NULL, false, NOW()),
('TRAINING_OVERDUE_WARNING', true, 0, true, 24, 5, '08:00:00', 3, false, NOW()),

-- Approval Overdue - Nagging (gửi liên tục đến khi xử lý)
('APPROVAL_OVERDUE_SV', true, 0, true, 24, 999, '08:00:00', NULL, false, NOW()),
('APPROVAL_OVERDUE_MANAGER', true, 0, true, 24, 999, '08:00:00', NULL, false, NOW()),
('APPROVAL_OVERDUE_FI', true, 0, true, 24, 999, '08:00:00', NULL, false, NOW());