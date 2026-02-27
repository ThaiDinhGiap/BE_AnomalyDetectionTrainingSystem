-- ============================================================================
-- ANOMALY TRAINING SYSTEM - SAMPLE DATA
-- Version: 2.0
-- Description: Insert sample data for testing with new core database schema
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- PART 1: USERS & ROLES
-- ============================================================================
-- Password: Password@123 (BCrypt encoded)
INSERT INTO users (id, username, email, password_hash, full_name, role, is_active, created_by)
VALUES
-- Admin
(1, 'admin', 'admin@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO', 'Quản Trị Hệ Thống',
 'ADMIN', TRUE, 'system'),

-- Manager
(2, 'manager01', 'manager01@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Nguyễn Văn Quản Lý', 'MANAGER', TRUE, 'system'),

-- Supervisors
(3, 'supervisor01', 'supervisor01@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Trần Văn Giám Sát', 'SUPERVISOR', TRUE, 'system'),
(4, 'supervisor02', 'supervisor02@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Lê Thị Giám Sát', 'SUPERVISOR', TRUE, 'system'),

-- Team Leaders (Production)
(5, 'tl_prod01', 'tl_prod01@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Phạm Văn Trưởng Ca 1', 'TEAM_LEADER', TRUE, 'system'),
(6, 'tl_prod02', 'tl_prod02@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Hoàng Văn Trưởng Ca 2', 'TEAM_LEADER', TRUE, 'system'),
(7, 'tl_prod03', 'tl_prod03@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Võ Thị Trưởng Ca 3', 'TEAM_LEADER', TRUE, 'system'),

-- Final Inspection
(8, 'tl_fi01', 'tl_fi01@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Đỗ Văn Kiểm Tra 1', 'FINAL_INSPECTION', TRUE, 'system'),
(9, 'tl_fi02', 'tl_fi02@congty.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Ngô Thị Kiểm Tra 2', 'FINAL_INSPECTION', TRUE, 'system');

-- Bổ sung một số Role và Module cơ bản
INSERT INTO roles (id, role_code, display_name, description, is_system, is_active, created_by)
VALUES (1, 'ROLE_ADMIN', 'Quản trị viên', 'Quyền truy cập toàn hệ thống', TRUE, TRUE, 'system'),
       (2, 'ROLE_MANAGER', 'Quản lý xưởng', 'Quyền phê duyệt cấp quản lý', TRUE, TRUE, 'system'),
       (3, 'ROLE_SUPERVISOR', 'Giám sát viên', 'Quyền giám sát, phê duyệt cấp 1', TRUE, TRUE, 'system'),
       (4, 'ROLE_TEAM_LEADER', 'Tổ trưởng sản xuất', 'Khởi tạo báo cáo và nhập liệu', TRUE, TRUE, 'system');

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 3),
       (5, 4),
       (6, 4),
       (7, 4);


-- ============================================================================
-- PART 2: ORGANIZATION STRUCTURE & MASTER DATA
-- ============================================================================

-- Sections (Xưởng)
INSERT INTO sections (id, name, manager_id, created_by)
VALUES (1, 'Xưởng Gia Công Cơ Khí', 2, 'admin'),
       (2, 'Xưởng Lắp Ráp & Đóng Gói', 2, 'admin');

-- Groups (Dây chuyền / Khu vực)
INSERT INTO `groups` (id, section_id, name, supervisor_id, created_by)
VALUES (1, 1, 'Khu Vực Tiện CNC', 3, 'admin'),
       (2, 1, 'Khu Vực Phay CNC', 3, 'admin'),
       (3, 2, 'Khu Vực Lắp Ráp Máy Bơm', 4, 'admin');

-- Product Lines (Dòng sản phẩm - Table mới)
INSERT INTO product_lines (id, group_id, name, created_by)
VALUES (1, 1, 'Dòng Máy Bơm Nước P1', 'admin'),
       (2, 2, 'Dòng Bơm Thủy Lực P2', 'admin'),
       (3, 3, 'Dây Chuyền Lắp Ráp Động Cơ Nổ', 'admin');

-- Teams (Tổ sản xuất)
INSERT INTO teams (id, group_id, name, team_leader_id, created_by)
VALUES (1, 1, 'Tổ Tiện Ca Ngày', 5, 'admin'),
       (2, 2, 'Tổ Phay Ca Ngày', 6, 'admin'),
       (3, 3, 'Tổ Lắp Ráp Cuối', 7, 'admin');

-- Employees (Công nhân)
INSERT INTO employees (id, employee_code, full_name, team_id, status, created_by)
VALUES (1, 'NV001', 'Nguyễn Văn An', 1, 'ACTIVE', 'admin'),
       (2, 'NV002', 'Trần Thị Bình', 1, 'ACTIVE', 'admin'),
       (3, 'NV003', 'Lê Văn Cường', 1, 'ACTIVE', 'admin'),
       (4, 'NV004', 'Võ Thị Dung', 2, 'ACTIVE', 'admin'),
       (5, 'NV005', 'Đặng Văn Em', 2, 'ACTIVE', 'admin'),
       (6, 'NV006', 'Bùi Thị Phượng', 3, 'MATERNITY_LEAVE', 'admin'),
       (7, 'NV007', 'Ngô Văn Giàu', 3, 'ACTIVE', 'admin');

-- Products (Sản phẩm - Table mới)
INSERT INTO products (id, code, name, description, created_by)
VALUES (1, 'BOM-2024-X1', 'Bơm nước dân dụng X1', 'Công suất 1.5HP', 'admin'),
       (2, 'BOM-2024-X2', 'Bơm nước công nghiệp X2', 'Công suất 5.0HP', 'admin'),
       (3, 'MOT-2024-Y1', 'Động cơ xăng Y1', 'Động cơ 4 thì', 'admin');

-- Processes (Công đoạn)
INSERT INTO processes (id, product_line_id, code, name, description, classification, standard_time_jt, created_by)
VALUES (1, 1, 'OP10', 'Gia công thô trục bơm', 'Tiện thô trục bằng máy CNC, dung sai ±0.5mm', 2, 15.00, 'admin'),
       (2, 1, 'OP20', 'Gia công tinh trục bơm', 'Tiện tinh trục chính xác, dung sai ±0.02mm', 1, 20.00, 'admin'),
       (3, 2, 'OP30', 'Phay mặt bích', 'Phay phẳng mặt bích tiếp xúc', 3, 18.00, 'admin'),
       (4, 3, 'AS10', 'Lắp ráp cánh bơm', 'Lắp cánh bơm vào thân buồng bơm', 2, 25.00, 'admin'),
       (5, 3, 'AS20', 'Test áp lực nước', 'Kiểm tra rò rỉ áp suất 10bar', 1, 30.00, 'admin');

-- Product_Process (N:M Sản phẩm & Công đoạn)
INSERT INTO product_process (product_id, process_id, standard_time_jt, created_by)
VALUES (1, 1, 15.00, 'admin'),
       (1, 2, 20.00, 'admin'),
       (2, 3, 18.00, 'admin'),
       (3, 4, 25.00, 'admin'),
       (3, 5, 30.00, 'admin');

-- Employee Skills
INSERT INTO employee_skills (employee_id, process_id, is_qualified, certified_date, expiry_date, created_by)
VALUES (1, 1, TRUE, '2023-01-15', '2026-01-15', 'admin'),
       (1, 2, TRUE, '2023-03-20', '2026-03-20', 'admin'),
       (2, 1, TRUE, '2023-02-10', '2026-02-10', 'admin'),
       (3, 1, TRUE, '2022-06-01', '2025-06-01', 'admin'),
       (4, 3, TRUE, '2023-05-01', '2026-05-01', 'admin'),
       (5, 3, TRUE, '2023-07-01', '2026-07-01', 'admin'),
       (7, 4, TRUE, '2023-04-15', '2026-04-15', 'admin'),
       (7, 5, TRUE, '2023-06-20', '2026-06-20', 'admin');


-- ============================================================================
-- PART 3: DEFECT MANAGEMENT (Lỗi quá khứ & Báo cáo)
-- ============================================================================

-- Defects (Master Data)
INSERT INTO defects (id, defect_description, process_id, detected_date, is_escaped, note, created_by)
VALUES (1, 'Xước bề mặt trục do dao cụ mòn', 2, '2023-09-15', FALSE, 'Phát hiện tại trạm kiểm tra OP20', 'system'),
       (2, 'Lỗi kích thước đường kính ngoài dung sai', 1, '2023-09-20', TRUE, 'Lọt ra đến khâu lắp ráp', 'system'),
       (3, 'Rò rỉ ron cao su khi test áp lực', 5, '2023-10-05', TRUE, 'Lọt ra đến khách hàng', 'system');

-- Defect Proposals (Header)
INSERT INTO defect_proposals (id, product_line_id, status, current_version, form_code, created_by)
VALUES (1, 1, 'APPROVED', 1, 'DEF-2023-001', 'tl_prod01'),
       (2, 3, 'WAITING_SV', 1, 'DEF-2023-002', 'tl_prod03');

-- Defect Proposal Details
INSERT INTO defect_proposal_details (defect_proposal_id, defect_id, proposal_type, defect_description, process_id,
                                     detected_date, is_escaped, note, origin_cause, outflow_cause, cause_point,
                                     created_by)
VALUES (1, 1, 'CREATE', 'Xước bề mặt trục do dao cụ mòn', 2, '2023-09-15', FALSE, 'Đã xử lý dao', 'Dao mẻ',
        'Không soi đèn kỹ', 'Tại đài dao', 'tl_prod01'),
       (1, 2, 'CREATE', 'Lỗi kích thước đường kính ngoài dung sai', 1, '2023-09-20', TRUE, 'Lọt ra khâu lắp ráp',
        'Setup sai thông số', 'Đo sai cách', 'Tại khâu kẹp phôi', 'tl_prod01'),
       (2, 3, 'CREATE', 'Rò rỉ ron cao su khi test áp lực', 5, '2023-10-05', TRUE, 'Cần huấn luyện khẩn',
        'Rách ron khi ép', 'Lực ép tay không đều', 'Trạm ép ron', 'tl_prod03');


-- ============================================================================
-- PART 4: TRAINING SAMPLES (Mẫu huấn luyện)
-- ============================================================================

-- Training Samples (Master Data)
INSERT INTO training_samples (id, process_id, product_line_id, defect_id, category_name, training_description,
                              product_id, sample_code, has_physical_sample, process_order, category_order,
                              content_order, note, created_by)
VALUES (1, 2, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Yêu cầu công nhân soi đèn góc 45 độ để phát hiện vết xước. Thời gian tiêu chuẩn: 20 giây.', 1, 'Mẫu NG #55',
        TRUE, 1, 1, 1, 'Lỗi quan trọng', 'system'),
       (2, 1, 1, 2, 'Lỗi Kích Thước', 'Sử dụng thước kẹp điện tử đo 3 điểm: đầu, giữa, cuối. Ghi nhận vào form.', 1,
        'Mẫu NG #62', TRUE, 1, 2, 1, 'Đã lọt qua trạm', 'system'),
       (3, 5, 3, 3, 'Lắp ráp ron cao su',
        'Sử dụng đồ gá chuẩn, ép lực đều tay tránh rách ron. Kiểm tra bằng mắt trước khi đưa vào test.', 3,
        'Mẫu chuẩn #01', TRUE, 1, 1, 1, 'Lỗi lọt KH', 'system');

-- Training Sample Proposals
INSERT INTO training_sample_proposals (id, product_line_id, status, current_version, form_code, created_by)
VALUES (1, 1, 'APPROVED', 1, 'TSP-2023-001', 'tl_prod01');

INSERT INTO training_sample_proposal_details (training_sample_proposal_id, training_sample_id, proposal_type,
                                              process_id, product_id, defect_id, category_name, training_sample_code,
                                              training_description, note, created_by)
VALUES (1, 1, 'CREATE', 2, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ', 'Mẫu NG #55', 'Yêu cầu soi đèn góc 45 độ...',
        'Ghi chú thêm', 'tl_prod01');


-- ============================================================================
-- PART 5: TRAINING PLAN & RESULTS (Kế hoạch & Kết quả huấn luyện)
-- ============================================================================

-- Training Plans
INSERT INTO training_plans (id, form_code, title, month_start, month_end, team_id, line_id, status, current_version,
                            note, created_by)
VALUES (1, 'TR_PLAN_2023_Q4', 'Kế hoạch huấn luyện Q4/2023 - Line Tiện', '2023-10-01', '2023-12-31', 1, 1, 'APPROVED',
        1, 'Kế hoạch định kỳ', 'tl_prod01');

-- Training Plan Details
INSERT INTO training_plan_details (id, training_plan_id, employee_id, process_id, target_month, planned_date,
                                   actual_date, status, created_by)
VALUES (1, 1, 1, 1, '2023-10-01', '2023-10-05', '2023-10-12', 'DONE', 'tl_prod01'),
       (2, 1, 1, 2, '2023-10-01', '2023-10-10', '2023-10-12', 'DONE', 'tl_prod01'),
       (3, 1, 2, 1, '2023-10-01', '2023-10-12', '2023-10-15', 'DONE', 'tl_prod01');

-- Training Results
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES (1, 1, 'Kết quả huấn luyện năm 2023 - Line Tiện', 'TR_RESULT_2023', 2023, 1, 1, 'APPROVED', 1,
        'Đã được phê duyệt', 'tl_prod01');

-- Training Result Details
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, process_id,
                                     training_sample_id, product_id, classification, cycle_time_standard, planned_date,
                                     actual_date, time_in, time_start_op, time_out, status, detection_time, is_pass,
                                     note, is_retrained, signature_pro_in, signature_fi_in, signature_pro_out,
                                     signature_fi_out, created_by)
VALUES (1, 1, 1, 1, 2, 1, 2, 15.00, '2023-10-05', '2023-10-12', '08:00:00', '08:05:00', '08:10:00', 'APPROVED', 12,
        TRUE, 'Thao tác tốt', FALSE, 5, 8, 5, 8, 'tl_prod01'),
       (2, 2, 1, 2, 1, 1, 1, 20.00, '2023-10-10', '2023-10-12', '09:00:00', '09:02:00', '09:15:00', 'APPROVED', 18,
        TRUE, 'Soi đèn đúng góc độ', FALSE, 5, 8, 5, 8, 'tl_prod01');


-- ============================================================================
-- PART 6: NOTIFICATIONS & SYSTEM CONFIGS
-- ============================================================================

-- Notification Templates
INSERT INTO notification_templates (code, subject_template, body_template, description, created_by)
VALUES ('DEFECT_WAITING_SV', '[Hệ Thống Đào Tạo] Báo cáo lỗi cần xem xét', 'email/defect-waiting-approval',
        'TL gửi báo cáo lỗi, thông báo SV', 'admin'),
       ('DEFECT_WAITING_MANAGER', '[Hệ Thống Đào Tạo] Báo cáo lỗi cần phê duyệt', 'email/defect-waiting-approval',
        'SV duyệt xong, thông báo Manager', 'admin'),
       ('PLAN_WAITING_SV', '[Hệ Thống Đào Tạo] Kế hoạch cần phê duyệt: $${formCode}', 'email/plan-waiting-approval',
        'TL gửi kế hoạch, thông báo SV', 'admin');

-- Notification Settings
INSERT INTO notification_settings (template_code, is_enabled, remind_before_days, is_persistent, remind_interval_hours,
                                   max_reminders, preferred_send_time, created_by)
VALUES ('DEFECT_WAITING_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', 'admin'),
       ('DEFECT_WAITING_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', 'admin'),
       ('PLAN_WAITING_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', 'admin');


-- ============================================================================
-- PART 7: REJECTION & APPROVAL SUPPORT
-- ============================================================================

-- Reject Reasons
INSERT INTO reject_reasons (category_name, reason_name, created_by)
VALUES ('Dữ liệu', 'Thiếu thông tin mô tả lỗi chi tiết', 'admin'),
       ('Quy trình', 'Sai phân loại công đoạn đánh giá', 'admin'),
       ('Nội dung', 'Mẫu vật lý không đạt tiêu chuẩn', 'admin');

-- Required Actions
INSERT INTO required_actions (action_name, created_by)
VALUES ('Vui lòng bổ sung thêm thông tin', 'admin'),
       ('Yêu cầu làm lại mẫu NG mới', 'admin'),
       ('Trình bày lại báo cáo sự cố', 'admin');


-- ============================================================================
-- PART 8: ANNUAL REVIEW CONFIG
-- ============================================================================

INSERT INTO training_sample_review_configs (product_line_id, trigger_month, trigger_day, due_days, assignee_id,
                                            is_active, created_by)
VALUES (1, 3, 1, 30, 5, TRUE, 'admin'), -- Review định kỳ vào tháng 3 cho Line Tiện
       (2, 3, 1, 30, 6, TRUE, 'admin');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- END OF SAMPLE DATA
-- ============================================================================