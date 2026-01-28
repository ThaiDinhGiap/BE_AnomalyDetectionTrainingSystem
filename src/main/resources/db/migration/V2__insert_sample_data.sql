-- ============================================================================
-- ANOMALY TRAINING SYSTEM - SAMPLE DATA
-- Version: 1.0
-- Description: Insert sample data for testing
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- PART 1: USERS
-- ============================================================================
-- Password: Password@123 (BCrypt encoded)
INSERT INTO users (id, username, email, password_hash, full_name, role, is_active, created_by)
VALUES
-- Admin
(1, 'admin', 'admin@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'System Administrator', 'ADMIN', TRUE, 'system'),

-- Manager
(2, 'manager01', 'manager01@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Nguyễn Văn Manager', 'MANAGER', TRUE, 'system'),

-- Supervisors
(3, 'supervisor01', 'supervisor01@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Trần Văn Supervisor', 'SUPERVISOR', TRUE, 'system'),
(4, 'supervisor02', 'supervisor02@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Lê Thị Supervisor', 'SUPERVISOR', TRUE, 'system'),

-- Team Leaders (Production)
(5, 'tl_prod01', 'tl_prod01@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Phạm Văn TL Sản Xuất 1', 'TEAM_LEADER', TRUE, 'system'),
(6, 'tl_prod02', 'tl_prod02@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Hoàng Văn TL Sản Xuất 2', 'TEAM_LEADER', TRUE, 'system'),
(7, 'tl_prod03', 'tl_prod03@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Võ Thị TL Sản Xuất 3', 'TEAM_LEADER', TRUE, 'system'),

-- Final Inspection
(8, 'tl_fi01', 'tl_fi01@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Đỗ Văn TL Kiểm Tra 1', 'FINAL_INSPECTION', TRUE, 'system'),
(9, 'tl_fi02', 'tl_fi02@training.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Ngô Thị TL Kiểm Tra 2', 'FINAL_INSPECTION', TRUE, 'system');


-- ============================================================================
-- PART 2: ORGANIZATION STRUCTURE
-- ============================================================================

-- Sections (Xưởng)
INSERT INTO sections (id, name, manager_id, created_by)
VALUES (1, 'Xưởng Gia Công Valve', 2, 'admin'),
       (2, 'Xưởng Lắp Ráp', 2, 'admin');

-- Groups (Dây chuyền)
INSERT INTO `groups` (id, section_id, name, supervisor_id, created_by)
VALUES (1, 1, 'Line Valve 01 - Ca Sáng', 3, 'admin'),
       (2, 1, 'Line Valve 02 - Ca Chiều', 3, 'admin'),
       (3, 1, 'Line Valve 03 - Ca Đêm', 4, 'admin'),
       (4, 2, 'Line Assembly 01', 4, 'admin');

-- Teams (Tổ sản xuất)
INSERT INTO teams (id, group_id, name, team_leader_id, created_by)
VALUES (1, 1, 'Tổ Valve A1', 5, 'supervisor01'),
       (2, 1, 'Tổ Valve A2', 6, 'supervisor01'),
       (3, 2, 'Tổ Valve B1', 7, 'supervisor01'),
       (4, 4, 'Tổ Assembly C1', 5, 'supervisor02');

-- Processes (Công đoạn)
INSERT INTO processes (id, group_id, code, name, description, classification, standard_time_jt, created_by)
VALUES
-- Line Valve 01 (classification: 1,2,3=Quan trọng cần FI, 4=Thường)
(1, 1, 'OP10', 'Gia công thô đường kính trong', 'Khoan lỗ sơ bộ, dung sai ±0.5mm', 2, 15.00, 'admin'),
(2, 1, 'OP20', 'Gia công tinh đường kính trong', 'Doa lỗ chính xác, dung sai ±0.02mm', 1, 20.00, 'admin'),
(3, 1, 'OP30', 'Gia công mặt ngoài', 'Tiện mặt ngoài theo bản vẽ', 3, 18.00, 'admin'),
(4, 1, 'OP40', 'Mài bóng', 'Mài bóng bề mặt Ra 0.8', 4, 12.00, 'admin'),
(5, 1, 'FI', 'Kiểm tra ngoại quan', 'Kiểm tra 100% sản phẩm', 1, 10.00, 'admin'),

-- Line Valve 02
(6, 2, 'OP10', 'Cắt phôi', 'Cắt phôi theo kích thước', 4, 8.00, 'admin'),
(7, 2, 'OP20', 'Dập định hình', 'Dập tạo hình cơ bản', 2, 15.00, 'admin'),
(8, 2, 'OP30', 'Xử lý nhiệt', 'Tôi + Ram theo quy trình', 1, 45.00, 'admin'),

-- Line Assembly
(9, 4, 'AS10', 'Lắp ráp cụm van', 'Lắp các chi tiết vào thân van', 2, 25.00, 'admin'),
(10, 4, 'AS20', 'Test áp suất', 'Kiểm tra rò rỉ áp suất 10bar', 1, 30.00, 'admin');

-- Product Groups
INSERT INTO product_groups (id, group_id, product_code, created_by)
VALUES (1, 1, 'VALVE-2024-X1', 'admin'),
       (2, 1, 'VALVE-2024-X2', 'admin'),
       (3, 1, 'VALVE-2024-Y1', 'admin'),
       (4, 2, 'VALVE-2024-Z1', 'admin'),
       (5, 4, 'ASSY-2024-A1', 'admin');


-- ============================================================================
-- PART 3: EMPLOYEES
-- ============================================================================

INSERT INTO employees (id, employee_code, full_name, team_id, status, created_by)
VALUES
-- Tổ Valve A1 (Team 1)
(1, 'NV001', 'Nguyễn Văn An', 1, 'ACTIVE', 'tl_prod01'),
(2, 'NV002', 'Trần Thị Bình', 1, 'ACTIVE', 'tl_prod01'),
(3, 'NV003', 'Lê Văn Cường', 1, 'ACTIVE', 'tl_prod01'),
(4, 'NV004', 'Phạm Thị Dung', 1, 'MATERNITY_LEAVE', 'tl_prod01'),
(5, 'NV005', 'Hoàng Văn Em', 1, 'ACTIVE', 'tl_prod01'),

-- Tổ Valve A2 (Team 2)
(6, 'NV006', 'Võ Thị Phương', 2, 'ACTIVE', 'tl_prod02'),
(7, 'NV007', 'Đặng Văn Giang', 2, 'ACTIVE', 'tl_prod02'),
(8, 'NV008', 'Bùi Thị Hoa', 2, 'ACTIVE', 'tl_prod02'),
(9, 'NV009', 'Ngô Văn Ích', 2, 'RESIGNED', 'tl_prod02'),

-- Tổ Valve B1 (Team 3)
(10, 'NV010', 'Dương Thị Kim', 3, 'ACTIVE', 'tl_prod03'),
(11, 'NV011', 'Lý Văn Long', 3, 'ACTIVE', 'tl_prod03'),
(12, 'NV012', 'Trương Thị Mai', 3, 'ACTIVE', 'tl_prod03'),

-- Tổ Assembly C1 (Team 4)
(13, 'NV013', 'Hồ Văn Nam', 4, 'ACTIVE', 'tl_prod01'),
(14, 'NV014', 'Châu Thị Oanh', 4, 'ACTIVE', 'tl_prod01'),
(15, 'NV015', 'Đinh Văn Phúc', 4, 'ACTIVE', 'tl_prod01');


-- ============================================================================
-- PART 4: EMPLOYEE SKILLS
-- ============================================================================

INSERT INTO employee_skills (employee_id, process_id, is_qualified, certified_date, created_by)
VALUES
-- NV001: OP10, OP20
(1, 1, TRUE, '2023-01-15', 'tl_prod01'),
(1, 2, TRUE, '2023-03-20', 'tl_prod01'),

-- NV002: OP20, OP30, FI
(2, 2, TRUE, '2023-02-10', 'tl_prod01'),
(2, 3, TRUE, '2023-02-10', 'tl_prod01'),
(2, 5, TRUE, '2023-06-01', 'tl_prod01'),

-- NV003: Tất cả OP Line 1
(3, 1, TRUE, '2022-06-01', 'tl_prod01'),
(3, 2, TRUE, '2022-06-01', 'tl_prod01'),
(3, 3, TRUE, '2022-08-15', 'tl_prod01'),
(3, 4, TRUE, '2022-10-01', 'tl_prod01'),
(3, 5, TRUE, '2023-01-01', 'tl_prod01'),

-- NV005: Mới, chỉ OP10
(5, 1, TRUE, '2024-01-10', 'tl_prod01'),

-- NV006, NV007
(6, 1, TRUE, '2023-05-01', 'tl_prod02'),
(6, 2, TRUE, '2023-07-01', 'tl_prod02'),
(7, 3, TRUE, '2023-04-15', 'tl_prod02'),
(7, 4, TRUE, '2023-06-20', 'tl_prod02'),

-- NV010, NV011: Line 02
(10, 6, TRUE, '2023-03-01', 'tl_prod03'),
(10, 7, TRUE, '2023-05-01', 'tl_prod03'),
(11, 7, TRUE, '2023-04-01', 'tl_prod03'),
(11, 8, TRUE, '2023-08-01', 'tl_prod03'),

-- NV013, NV014: Assembly
(13, 9, TRUE, '2023-06-01', 'tl_prod01'),
(13, 10, TRUE, '2023-09-01', 'tl_prod01'),
(14, 9, TRUE, '2023-07-01', 'tl_prod01');


-- ============================================================================
-- PART 5: DEFECTS (Master Data - Lỗi đã được duyệt)
-- ============================================================================

INSERT INTO defects (id, defect_description, process_id, detected_date, is_escaped, note, created_by)
VALUES (1, 'Xước bề mặt trong do dao mòn', 2, '2023-09-15', FALSE, 'Phát hiện tại OP30', 'system'),
       (2, 'Lỗi kích thước ngoài dung sai do setup sai', 3, '2023-09-20', TRUE,
        'Lọt ra đến khách hàng - Cần huấn luyện đặc biệt', 'system'),
       (3, 'Vết nứt tế vi sau xử lý nhiệt', 8, '2023-09-25', FALSE, 'Phát hiện khi kiểm tra siêu âm', 'system'),
       (4, 'Lỗi lắp ngược chi tiết', 9, '2023-10-01', FALSE, 'Phát hiện tại công đoạn test', 'system'),
       (5, 'Rò rỉ áp suất tại mối ghép', 10, '2023-10-05', TRUE, 'Lọt ra khách hàng', 'system');


-- ============================================================================
-- PART 6: DEFECT REPORT (Báo cáo lỗi)
-- ============================================================================

-- Report 1: Đã APPROVED
INSERT INTO defect_report (id, group_id, status, current_version, created_by, created_at)
VALUES (1, 1, 'APPROVED', 1, 'tl_prod01', '2023-10-01 08:00:00');

INSERT INTO defect_report_detail (id, defect_report_id, defect_id, report_type, defect_description, process_id,
                                  detected_date, is_escaped, note, created_by)
VALUES (1, 1, 1, 'CREATE', 'Xước bề mặt trong do dao mòn', 2, '2023-09-15', FALSE, 'Phát hiện tại OP30', 'tl_prod01'),
       (2, 1, 2, 'CREATE', 'Lỗi kích thước ngoài dung sai do setup sai', 3, '2023-09-20', TRUE, 'Lọt ra đến khách hàng',
        'tl_prod01');

-- Report 2: DRAFT (đang soạn)
INSERT INTO defect_report (id, group_id, status, current_version, created_by, created_at)
VALUES (2, 1, 'DRAFT', 1, 'tl_prod01', '2024-01-15 09:00:00');

INSERT INTO defect_report_detail (id, defect_report_id, defect_id, report_type, defect_description, process_id,
                                  detected_date, is_escaped, note, created_by)
VALUES (3, 2, NULL, 'CREATE', 'Lỗi mới phát hiện - Bavia mặt trong', 2, '2024-01-14', FALSE, 'Cần xác nhận thêm',
        'tl_prod01');

-- Report 3: WAITING_SV
INSERT INTO defect_report (id, group_id, status, current_version, created_by, created_at)
VALUES (3, 2, 'WAITING_SV', 1, 'tl_prod03', '2024-01-16 10:00:00');

INSERT INTO defect_report_detail (id, defect_report_id, defect_id, report_type, defect_description, process_id,
                                  detected_date, is_escaped, note, created_by)
VALUES (4, 3, 3, 'UPDATE', 'Vết nứt tế vi sau xử lý nhiệt - Cập nhật mô tả chi tiết hơn', 8, '2023-09-25', FALSE,
        'Bổ sung hình ảnh minh họa', 'tl_prod03');


-- ============================================================================
-- PART 7: TRAINING TOPICS (Master Data - Mẫu huấn luyện đã duyệt)
-- ============================================================================

INSERT INTO training_topics (id, process_id, defect_id, category_name, training_sample, training_detail, note,
                             created_by)
VALUES (1, 2, 1, 'Lỗi Ngoại Quan - Xước', 'Mẫu NG #55',
        'Yêu cầu công nhân soi đèn góc 45 độ để phát hiện vết xước. Thời gian tiêu chuẩn: 20 giây.',
        'Lỗi rank 1 - Quan trọng', 'system'),
       (2, 3, 2, 'Lỗi Kích Thước', 'Mẫu NG #62',
        'Sử dụng thước kẹp điện tử, đo 3 điểm: đầu, giữa, cuối. Ghi nhận vào form kiểm tra.', 'Lỗi đã lọt khách hàng',
        'system'),
       (3, 5, NULL, 'Kiểm tra tổng quát', 'Bộ mẫu chuẩn #01', 'Quy trình kiểm tra 100% theo checklist 15 điểm.', NULL,
        'system'),
       (4, 8, 3, 'Kiểm tra nứt sau nhiệt luyện', 'Mẫu NG #78',
        'Sử dụng thiết bị siêu âm, quét toàn bộ bề mặt. Thời gian: 30 giây/sản phẩm.', 'Quan trọng - Classification 1',
        'system'),
       (5, 9, 4, 'Kiểm tra hướng lắp ráp', 'Mẫu NG #85',
        'Đối chiếu với hình ảnh chuẩn trước khi lắp. Kiểm tra chiều mũi tên.', NULL, 'system');


-- ============================================================================
-- PART 8: TRAINING TOPIC REPORT (Báo cáo mẫu huấn luyện)
-- ============================================================================

-- Report 1: APPROVED
INSERT INTO training_topic_report (id, group_id, status, current_version, created_by, created_at)
VALUES (1, 1, 'APPROVED', 1, 'tl_prod01', '2023-10-15 09:00:00');

INSERT INTO training_topic_report_detail (id, training_topic_report_id, training_topic_id, report_type, process_id,
                                          defect_id, category_name, training_sample, training_detail, note, created_by)
VALUES (1, 1, 1, 'CREATE', 2, 1, 'Lỗi Ngoại Quan - Xước', 'Mẫu NG #55',
        'Yêu cầu công nhân soi đèn góc 45 độ để phát hiện vết xước.', 'Lỗi rank 1', 'tl_prod01'),
       (2, 1, 2, 'CREATE', 3, 2, 'Lỗi Kích Thước', 'Mẫu NG #62', 'Sử dụng thước kẹp điện tử, đo 3 điểm.',
        'Lỗi đã lọt KH', 'tl_prod01'),
       (3, 1, 3, 'CREATE', 5, NULL, 'Kiểm tra tổng quát', 'Bộ mẫu chuẩn #01',
        'Quy trình kiểm tra 100% theo checklist 15 điểm.', NULL, 'tl_prod01');

-- Report 2: DRAFT
INSERT INTO training_topic_report (id, group_id, status, current_version, created_by, created_at)
VALUES (2, 1, 'DRAFT', 1, 'tl_prod01', '2024-01-18 14:00:00');

INSERT INTO training_topic_report_detail (id, training_topic_report_id, training_topic_id, report_type, process_id,
                                          defect_id, category_name, training_sample, training_detail, note, created_by)
VALUES (4, 2, NULL, 'CREATE', 4, NULL, 'Kiểm tra độ bóng mài', 'Mẫu chuẩn Ra 0.8',
        'Sử dụng máy đo độ nhám, đo 5 điểm ngẫu nhiên.', 'Đang soạn', 'tl_prod01'),
       (5, 2, 1, 'UPDATE', 2, 1, 'Lỗi Ngoại Quan - Xước (Cập nhật)', 'Mẫu NG #55 + #56',
        'Bổ sung thêm mẫu NG mới và cập nhật quy trình soi đèn.', 'Cập nhật 01/2024', 'tl_prod01');


-- ============================================================================
-- PART 9: TRAINING PLAN (Kế hoạch huấn luyện)
-- ============================================================================

-- Plan 1: APPROVED (Q4/2023)
INSERT INTO training_plan (id,title, form_code, month_start, month_end, group_id, status, current_version, note, created_by,
                           created_at)
VALUES (1, 'Kế hoạch huấn luyện Q4/2023 - Line Valve 01','TR_PLAN_2023_Q4', '2023-10-01', '2023-12-31', 1, 'APPROVED', 1,
        'Kế hoạch huấn luyện Q4/2023 - Line Valve 01', 'tl_prod01', '2023-09-25 10:00:00');

INSERT INTO training_plan_detail (id, training_plan_id, employee_id, process_id, target_month, planned_date,actual_date, status,
                                  note, created_by)
VALUES
-- Tháng 10
(1, 1, 1, 1, '2023-10-01', '2023-10-05', '2023-10-12',  'DONE', NULL, 'tl_prod01'),
(2, 1, 1, 2, '2023-10-01', '2023-10-10','2023-10-12', 'DONE', NULL, 'tl_prod01'),
(3, 1, 2, 2, '2023-10-01', '2023-10-12','2023-10-15', 'DONE', NULL, 'tl_prod01'),
(4, 1, 5, 1, '2023-10-01', '2023-10-15', '2023-10-18', 'DONE', NULL, 'tl_prod01'),
-- Tháng 11
(5, 1, 3, 3, '2023-11-01', '2023-11-05','2023-11-05', 'DONE', NULL, 'tl_prod01'),
(6, 1, 3, 4, '2023-11-01', '2023-11-10', '2023-11-12','DONE', NULL, 'tl_prod01'),
(7, 1, 6, 1, '2023-11-01', '2023-11-08', '2023-12-05', 'MISSED', 'Nghỉ ốm', 'tl_prod01'),
-- Tháng 12
(8, 1, 2, 5, '2023-12-01', '2023-12-05', '2023-12-10', 'DONE', NULL, 'tl_prod01'),
(9, 1, 7, 3, '2023-12-01', '2023-12-10', '2023-12-11', 'DONE', NULL, 'tl_prod01');

-- Plan 2: WAITING_SV (Q1/2024)
INSERT INTO training_plan (id,title, form_code, month_start, month_end, group_id, status, current_version, note, created_by,
                           created_at)
VALUES (2,'Kế hoạch huấn luyện Q1/2024 - Line Valve 01', 'TR_PLAN_2024_Q1', '2024-01-01', '2024-03-31', 1, 'WAITING_SV', 1,
        'Kế hoạch huấn luyện Q1/2024 - Line Valve 01', 'tl_prod01', '2023-12-20 14:00:00');

INSERT INTO training_plan_detail (id, training_plan_id, employee_id, process_id, target_month, planned_date,actual_date, status,
                                  note, created_by)
VALUES
-- Tháng 1/2024
(10, 2, 5, 2, '2024-01-01', '2024-01-08','2024-01-15', 'PENDING', NULL, 'tl_prod01'),
(11, 2, 5, 3, '2024-01-01', '2024-01-15', '2024-01-20','PENDING', NULL, 'tl_prod01'),
(12, 2, 1, 3, '2024-01-01', '2024-01-20', '2024-01-22',  'PENDING', NULL, 'tl_prod01'),
-- Tháng 2/2024
(13, 2, 6, 2, '2024-02-01', '2024-02-05', '2024-02-12','PENDING', NULL, 'tl_prod01'),
(14, 2, 7, 4, '2024-02-01', '2024-02-12', '2024-02-15','PENDING', NULL, 'tl_prod01'),
-- Tháng 3/2024
(15, 2, 8, 1, '2024-03-01', '2024-03-05', '2024-03-25','PENDING', NULL, 'tl_prod01'),
(16, 2, 8, 2, '2024-03-01', '2024-03-15', '2024-03-28','PENDING', NULL, 'tl_prod01');

-- Plan 3: DRAFT
INSERT INTO training_plan (id,title, form_code, month_start, month_end, group_id, status, current_version, note, created_by,
                           created_at)
VALUES (3, 'Kế hoạch Q1/2024 - Line Valve 02 (Đang soạn)','TR_PLAN_2024_Q1_L2', '2024-01-01', '2024-03-31', 2, 'DRAFT', 1,
        'Kế hoạch Q1/2024 - Line Valve 02 (Đang soạn)', 'tl_prod03', '2024-01-05 09:00:00');

INSERT INTO training_plan_detail (id, training_plan_id, employee_id, process_id, target_month, planned_date ,actual_date , status,
                                  note, created_by)
VALUES (17, 3, 10, 6, '2024-01-01', '2024-01-10', '2024-01-20', 'PENDING', NULL, 'tl_prod03'),
       (18, 3, 10, 7, '2024-01-01', '2024-01-20','2024-01-22',  'PENDING', NULL, 'tl_prod03'),
       (19, 3, 11, 8, '2024-02-01', '2024-02-15','2024-02-18',  'PENDING', NULL, 'tl_prod03');


-- ============================================================================
-- PART 10: TRAINING RESULT (Kết quả huấn luyện)
-- ============================================================================

-- Result 1: APPROVED_BY_MANAGER (2023)
INSERT INTO training_result (id,title, form_code, year, group_id, status, current_version, note, created_by, created_at)
VALUES (1, 'Kết quả huấn luyện năm 2023 - Line Valve 01','TR_RESULT_2023', 2023, 1, 'APPROVED_BY_MANAGER', 1, 'Kết quả huấn luyện năm 2023 - Line Valve 01',
        'tl_prod01', '2023-12-28 16:00:00');

INSERT INTO training_result_detail (id, training_result_id, training_plan_detail_id, training_topic_id,
                                    planned_date, actual_date, product_group_id,
                                    time_in, time_out, training_sample,status,
                                    detection_time, is_pass, remedial_action,
                                    signature_pro_in, signature_fi_in, signature_pro_out, signature_fi_out,
                                    created_by)
VALUES
-- NV001 - OP10 (Pass)
(1, 1, 1, NULL,
 '2023-10-05', '2023-10-05', 1,
 '08:30:00', '08:45:00', 'Mẫu chuẩn OP10','DONE',
 12, TRUE, NULL,
 5, 8, 5, 8, 'tl_prod01'),

-- NV001 - OP20 (Pass) - Dùng training topic 1
(2, 1, 2, 1,
 '2023-10-10', '2023-10-10', 1,
 '09:00:00', '09:18:00', 'Mẫu NG #55','DONE',
 18, TRUE, NULL,
 5, 8, 5, 8, 'tl_prod01'),

-- NV002 - OP20 (Fail)
(3, 1, 3, 1,
 '2023-10-12', '2023-10-12', 2,
 '10:00:00', '10:25:00', 'Mẫu NG #55','DONE',
 25, FALSE, 'Huấn luyện lại sau 1 tuần. Nguyên nhân: Chưa quen thao tác soi đèn.',
 5, 8, 5, 8, 'tl_prod01'),

-- NV005 - OP10 (Pass)
(4, 1, 4, NULL,
 '2023-10-15', '2023-10-15', 1,
 '14:00:00', '14:12:00', 'Mẫu chuẩn OP10','DONE',
 10, TRUE, NULL,
 5, 8, 5, 8, 'tl_prod01'),

-- NV003 - OP30 (Pass)
(5, 1, 5, 2,
 '2023-11-05', '2023-11-05', 3,
 '08:00:00', '08:20:00', 'Mẫu NG #62','DONE',
 15, TRUE, NULL,
 5, 8, 5, 8, 'tl_prod01'),

-- NV003 - OP40 (Pass) - Classification 4, không cần FI
(6, 1, 6, NULL,
 '2023-11-10', '2023-11-10', 1,
 '09:30:00', '09:42:00', 'Mẫu mài chuẩn','DONE',
 11, TRUE, NULL,
 5, NULL, 5, NULL, 'tl_prod01'),

-- NV002 - FI (Pass)
(7, 1, 8, 3,
 '2023-12-05', '2023-12-05', 1,
 '08:00:00', '08:10:00', 'Bộ mẫu chuẩn #01','DONE',
 9, TRUE, NULL,
 5, 8, 5, 8, 'tl_prod01'),

-- NV007 - OP30 (Pass)
(8, 1, 9, 2,
 '2023-12-10', '2023-12-10', 2,
 '10:00:00', '10:18:00', 'Mẫu NG #62','DONE',
 16, TRUE, NULL,
 6, 8, 6, 8, 'tl_prod02');

-- Result 2: ON_GOING (2024)
INSERT INTO training_result (id,title, form_code, year, group_id, status, current_version, note, created_by, created_at)
VALUES (2,'Kết quả huấn luyện năm 2024 - Line Valve 01', 'TR_RESULT_2024', 2024, 1, 'ON_GOING', 1, 'Kết quả huấn luyện năm 2024 - Line Valve 01', 'tl_prod01',
        '2024-01-02 08:00:00');

-- Detail cho plan đang pending (chưa có kết quả)
INSERT INTO training_result_detail (id, training_result_id, training_plan_detail_id, training_topic_id,
                                    planned_date, actual_date, product_group_id,
                                    time_in, time_out, training_sample,status,
                                    detection_time, is_pass, remedial_action,
                                    signature_pro_in, signature_fi_in, signature_pro_out, signature_fi_out,
                                    created_by)
VALUES
-- NV005 - OP20 (Chưa làm)
(9, 2, 10, 1,
 '2024-01-08', NULL, NULL,
 NULL, NULL, NULL,'PENDING',
 NULL, NULL, NULL,
 NULL, NULL, NULL, NULL, 'tl_prod01'),

-- NV005 - OP30 (Chưa làm)
(10, 2, 11, 2,
 '2024-01-15', NULL, NULL,
 NULL, NULL, NULL,'PENDING',
 NULL, NULL, NULL,
 NULL, NULL, NULL, NULL, 'tl_prod01');


-- ============================================================================
-- PART 11: NOTIFICATION TEMPLATES
-- ============================================================================

INSERT INTO notification_templates (code, subject_template, body_template, description, created_by)
VALUES
-- Defect Report
('DEFECT_WAITING_SV', '[Training] Báo cáo lỗi cần xem xét', 'email/defect-waiting-approval',
 'TL gửi báo cáo lỗi, thông báo SV', 'admin'),
('DEFECT_WAITING_MANAGER', '[Training] Báo cáo lỗi cần phê duyệt', 'email/defect-waiting-approval',
 'SV duyệt xong, thông báo Manager', 'admin'),
('DEFECT_APPROVED', '[Training] Báo cáo lỗi đã được duyệt', 'email/defect-approved', 'Báo cáo lỗi được duyệt', 'admin'),
('DEFECT_REJECTED_BY_SV', '[Training] Báo cáo lỗi bị từ chối', 'email/defect-rejected', 'SV từ chối', 'admin'),
('DEFECT_REJECTED_BY_MANAGER', '[Training] Báo cáo lỗi bị từ chối', 'email/defect-rejected', 'Manager từ chối',
 'admin'),

-- Training Topic Report
('TOPIC_WAITING_SV', '[Training] Mẫu huấn luyện cần phê duyệt', 'email/topic-waiting-approval', 'TL gửi, thông báo SV',
 'admin'),
('TOPIC_WAITING_MANAGER', '[Training] Mẫu huấn luyện cần phê duyệt', 'email/topic-waiting-approval',
 'SV duyệt xong, thông báo Manager', 'admin'),
('TOPIC_APPROVED', '[Training] Mẫu huấn luyện đã được duyệt', 'email/topic-approved', 'Mẫu huấn luyện được duyệt',
 'admin'),
('TOPIC_REJECTED_BY_SV', '[Training] Mẫu huấn luyện bị từ chối', 'email/topic-rejected', 'SV từ chối', 'admin'),
('TOPIC_REJECTED_BY_MANAGER', '[Training] Mẫu huấn luyện bị từ chối', 'email/topic-rejected', 'Manager từ chối',
 'admin'),

-- Training Plan
('PLAN_WAITING_SV', '[Training] Kế hoạch cần phê duyệt: $${formCode}', 'email/plan-waiting-approval',
 'TL gửi kế hoạch, thông báo SV', 'admin'),
('PLAN_WAITING_MANAGER', '[Training] Kế hoạch cần phê duyệt: $${formCode}', 'email/plan-waiting-approval',
 'SV duyệt xong, thông báo Manager', 'admin'),
('PLAN_APPROVED', '[Training] Kế hoạch đã được duyệt: $${formCode}', 'email/plan-approved', 'Kế hoạch được duyệt',
 'admin'),
('PLAN_REJECTED_BY_SV', '[Training] Kế hoạch bị từ chối: $${formCode}', 'email/plan-rejected', 'SV từ chối', 'admin'),
('PLAN_REJECTED_BY_MANAGER', '[Training] Kế hoạch bị từ chối: $${formCode}', 'email/plan-rejected', 'Manager từ chối',
 'admin'),

-- Training Result
('RESULT_WAITING_MANAGER', '[Training] Kết quả cần phê duyệt', 'email/result-waiting-approval', 'Thông báo Manager',
 'admin'),
('RESULT_APPROVED', '[Training] Kết quả đã được duyệt', 'email/result-approved', 'Kết quả được duyệt', 'admin'),
('RESULT_REJECTED_BY_MANAGER', '[Training] Kết quả bị từ chối', 'email/result-rejected', 'Manager từ chối', 'admin'),

-- Reminders
('TRAINING_REMINDER_TODAY', '[Training] Nhắc nhở: Lịch huấn luyện hôm nay', 'email/training-reminder-today',
 'Nhắc TL có lịch hôm nay', 'admin'),
('TRAINING_REMINDER_UPCOMING', '[Training] Nhắc nhở: Lịch huấn luyện ngày mai', 'email/training-reminder-upcoming',
 'Nhắc TL lịch ngày mai', 'admin'),
('TRAINING_OVERDUE', '[Training] Cảnh báo: Lịch huấn luyện quá hạn', 'email/training-overdue', 'Cảnh báo quá hạn',
 'admin'),

-- Approval Overdue
('APPROVAL_OVERDUE_SV', '[Training] Nhắc nhở: $${count} phê duyệt đang chờ', 'email/approval-overdue', 'Nhắc SV xử lý',
 'admin'),
('APPROVAL_OVERDUE_MANAGER', '[Training] Nhắc nhở: $${count} phê duyệt đang chờ', 'email/approval-overdue',
 'Nhắc Manager xử lý', 'admin');


-- ============================================================================
-- PART 12: NOTIFICATION SETTINGS
-- ============================================================================

INSERT INTO notification_settings (template_code, is_enabled, remind_before_days, is_persistent, remind_interval_hours,
                                   max_reminders, preferred_send_time, escalate_after_days, created_by)
VALUES
-- Defect Report
('DEFECT_WAITING_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('DEFECT_WAITING_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('DEFECT_APPROVED', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('DEFECT_REJECTED_BY_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('DEFECT_REJECTED_BY_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),

-- Training Topic Report
('TOPIC_WAITING_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('TOPIC_WAITING_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('TOPIC_APPROVED', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('TOPIC_REJECTED_BY_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('TOPIC_REJECTED_BY_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),

-- Training Plan
('PLAN_WAITING_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('PLAN_WAITING_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('PLAN_APPROVED', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('PLAN_REJECTED_BY_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('PLAN_REJECTED_BY_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),

-- Training Result
('RESULT_WAITING_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('RESULT_APPROVED', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),
('RESULT_REJECTED_BY_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'admin'),

-- Reminders
('TRAINING_REMINDER_TODAY', TRUE, 0, FALSE, 24, 1, '06:00:00', NULL, 'admin'),
('TRAINING_REMINDER_UPCOMING', TRUE, 1, FALSE, 24, 1, '06:00:00', NULL, 'admin'),
('TRAINING_OVERDUE', TRUE, 0, TRUE, 24, 5, '08:00:00', 3, 'admin'),

-- Approval Overdue (Nagging)
('APPROVAL_OVERDUE_SV', TRUE, 0, TRUE, 24, 999, '08:00:00', NULL, 'admin'),
('APPROVAL_OVERDUE_MANAGER', TRUE, 0, TRUE, 24, 999, '08:00:00', NULL, 'admin');


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- END OF SAMPLE DATA
-- ============================================================================

-- ============================================================================
-- Alter Table
-- ============================================================================
ALTER TABLE defects
DROP COLUMN is_escaped;

ALTER TABLE defect_report_detail
DROP COLUMN is_escaped;

ALTER TABLE defect_report_detail_history
DROP COLUMN is_escaped;