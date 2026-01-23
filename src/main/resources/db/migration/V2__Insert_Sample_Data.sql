/* =========================================================================
   SAMPLE DATA SCRIPT FOR ANOMALY TRAINING SYSTEM (Schema V2)
   - Covers: Users, Org, Issues, Training (Topic/Plan/Result), Notifications
   - Includes: Audit fields (created_by, updated_by)
   ========================================================================= */

SET FOREIGN_KEY_CHECKS = 0;

-- 1. RESET DATA (Optional - Comment out if you want to keep existing data)
TRUNCATE TABLE training_result_detail_history;
TRUNCATE TABLE training_result_detail;
TRUNCATE TABLE training_result_history;
TRUNCATE TABLE training_result;
TRUNCATE TABLE training_plan_detail_history;
TRUNCATE TABLE training_plan_detail;
TRUNCATE TABLE training_plan_history;
TRUNCATE TABLE training_plan;
TRUNCATE TABLE defect_training_content;
TRUNCATE TABLE training_topic_detail_history;
TRUNCATE TABLE training_topic_detail;
TRUNCATE TABLE training_topics_history;
TRUNCATE TABLE training_topics;
TRUNCATE TABLE process_defects;
TRUNCATE TABLE issue_detail_history;
TRUNCATE TABLE issue_detail;
TRUNCATE TABLE issue_report_history;
TRUNCATE TABLE issue_report;
TRUNCATE TABLE process_qualifications;
TRUNCATE TABLE employees;
TRUNCATE TABLE teams;
TRUNCATE TABLE product_group;
TRUNCATE TABLE processes;
TRUNCATE TABLE `groups`;
TRUNCATE TABLE sections;
TRUNCATE TABLE notification_queue;
TRUNCATE TABLE notification_settings;
TRUNCATE TABLE notification_templates;
TRUNCATE TABLE users;

-- ============================================
-- 2. USERS (Full Roles)
-- ============================================
INSERT INTO users (id, username, email, password_hash, full_name, role, is_active, created_by) VALUES
                                                                                                   (1, 'manager', 'manager@sep490.com', '{bcrypt}$2a$10$X...', 'Nguyen Van Manager', 'MANAGER', TRUE, 'system'),
                                                                                                   (2, 'supervisor', 'sup@sep490.com', '{bcrypt}$2a$10$X...', 'Tran Van Supervisor', 'SUPERVISOR', TRUE, 'system'),
                                                                                                   (3, 'tl_prod', 'tl_prod@sep490.com', '{bcrypt}$2a$10$X...', 'Le Van TL Product', 'TEAM_LEADER', TRUE, 'system'),
                                                                                                   (4, 'tl_fi', 'tl_fi@sep490.com', '{bcrypt}$2a$10$X...', 'Pham Thi TL FI', 'FINAL_INSPECTION', TRUE, 'system'),
                                                                                                   (5, 'tl_prod_2', 'tl_prod2@sep490.com', '{bcrypt}$2a$10$X...', 'Hoang Van TL 2', 'TEAM_LEADER', TRUE, 'system');

-- ============================================
-- 3. ORGANIZATION (Section -> Group -> Team)
-- ============================================
-- Sections
INSERT INTO sections (id, name, manager_id, created_by) VALUES
    (1, 'Xưởng Gia Công Valve', 1, 'manager');

-- Groups (Lines)
INSERT INTO `groups` (id, section_id, name, supervisor_id, created_by) VALUES
                                                                           (1, 1, 'Line Valve 01', 2, 'manager'),
                                                                           (2, 1, 'Line Valve 02', 2, 'manager');

-- Teams
INSERT INTO teams (id, group_id, name, team_leader_id, created_by) VALUES
                                                                       (1, 1, 'Team Valve A (Ca 1)', 3, 'supervisor'),
                                                                       (2, 1, 'Team Valve B (Ca 2)', 5, 'supervisor');

-- Processes (Công đoạn)
INSERT INTO processes (id, group_id, code, name, classification, standard_time_jt, created_by) VALUES
                                                                                                   (1, 1, 'OP10', 'Gia công thô', 2, 15.5, 'admin'),
                                                                                                   (2, 1, 'OP20', 'Gia công tinh', 3, 20.0, 'admin'),
                                                                                                   (3, 1, 'FI', 'Kiểm tra ngoại quan', 1, 10.0, 'admin'); -- Classification 1 = Quan trọng/Lỗi quá khứ

-- Product Groups
INSERT INTO product_group (id, group_id, product_code, created_by) VALUES
                                                                       (1, 1, 'VALVE-2024-X', 'admin'),
                                                                       (2, 1, 'VALVE-2024-Y', 'admin');

-- Employees
INSERT INTO employees (id, employee_code, full_name, team_id, status, created_by) VALUES
                                                                                      (1, 'V001', 'Cong Nhan A', 1, 'ACTIVE', 'tl_prod'),
                                                                                      (2, 'V002', 'Cong Nhan B', 1, 'ACTIVE', 'tl_prod'),
                                                                                      (3, 'V003', 'Cong Nhan C', 2, 'MATERNITY_LEAVE', 'tl_prod_2');

-- Process Qualifications (Ai làm được việc gì)
INSERT INTO process_qualifications (employee_id, process_id, is_qualified, certified_date, created_by) VALUES
                                                                                                           (1, 1, TRUE, '2023-01-01', 'tl_prod'), -- A làm OP10
                                                                                                           (1, 2, TRUE, '2023-01-01', 'tl_prod'), -- A làm OP20
                                                                                                           (2, 3, TRUE, '2023-06-01', 'tl_prod'); -- B làm FI

-- ============================================
-- 4. ISSUE REPORTING (Lỗi -> Process Defects)
-- ============================================
-- Case 1: Lỗi đã được duyệt (Approved) -> Tạo ra Process Defect
INSERT INTO issue_report (id, verified_by_sv, approved_by_manager, status, created_by) VALUES
    (1, 2, 1, 'APPROVED', 'tl_prod');

INSERT INTO issue_detail (id, issue_report_id, defect_description, process_id, detected_date, is_escaped, note, created_by) VALUES
    (1, 1, 'Xước bề mặt trong tại OP10', 1, '2023-10-01', FALSE, 'Phát hiện tại công đoạn sau', 'tl_prod');

-- Mapping sang Process Defect (Lỗi quá khứ chính thức)
INSERT INTO process_defects (id, issue_detail_id, defect_description, process_id, detected_date, is_escaped, created_by) VALUES
    (1, 1, 'Xước bề mặt trong tại OP10', 1, '2023-10-01', FALSE, 'system');

-- Case 2: Lỗi bị từ chối bởi SV (Rejected) -> Có History
INSERT INTO issue_report (id, status, rejected_by, reject_level, reject_reason, current_version, created_by) VALUES
    (2, 'REJECTED_BY_SV', 2, 'SUPERVISOR', 'Mô tả chưa rõ ràng', 2, 'tl_prod');

INSERT INTO issue_report_history (issue_report_id, version, rejected_by, reject_reason, created_by) VALUES
    (2, 1, 2, 'Mô tả chưa rõ ràng', 'system');

-- ============================================
-- 5. TRAINING TOPICS (Mẫu huấn luyện)
-- ============================================
-- Topic gắn với Process Defect ở trên
INSERT INTO training_topics (id, title, verified_by_sv, approved_by_manager, status, created_by) VALUES
    (1, 'Huấn luyện nhận diện lỗi Xước OP10', 2, 1, 'APPROVED', 'tl_prod');

INSERT INTO training_topic_detail (id, topic_id, process_defect_id, category_name, training_sample, training_detail, created_by) VALUES
    (1, 1, 1, 'Lỗi Ngoại Quan', 'Mẫu NG số #55', 'Yêu cầu công nhân soi đèn góc 45 độ để phát hiện vết xước.', 'tl_prod');

-- Bảng Defect Training Content (Link chi tiết)
INSERT INTO defect_training_content (training_topic_detail_id, process_defect_id, category_name, training_sample, training_detail, created_by) VALUES
    (1, 1, 'Lỗi Ngoại Quan', 'Mẫu NG số #55', 'Yêu cầu công nhân soi đèn góc 45 độ...', 'tl_prod');

-- ============================================
-- 6. TRAINING PLAN (Kế hoạch)
-- ============================================
INSERT INTO training_plan (id, title, month_start, month_end, group_id, verified_by_sv, approved_by_manager, status, created_by) VALUES
    (1, 'Kế hoạch Đào tạo Tháng 11/2023', '2023-11-01', '2023-11-30', 1, 2, 1, 'APPROVED', 'tl_prod');

-- Chi tiết kế hoạch (Ai, làm gì, ngày nào)
INSERT INTO training_plan_detail (id, training_plan_id, employee_id, process_id, target_month, planned_date, result_status, created_by) VALUES
                                                                                                                                            (1, 1, 1, 1, '2023-11-01', '2023-11-10', 'DONE', 'tl_prod'), -- Đã làm
                                                                                                                                            (2, 1, 2, 3, '2023-11-01', '2023-11-15', 'PENDING', 'tl_prod'); -- Chưa làm

-- ============================================
-- 7. TRAINING RESULT (Kết quả thực tế)
-- ============================================
-- Kết quả cho Detail ID 1 (Đã làm, Pass)
INSERT INTO training_result (id, title, year, group_id, confirm_by_fi, verified_by_sv, approved_by_manager, status, created_by) VALUES
    (1, 'Báo cáo kết quả đợt 1', 2023, 1, 4, 2, 1, 'APPROVED_BY_MANAGER', 'tl_prod');

INSERT INTO training_result_detail (
    training_result_id, training_plan_detail_id, actual_date, product_group_id, training_topic_id,
    time_in, time_out, detection_time, is_pass, status,
    signature_pro_in, signature_pro_out, signature_fi_in, signature_fi_out, created_by
) VALUES (
             1, 1, '2023-11-10', 1, 1,
             '08:00:00', '08:15:00', 15, TRUE, 'APPROVED_BY_MANAGER',
             3, 3, 4, 4, 'tl_prod'
         );

-- ============================================
-- 8. NOTIFICATIONS (Cấu hình & Queue)
-- ============================================
-- Templates
INSERT INTO notification_templates (code, subject_template, body_template, description, created_by) VALUES
                                                                                                        ('PLAN_REMINDER', 'Nhắc nhở lịch: {{date}}', 'Chào {{name}}, có lịch huấn luyện.', 'Nhắc lịch', 'admin'),
                                                                                                        ('APPROVAL_REQ', 'Yêu cầu duyệt: {{title}}', 'Kính gửi {{role}}, vui lòng duyệt.', 'Duyệt', 'admin');

-- Settings
INSERT INTO notification_settings (template_code, is_enabled, remind_before_days, is_persistent, created_by) VALUES
                                                                                                                 ('PLAN_REMINDER', TRUE, 1, FALSE, 'admin'),
                                                                                                                 ('APPROVAL_REQ', TRUE, 0, TRUE, 'admin');

-- Queue (Ví dụ mail đang chờ gửi)
INSERT INTO notification_queue (
    recipient_user_id, notification_type, related_entity_id, related_entity_table,
    subject, body, status, scheduled_at, created_by
) VALUES
    (2, 'APPROVAL_REQ', 1, 'training_plan', 'Yêu cầu duyệt: Kế hoạch Tháng 11', 'Kính gửi Supervisor...', 'PENDING', NOW(), 'system');

SET FOREIGN_KEY_CHECKS = 1;