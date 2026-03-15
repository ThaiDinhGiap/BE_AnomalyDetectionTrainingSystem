-- ============================================================================
-- ANOMALY TRAINING SYSTEM - SAMPLE DATA
-- Version: 2.0
-- Description: Insert sample data for testing with new core database schema
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE training_result_details;
TRUNCATE TABLE training_results;
TRUNCATE TABLE training_plan_detail_history;
TRUNCATE TABLE training_plan_history;
TRUNCATE TABLE training_plan_details;
TRUNCATE TABLE training_plans;
-- ============================================================================
-- PART 1: USERS & ROLES
-- ============================================================================
-- Password: Password@123 (BCrypt encoded)
INSERT INTO users
(id, username, email, password_hash, full_name, is_active, created_by, employee_code)
VALUES
-- ROLE_ADMIN
(1, 'ROLE_ADMIN', 'ROLE_ADMIN@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Quản Trị Hệ Thống', TRUE, 'system', 'EMP000'),

-- ROLE_MANAGER
(2, 'ROLE_MANAGER01', 'ROLE_MANAGER01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Nguyễn Văn Quản Lý', TRUE, 'system', 'EMP001'),

-- ROLE_SUPERVISORs
(3, 'ROLE_SUPERVISOR01', 'ROLE_SUPERVISOR01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Trần Văn Giám Sát', TRUE, 'system', 'EMP002'),

(4, 'ROLE_SUPERVISOR02', 'ROLE_SUPERVISOR02@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Lê Thị Giám Sát', TRUE, 'system', 'EMP003'),

-- Team Leaders (Production)
(5, 'tl_prod01', 'tl_prod01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Phạm Văn Trưởng Ca 1', TRUE, 'system', 'EMP004'),

(6, 'tl_prod02', 'tl_prod02@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Hoàng Văn Trưởng Ca 2', TRUE, 'system', 'EMP005'),

(7, 'tl_prod03', 'tl_prod03@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Võ Thị Trưởng Ca 3', TRUE, 'system', 'EMP006'),

-- Final Inspection
(8, 'tl_fi01', 'tl_fi01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Đỗ Văn Kiểm Tra 1', TRUE, 'system', 'EMP007'),

(9, 'tl_fi02', 'tl_fi02@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Ngô Thị Kiểm Tra 2', TRUE, 'system', 'EMP008');

-- Bổ sung một số Role và Module cơ bản
INSERT INTO roles (id, role_code, display_name, description, is_system, is_active, created_by)
VALUES (1, 'ROLE_ADMIN', 'ADMIN', 'Quyền truy cập toàn hệ thống', TRUE, TRUE, 'system'),
       (2, 'ROLE_MANAGER', 'MANAGER', 'Quyền phê duyệt cấp quản lý', TRUE, TRUE, 'system'),
       (3, 'ROLE_SUPERVISOR', 'SUPERVISOR', 'Quyền giám sát, phê duyệt cấp 1', TRUE, TRUE, 'system'),
       (4, 'ROLE_TEAM_LEADER', 'TEAM_LEADER', 'Khởi tạo báo cáo và nhập liệu', TRUE, TRUE, 'system'),
       (5, 'ROLE_FINAL_INSPECTION', 'FINAL_INSPECTION', 'Nhập kết quả kiểm tra cuối', TRUE, TRUE, 'system');

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
INSERT INTO sections (id, code, name, manager_id, created_by)
VALUES (1, 'SEC01', 'Xưởng Gia Công Cơ Khí', 2, 'ROLE_ADMIN'),
       (2, 'SEC02', 'Xưởng Lắp Ráp & Đóng Gói', 2, 'ROLE_ADMIN');

-- Groups (Dây chuyền / Khu vực)
INSERT INTO `groups` (id, section_id, name, supervisor_id, created_by)
VALUES (1, 1, 'Khu Vực Tiện CNC', 3, 'ROLE_ADMIN'),
       (2, 1, 'Khu Vực Phay CNC', 3, 'ROLE_ADMIN'),
       (3, 2, 'Khu Vực Lắp Ráp Máy Bơm', 4, 'ROLE_ADMIN');

-- Product Lines (Dòng sản phẩm - Table mới)
INSERT INTO product_lines (id, code, name, group_id, created_by)
VALUES (1, 'PL-P1', 'Dòng Máy Bơm Nước P1', 1, 'ROLE_ADMIN'),
       (2, 'PL-P2', 'Dòng Bơm Thủy Lực P2', 2, 'ROLE_ADMIN'),
       (3, 'PL-DE', 'Dây Chuyền Lắp Ráp Động Cơ Nổ', 3, 'ROLE_ADMIN');

-- Teams (Tổ sản xuất)
INSERT INTO teams (id, group_id, name, team_leader_id, created_by)
VALUES (1, 1, 'Tổ Tiện Ca Ngày', 5, 'ROLE_ADMIN'),
       (2, 2, 'Tổ Phay Ca Ngày', 6, 'ROLE_ADMIN'),
       (3, 3, 'Tổ Lắp Ráp Cuối', 7, 'ROLE_ADMIN');

-- Employees (Công nhân)
INSERT INTO employees (id, employee_code, full_name, team_id, status, created_by)
VALUES (1, 'NV001', 'Nguyễn Văn An', 1, 'ACTIVE', 'ROLE_ADMIN'),
       (2, 'NV002', 'Trần Thị Bình', 1, 'ACTIVE', 'ROLE_ADMIN'),
       (3, 'NV003', 'Lê Văn Cường', 1, 'ACTIVE', 'ROLE_ADMIN'),
       (4, 'NV004', 'Võ Thị Dung', 2, 'ACTIVE', 'ROLE_ADMIN'),
       (5, 'NV005', 'Đặng Văn Em', 2, 'ACTIVE', 'ROLE_ADMIN'),
       (6, 'NV006', 'Bùi Thị Phượng', 3, 'MATERNITY_LEAVE', 'ROLE_ADMIN'),
       (7, 'NV007', 'Ngô Văn Giàu', 3, 'ACTIVE', 'ROLE_ADMIN');

-- Products (Sản phẩm - Table mới)
INSERT INTO products (id, code, name, description, created_by)
VALUES (1, 'BOM-2024-X1', 'Bơm nước dân dụng X1', 'Công suất 1.5HP', 'ROLE_ADMIN'),
       (2, 'BOM-2024-X2', 'Bơm nước công nghiệp X2', 'Công suất 5.0HP', 'ROLE_ADMIN'),
       (3, 'MOT-2024-Y1', 'Động cơ xăng Y1', 'Động cơ 4 thì', 'ROLE_ADMIN');

-- Processes (Công đoạn)
INSERT INTO processes (id, product_line_id, code, name, description, classification, standard_time_jt, created_by)
VALUES (1, 1, 'OP10', 'Gia công thô trục bơm', 'Tiện thô trục bằng máy CNC, dung sai ±0.5mm', 2, 15.00, 'ROLE_ADMIN'),
       (2, 1, 'OP20', 'Gia công tinh trục bơm', 'Tiện tinh trục chính xác, dung sai ±0.02mm', 1, 20.00, 'ROLE_ADMIN'),
       (3, 2, 'OP30', 'Phay mặt bích', 'Phay phẳng mặt bích tiếp xúc', 3, 18.00, 'ROLE_ADMIN'),
       (4, 3, 'AS10', 'Lắp ráp cánh bơm', 'Lắp cánh bơm vào thân buồng bơm', 2, 25.00, 'ROLE_ADMIN'),
       (5, 3, 'AS20', 'Test áp lực nước', 'Kiểm tra rò rỉ áp suất 10bar', 1, 30.00, 'ROLE_ADMIN');

-- Product_Process (N:M Sản phẩm & Công đoạn)
INSERT INTO product_process (product_id, process_id, standard_time_jt, created_by)
VALUES (1, 1, 15.00, 'ROLE_ADMIN'),
       (1, 2, 20.00, 'ROLE_ADMIN'),
       (2, 3, 18.00, 'ROLE_ADMIN'),
       (3, 4, 25.00, 'ROLE_ADMIN'),
       (3, 5, 30.00, 'ROLE_ADMIN');

-- Employee Skills
INSERT INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by)
VALUES (1, 1, 'VALID', '2023-01-15', '2026-01-15', 'ROLE_ADMIN'),
       (1, 2, 'PENDING_REVIEW', '2023-03-20', '2026-03-20', 'ROLE_ADMIN'),
       (2, 1, 'REVOKED', '2023-02-10', '2026-02-10', 'ROLE_ADMIN'),
       (3, 1, 'REVOKED', '2022-06-01', '2025-06-01', 'ROLE_ADMIN'),
       (4, 3, 'PENDING_REVIEW', '2023-05-01', '2026-05-01', 'ROLE_ADMIN'),
       (5, 3, 'PENDING_REVIEW', '2023-07-01', '2026-07-01', 'ROLE_ADMIN'),
       (7, 4, 'VALID', '2023-04-15', '2026-04-15', 'ROLE_ADMIN'),
       (7, 5, 'VALID', '2023-06-20', '2026-06-20', 'ROLE_ADMIN'),
       -- Data for Expiring Skills
       (1, 3, 'VALID', CURDATE() - INTERVAL 1 YEAR, CURDATE() + INTERVAL 15 DAY, 'ROLE_ADMIN'),
       (2, 2, 'VALID', CURDATE() - INTERVAL 2 YEAR, CURDATE() + INTERVAL 1 DAY, 'ROLE_ADMIN'),
       (3, 3, 'VALID', CURDATE() - INTERVAL 1 YEAR, CURDATE() + INTERVAL 29 DAY, 'ROLE_ADMIN'),
       -- Counter-examples (not expiring)
       (4, 4, 'VALID', CURDATE() - INTERVAL 1 YEAR, CURDATE() + INTERVAL 31 DAY, 'ROLE_ADMIN'),
       (5, 5, 'VALID', CURDATE() - INTERVAL 1 YEAR, CURDATE() - INTERVAL 1 DAY, 'ROLE_ADMIN');


-- ============================================================================
-- PART 3: DEFECT MANAGEMENT (Lỗi quá khứ & Báo cáo)
-- ============================================================================
INSERT INTO defects (defect_code,
                     defect_description,
                     process_id,
                     detected_date,
                     defect_type,
                     origin_measures,
                     outflow_measures,
                     note,
                     origin_cause,
                     outflow_cause,
                     cause_point,
                     created_by)
VALUES ('DF001', 'Xước bề mặt trục', 1, '2023-09-01', 'DEFECTIVE_GOODS', 'Tăng tần suất kiểm tra dao',
        'Thêm bước kiểm tra dao vào checklist', 'Phát hiện tại OP10', 'Dao cụ mòn', 'Không kiểm tra dao định kỳ',
        'Tiện', 'system'),
       ('DF002', 'Sai dung sai đường kính', 1, '2023-09-02', 'CLAIM', 'Hiệu chỉnh lại máy', 'Đào tạo lại QC',
        'Lọt lắp ráp', 'Sai offset', 'QC không kiểm 100%', 'Tiện', 'system'),
       ('DF003', 'Bavia chưa xử lý', 1, '2023-09-03', 'DEFECTIVE_GOODS', 'Bổ sung bước deburr vào quy trình',
        'Tạo checklist kiểm tra', 'Phát hiện QC', 'Thiếu bước deburr', 'Không có checklist', 'Hoàn thiện', 'system'),
       ('DF004', 'Sai vị trí lỗ', 1, '2023-09-04', 'DEFECTIVE_GOODS', 'Sửa lại đồ gá', 'Thêm bước xác nhận đầu ca',
        'Máy lệch tâm', 'Đồ gá không cố định', 'Không xác nhận đầu ca', 'Khoan', 'system'),
       ('DF005', 'Biến dạng sau ép', 1, '2023-09-05', 'CLAIM', 'Hiệu chỉnh lực ép', 'Thêm bước kiểm tra lực ép',
        'Lọt công đoạn sau', 'Áp lực ép lớn', 'Không kiểm tra lực ép', 'Ép', 'system'),

       ('DF006', 'Nứt bề mặt', 1, '2023-09-06', 'DEFECTIVE_GOODS', 'Hiệu chỉnh lò nhiệt', 'Giám sát nhiệt độ chặt chẽ',
        'Kiểm tra từ tính', 'Nhiệt luyện sai', 'Không kiểm soát nhiệt độ', 'Nhiệt luyện', 'system'),
       ('DF007', 'Sai độ nhám', 1, '2023-09-07', 'STARTLED_CLAIM', 'Cập nhật lại thông số',
        'Thêm trạm kiểm tra cuối line', 'Khách hàng phản hồi', 'Thông số sai', 'Thiếu kiểm tra cuối line', 'Tiện',
        'system'),
       ('DF008', 'Ren bị mòn', 1, '2023-09-08', 'DEFECTIVE_GOODS', 'Thay dao định kỳ', 'Quản lý tuổi thọ dao',
        'Dao ren mòn', 'Dao quá tuổi thọ', 'Không thay dao định kỳ', 'Tiện ren', 'system'),
       ('DF009', 'Sai kích thước then', 1, '2023-09-09', 'DEFECTIVE_GOODS', 'Cập nhật lại bản vẽ',
        'Quy trình review bản vẽ', 'Sai bản vẽ', 'Cập nhật nhầm version', 'Không review bản vẽ', 'Phay', 'system'),
       ('DF010', 'Trầy xước nội bộ', 1, '2023-09-10', 'CLAIM', 'Sử dụng khay chuyên dụng', 'Kiểm tra packaging',
        'Vận chuyển nội bộ', 'Không có khay đựng', 'Không kiểm tra packaging', 'Vận chuyển', 'system'),

       ('DF011', 'Sai lực siết vít', 1, '2023-09-11', 'DEFECTIVE_GOODS', 'Hiệu chỉnh súng torque',
        'Kiểm tra torque định kỳ', 'Torque sai', 'Chưa calibrate', 'Không kiểm tra torque định kỳ', 'Lắp ráp',
        'system'),
       ('DF012', 'Sai vị trí rãnh', 1, '2023-09-12', 'DEFECTIVE_GOODS', 'Đào tạo lại cách set dao',
        'Kiểm tra first piece 100%', 'Lệch dao', 'Set dao sai', 'Không kiểm tra first piece', 'Phay', 'system'),
       ('DF013', 'Sai chiều dài tổng', 1, '2023-09-13', 'CLAIM', 'Hiệu chỉnh offset', 'Kiểm tra 3 mẫu đầu', 'Lọt QC',
        'Offset sai', 'Không kiểm tra 3 mẫu đầu', 'Tiện', 'system'),
       ('DF014', 'Rỗ bề mặt', 1, '2023-09-14', 'DEFECTIVE_GOODS', 'Kiểm tra vật liệu đầu vào',
        'Yêu cầu chứng chỉ vật liệu', 'Vật liệu lỗi', 'Nguyên liệu kém', 'Không kiểm incoming', 'Tiện', 'system'),
       ('DF015', 'Cong vênh chi tiết', 1, '2023-09-15', 'CLAIM', 'Hiệu chỉnh quy trình nhiệt luyện',
        'Kiểm soát nhiệt độ chặt chẽ', 'Biến dạng', 'Nhiệt luyện sai', 'Không kiểm nhiệt độ', 'Nhiệt luyện', 'system'),

       ('DF016', 'Tràn keo terminal', 2, '2023-10-01', 'DEFECTIVE_GOODS', 'Hiệu chỉnh máy bơm keo',
        'Kiểm tra đầu ca', 'Keo quá mức', 'Cài đặt sai', 'Không hiệu chỉnh đầu ca', 'Bơm keo', 'system'),
       ('DF017', 'Thiếu lượng keo', 2, '2023-10-02', 'CLAIM', 'Bảo trì bơm định kỳ', 'Kiểm tra định lượng',
        'Test fail', 'Bơm không ổn định', 'Không kiểm tra định lượng', 'Bơm keo', 'system'),
       ('DF018', 'Sai cực linh kiện', 2, '2023-10-03', 'DEFECTIVE_GOODS', 'Đào tạo lại nhân viên',
        'Thêm bước kiểm tra visual', 'Lắp ngược', 'Thiếu đào tạo', 'Không kiểm tra visual', 'Lắp ráp', 'system'),
       ('DF019', 'Hở mối hàn', 2, '2023-10-04', 'CLAIM', 'Hiệu chỉnh nhiệt độ hàn', 'Tăng cường kiểm tra AOI',
        'AOI bỏ sót', 'Nhiệt hàn thấp', 'Không kiểm nhiệt độ', 'Hàn', 'system'),
       ('DF020', 'Cháy linh kiện', 2, '2023-10-05', 'CLAIM', 'Hiệu chỉnh máy test', 'Kiểm tra setup máy',
        'Sai điện áp', 'Cài đặt máy sai', 'Không kiểm setup', 'Test điện', 'system'),

       ('DF021', 'Lệch vị trí bắt vít', 2, '2023-10-06', 'DEFECTIVE_GOODS', 'Sửa lại template', 'Kiểm tra jig đầu ca',
        'Template sai', 'Không cố định jig', 'Không kiểm tra đầu ca', 'Lắp ráp', 'system'),
       ('DF022', 'Bong keo test nhiệt', 2, '2023-10-07', 'STARTLED_CLAIM', 'Thay đổi nhà cung cấp keo',
        'Kiểm tra vật liệu đầu vào', 'Khách hàng trả về', 'Keo kém chất lượng', 'Không kiểm vật liệu đầu vào',
        'Bơm keo', 'system'),
       ('DF023', 'Thiếu linh kiện nhỏ', 2, '2023-10-08', 'DEFECTIVE_GOODS', 'Sử dụng hệ thống pick-to-light',
        'Kiểm tra final 100%', 'Sót linh kiện', 'Không check BOM', 'Không kiểm final', 'Lắp ráp', 'system'),
       ('DF024', 'Nứt chân linh kiện', 2, '2023-10-09', 'DEFECTIVE_GOODS', 'Đào tạo lại thao tác',
        'Sử dụng khay đựng phù hợp', 'Va chạm', 'Handling sai', 'Không đào tạo thao tác', 'Lắp ráp', 'system'),
       ('DF025', 'Sai barcode', 2, '2023-10-10', 'CLAIM', 'Sửa lại template in', 'Review file in trước khi in',
        'In sai mã', 'Template lỗi', 'Không review file in', 'In nhãn', 'system'),

       ('DF026', 'Chạm chập mạch', 2, '2023-10-11', 'STARTLED_CLAIM', 'Hiệu chỉnh máy hàn', 'Tăng cường kiểm tra AOI',
        'Khách hàng phản hồi', 'Hàn dư thiếc', 'Không kiểm AOI', 'Hàn', 'system'),
       ('DF027', 'Sai thông số điện trở', 2, '2023-10-12', 'DEFECTIVE_GOODS', 'Sử dụng máy quét barcode',
        'Xác nhận linh kiện trước khi lắp', 'Chọn nhầm part', 'Thiếu xác nhận linh kiện', 'Không check BOM', 'Lắp ráp',
        'system'),
       ('DF028', 'Bể vỏ khi ép', 2, '2023-10-13', 'CLAIM', 'Hiệu chỉnh lực ép', 'Kiểm tra lực ép định kỳ',
        'Lực ép lớn', 'Cài đặt sai', 'Không kiểm lực ép', 'Ép vỏ', 'system'),
       ('DF029', 'Hở gioăng', 2, '2023-10-14', 'DEFECTIVE_GOODS', 'Sử dụng guide lắp ráp', 'Kiểm tra cuối line',
        'Lắp lệch', 'Thiếu guide', 'Không kiểm cuối line', 'Lắp ráp', 'system'),
       ('DF030', 'Sai thứ tự lắp ráp', 2, '2023-10-15', 'CLAIM', 'Đào tạo lại quy trình', 'Giám sát chặt chẽ hơn',
        'Lọt QC', 'Không theo WI', 'Không giám sát', 'Assembly', 'system');
-- Defect Proposals (Header)
INSERT INTO defect_proposals (id, product_line_id, status, current_version, form_code, created_by)
VALUES (1, 1, 'APPROVED', 1, 'DEF-2023-001', 'tl_prod01'),
       (2, 3, 'WAITING_SV', 1, 'DEF-2023-002', 'tl_prod03');

-- Defect Proposal Details
INSERT INTO defect_proposal_details (defect_proposal_id, defect_id, proposal_type, defect_description, process_id,
                                     detected_date, defect_type, origin_measures, outflow_measures, note,
                                     origin_cause, outflow_cause, cause_point, created_by)
VALUES (1, 1, 'CREATE', 'Xước bề mặt trục do dao cụ mòn', 2, '2023-09-15', 'DEFECTIVE_GOODS', 'Thay dao mới',
        'Tăng tần suất kiểm tra', 'Đã xử lý dao', 'Dao mẻ', 'Không soi đèn kỹ', 'Tại đài dao', 'tl_prod01'),
       (1, 2, 'CREATE', 'Lỗi kích thước đường kính ngoài dung sai', 1, '2023-09-20', 'CLAIM', 'Hiệu chỉnh máy',
        'Đào tạo lại cách đo', 'Lọt ra khâu lắp ráp', 'Setup sai thông số', 'Đo sai cách', 'Tại khâu kẹp phôi',
        'tl_prod01'),
       (2, 3, 'CREATE', 'Rò rỉ ron cao su khi test áp lực', 5, '2023-10-05', 'CLAIM', 'Sử dụng đồ gá mới',
        'Đào tạo lại thao tác ép', 'Cần huấn luyện khẩn', 'Rách ron khi ép', 'Lực ép tay không đều', 'Trạm ép ron',
        'tl_prod03');


-- ============================================================================
-- PART 4: TRAINING SAMPLES (Mẫu huấn luyện)
-- ============================================================================

-- Training Samples (Master Data)
INSERT INTO training_samples (id,
                              process_id,
                              product_line_id,
                              defect_id,
                              category_name,
                              training_description,
                              product_id,
                              training_sample_code,
                              training_code,
                              process_order,
                              category_order,
                              content_order,
                              note,
                              created_by)
VALUES (1, 2, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Yêu cầu công nhân soi đèn góc 45 độ để phát hiện vết xước. Thời gian tiêu chuẩn: 20 giây.',
        1, 'Mẫu NG #55', 'TS0001', 1, 1, 1, 'Lỗi quan trọng', 'system'),

       (2, 1, 1, 2, 'Lỗi Kích Thước',
        'Sử dụng thước kẹp điện tử đo 3 điểm: đầu, giữa, cuối. Ghi nhận vào form.',
        1, 'Mẫu NG #62', 'TS0002', 1, 2, 1, 'Đã lọt qua trạm', 'system'),

       (3, 5, 3, 3, 'Lắp ráp ron cao su',
        'Sử dụng đồ gá chuẩn, ép lực đều tay tránh rách ron. Kiểm tra bằng mắt trước khi đưa vào test.',
        3, 'Mẫu chuẩn #01', 'TS0003', 1, 1, 1, 'Lỗi lọt KH', 'system'),

       (4, 2, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Kiểm tra bề mặt sản phẩm dưới ánh sáng trắng, xoay đủ 4 mặt để nhận diện vết mẻ nhỏ.',
        1, 'Mẫu NG #56', 'TS0004', 1, 1, 2, 'Lỗi ngoại quan', 'system'),

       (5, 2, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Đặt mẫu trên khay đen để tăng độ tương phản khi quan sát vết trầy xước.',
        1, 'Mẫu NG #57', 'TS0005', 1, 1, 3, 'Tăng khả năng nhận diện', 'system'),

       (6, 1, 1, 2, 'Lỗi Kích Thước',
        'Đo chiều dài tổng thể bằng thước điện tử, sai số cho phép không vượt quá 0.05 mm.',
        1, 'Mẫu NG #63', 'TS0006', 1, 2, 2, 'Kiểm tra đầu ca', 'system'),

       (7, 1, 1, 2, 'Lỗi Kích Thước',
        'Kiểm tra đường kính ngoài tại 2 vị trí khác nhau, nếu lệch phải báo tổ trưởng xác nhận.',
        1, 'Mẫu NG #64', 'TS0007', 1, 2, 3, 'Sai số thường gặp', 'system'),

       (8, 3, 2, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'So sánh trực tiếp với mẫu chuẩn OK và NG để phân biệt mức độ lỗi ngoại quan.',
        2, 'Mẫu NG #70', 'TS0008', 2, 1, 1, 'Áp dụng cho line B', 'system'),

       (9, 3, 2, 2, 'Lỗi Kích Thước',
        'Dùng dưỡng kiểm chuyên dùng để xác nhận nhanh kích thước giới hạn trên và dưới.',
        2, 'Mẫu NG #71', 'TS0009', 2, 2, 1, 'Ưu tiên đào tạo mới', 'system'),

       (10, 3, 2, 3, 'Lắp ráp ron cao su',
        'Lắp ron đúng chiều, không xoắn mép, bề mặt ron phải nằm kín hoàn toàn trên rãnh.',
        2, 'Mẫu chuẩn #02', 'TS0010', 2, 3, 1, 'Cần thao tác mẫu', 'system'),

       (11, 4, 2, 3, 'Lắp ráp ron cao su',
        'Trước khi lắp phải kiểm tra ron có bụi, ba via hoặc biến dạng hay không.',
        2, 'Mẫu chuẩn #03', 'TS0011', 2, 3, 2, 'Kiểm tra trước lắp ráp', 'system'),

       (12, 4, 2, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Quan sát vùng mép bo tròn vì đây là vị trí thường phát sinh xước do va chạm khay.',
        2, 'Mẫu NG #72', 'TS0012', 2, 1, 2, 'Tỷ lệ lỗi cao', 'system'),

       (13, 4, 2, 2, 'Lỗi Kích Thước',
        'Đo khoảng cách tâm lỗ bằng dưỡng đo chuyên dụng, ghi nhận kết quả vào biểu mẫu QC.',
        2, 'Mẫu NG #73', 'TS0013', 2, 2, 2, 'Bổ sung minh hoạ', 'system'),

       (14, 5, 3, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Kiểm tra mặt tiếp xúc với gioăng vì khu vực này dễ bị xước trong quá trình vận chuyển nội bộ.',
        3, 'Mẫu NG #80', 'TS0014', 3, 1, 1, 'Lỗi nội bộ', 'system'),

       (15, 5, 3, 2, 'Lỗi Kích Thước',
        'Đo chiều sâu rãnh bằng panme đo sâu, bảo đảm kết quả nằm trong giới hạn bản vẽ.',
        3, 'Mẫu NG #81', 'TS0015', 3, 2, 1, 'Đo tại 3 vị trí', 'system'),

       (16, 5, 3, 3, 'Lắp ráp ron cao su',
        'Bôi lớp dầu mỏng đúng tiêu chuẩn trước khi lắp để tránh cắn ron trong quá trình ép.',
        3, 'Mẫu chuẩn #04', 'TS0016', 3, 3, 2, 'Có dùng dầu bôi trơn', 'system'),

       (17, 2, 1, 3, 'Lắp ráp ron cao su',
        'Không dùng vật sắc nhọn để chỉnh ron, chỉ dùng tay hoặc dụng cụ nhựa mềm.',
        1, 'Mẫu chuẩn #05', 'TS0017', 1, 3, 1, 'Tránh rách ron', 'system'),

       (18, 2, 1, 2, 'Lỗi Kích Thước',
        'Kiểm tra độ dày thành sản phẩm bằng đồng hồ so, nếu bất thường phải cô lập lô.',
        1, 'Mẫu NG #58', 'TS0018', 1, 2, 4, 'Điểm kiểm soát chính', 'system'),

       (19, 1, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Quan sát vết cấn tại cạnh sắc bằng tay kết hợp mắt thường để nhận biết lỗi nhỏ.',
        1, 'Mẫu NG #65', 'TS0019', 1, 1, 4, 'Dễ nhầm với ba via', 'system'),

       (20, 1, 1, 3, 'Lắp ráp ron cao su',
        'Sau khi lắp xong phải rà một vòng toàn chu vi để chắc chắn ron không bị đội lên.',
        1, 'Mẫu chuẩn #06', 'TS0020', 1, 3, 2, 'Kiểm 100%', 'system'),

       (21, 3, 2, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Dùng khăn sạch lau bề mặt trước khi kiểm tra để tránh nhầm bụi với vết xước.',
        2, 'Mẫu NG #74', 'TS0021', 2, 1, 3, 'Làm sạch trước kiểm tra', 'system'),

       (22, 3, 2, 2, 'Lỗi Kích Thước',
        'Kiểm tra chiều cao gờ chặn bằng thước đo cao, chấp nhận theo giới hạn bản vẽ.',
        2, 'Mẫu NG #75', 'TS0022', 2, 2, 3, 'Áp dụng cho model Y1', 'system'),

       (23, 3, 2, 3, 'Lắp ráp ron cao su',
        'Thực hiện thao tác lắp theo chiều kim đồng hồ để tránh bỏ sót đoạn cuối.',
        2, 'Mẫu chuẩn #07', 'TS0023', 2, 3, 3, 'Thống nhất thao tác', 'system'),

       (24, 4, 2, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Đặc biệt chú ý vùng gần lỗ bắt vít vì dễ xuất hiện mẻ cạnh nhỏ.',
        2, 'Mẫu NG #76', 'TS0024', 2, 1, 4, 'Hay phát sinh ở khuôn cũ', 'system'),

       (25, 4, 2, 2, 'Lỗi Kích Thước',
        'Dùng pin gauge kiểm tra lỗ xuyên, không được ép mạnh gây sai lệch kết quả.',
        2, 'Mẫu NG #77', 'TS0025', 2, 2, 4, 'Kiểm tra theo SOP mới', 'system'),

       (26, 4, 2, 3, 'Lắp ráp ron cao su',
        'Không để ron tiếp xúc với bụi hoặc mạt kim loại trước khi đưa vào công đoạn lắp.',
        2, 'Mẫu chuẩn #08', 'TS0026', 2, 3, 4, 'Bắt buộc vệ sinh', 'system'),

       (27, 5, 3, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Đặt sản phẩm nghiêng 30 độ dưới đèn để quan sát rõ các vết cấn chìm trên bề mặt.',
        3, 'Mẫu NG #82', 'TS0027', 3, 1, 2, 'Khó thấy bằng mắt thường', 'system'),

       (28, 5, 3, 2, 'Lỗi Kích Thước',
        'Kiểm tra khoảng hở lắp ghép bằng lá căn, đối chiếu tiêu chuẩn từng model.',
        3, 'Mẫu NG #83', 'TS0028', 3, 2, 2, 'Phải ghi nhận vào checklist', 'system'),

       (29, 2, 1, 3, 'Lắp ráp ron cao su',
        'Sau khi lắp xong phải kiểm tra độ kín sơ bộ bằng mắt trước khi chuyển công đoạn.',
        1, 'Mẫu chuẩn #06', 'TS0029', 1, 3, 2, 'Kiểm 100%', 'system'),

       (30, 1, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
        'Đối chiếu vị trí lỗi với hình ảnh hướng dẫn đào tạo để tránh phân loại sai lỗi.',
        1, 'Mẫu NG #66', 'TS0030', 1, 1, 5, 'Dùng cho nhân viên mới', 'system');
-- Training Sample Proposals
INSERT INTO training_sample_proposals (id, product_line_id, status, current_version, form_code, created_by)
VALUES (1, 1, 'APPROVED', 1, 'TSP-2023-001', 'tl_prod01');

INSERT INTO training_sample_proposal_details (training_sample_proposal_id, training_sample_id, proposal_type,
                                              process_id, product_id, defect_id, category_name, training_sample_code,
                                              training_description, note, created_by)
VALUES (1, 1, 'CREATE', 2, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ', 'Mẫu NG #55', 'Yêu cầu soi đèn góc 45 độ...',
        'Ghi chú thêm', 'tl_prod01');

-- ============================================================================
-- 1. TRAINING PLANS (Tổng 15 Kế hoạch)
-- Team 1 (Line 1): NV 1, 2, 3
-- Team 2 (Line 2): NV 4, 5
-- Team 3 (Line 3): NV 7
-- ============================================================================
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version,
                            note, min_training_per_day, max_training_per_day, created_by)
VALUES
-- 5 Kế hoạch gốc (1 tháng)
(1, 'TR_PLAN_TIEN_001', 'Kế hoạch huấn luyện T3/2026 - Line Tiện CNC', '2026-03-01', '2026-03-31', 1, 1, 'APPROVED', 2,
 'Đã duyệt. NV001 có 2 lần thêm.', 1, 3, 'tl_prod01'),
(2, 'TR_PLAN_TIEN_002', 'Kế hoạch huấn luyện T4/2026 - Line Tiện CNC', '2026-04-01', '2026-04-30', 1, 1, 'DRAFT', 1,
 'Đang soạn thảo, chưa submit.', 1, 3, 'tl_prod01'),
(3, 'TR_PLAN_PHAY_001', 'Kế hoạch huấn luyện T3/2026 - Line Phay CNC', '2026-03-01', '2026-03-31', 2, 2, 'WAITING_SV',
 1, 'Đã submit, chờ ROLE_SUPERVISOR duyệt.', 1, 3, 'tl_prod02'),
(4, 'TR_PLAN_LAP_001', 'Kế hoạch huấn luyện T3/2026 - Line Lắp Ráp', '2026-03-01', '2026-03-31', 3, 3,
 'REJECTED_BY_SV', 1, 'Bị SV trả lại vì thiếu lịch cho NV007.', 1, 3, 'tl_prod03'),
(5, 'TR_PLAN_PHAY_002', 'Kế hoạch huấn luyện T2/2026 - Line Phay CNC', '2026-02-01', '2026-02-28', 2, 2, 'APPROVED', 1,
 'Tháng 2, đã duyệt và hoàn thành.', 1, 3, 'tl_prod02'),

-- 10 Kế hoạch mới (Nhiều tháng)
(6, 'TR_PLAN_TIEN_003', 'Kế hoạch huấn luyện T4-T5/2026 - Line Tiện CNC', '2026-04-01', '2026-05-31', 1, 1, 'APPROVED',
 1, 'Kế hoạch 2 tháng. Đã duyệt.', 1, 3, 'tl_prod01'),
(7, 'TR_PLAN_PHAY_003', 'Kế hoạch huấn luyện T5-T7/2026 - Line Phay CNC', '2026-05-01', '2026-07-31', 2, 2, 'APPROVED',
 1, 'Kế hoạch 3 tháng. Đã duyệt.', 1, 3, 'tl_prod02'),
(8, 'TR_PLAN_LAP_002', 'Kế hoạch huấn luyện T6-T9/2026 - Line Lắp Ráp', '2026-06-01', '2026-09-30', 3, 3, 'WAITING_SV',
 1, 'Kế hoạch 4 tháng dài hạn, chờ SV duyệt.', 1, 3, 'tl_prod03'),
(9, 'TR_PLAN_TIEN_004', 'Kế hoạch huấn luyện T7-T8/2026 - Line Tiện CNC', '2026-07-01', '2026-08-31', 1, 1, 'DRAFT', 1,
 'Đang soạn thảo cho Quý 3.', 1, 3, 'tl_prod01'),
(10, 'TR_PLAN_PHAY_004', 'Kế hoạch huấn luyện T8-T10/2026 - Line Phay CNC', '2026-08-01', '2026-10-31', 2, 2,
 'APPROVED', 1, 'Đã duyệt cho T8, T9, T10.', 1, 3, 'tl_prod02'),
(11, 'TR_PLAN_LAP_003', 'Kế hoạch huấn luyện T9-T10/2026 - Line Lắp Ráp', '2026-09-01', '2026-10-31', 3, 3,
 'REJECTED_BY_SV', 1, 'Bị từ chối do trùng lịch sản xuất lớn T9.', 1, 3, 'tl_prod03'),
(12, 'TR_PLAN_TIEN_005', 'Kế hoạch huấn luyện T1-T4/2026 - Line Tiện CNC', '2026-01-01', '2026-04-30', 1, 1,
 'APPROVED', 1, 'Kế hoạch đầu năm, đã hoàn tất toàn bộ.', 1, 3, 'tl_prod01'),
(13, 'TR_PLAN_PHAY_005', 'Kế hoạch huấn luyện T10-T11/2026 - Line Phay CNC', '2026-10-01', '2026-11-30', 2, 2, 'DRAFT',
 1, 'Dự thảo cuối năm.', 1, 3, 'tl_prod02'),
(14, 'TR_PLAN_LAP_004', 'Kế hoạch huấn luyện T11-T1/2027 - Line Lắp Ráp', '2026-11-01', '2027-01-31', 3, 3,
 'WAITING_SV', 1, 'Kế hoạch vắt qua năm sau, chờ duyệt.', 1, 3, 'tl_prod03'),
(15, 'TR_PLAN_TIEN_006', 'Kế hoạch huấn luyện T11-T12/2026 - Line Tiện CNC', '2026-11-01', '2026-12-31', 1, 1,
 'APPROVED', 1, 'Chốt sổ cuối năm 2026, đã duyệt.', 1, 3, 'tl_prod01');


-- ============================================================================
-- 2. TRAINING PLAN DETAILS (50 Dòng chi tiết)
-- ============================================================================
INSERT INTO training_plan_details (id, training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by)
VALUES
-- ==== Dữ liệu từ 5 Plan gốc ====
(1, 1, 1, 'batch-p1-nv001-1', '2026-03-01', '2026-03-05', '2026-03-05', 'DONE', 'NV001 lần 1 - ngày 5', 'tl_prod01'),
(2, 1, 1, 'batch-p1-nv001-1', '2026-03-01', '2026-03-12', '2026-03-12', 'DONE', 'NV001 lần 1 - ngày 12', 'tl_prod01'),
(3, 1, 1, 'batch-p1-nv001-1', '2026-03-01', '2026-03-19', NULL, 'PENDING', 'NV001 lần 1 - ngày 19', 'tl_prod01'),
(4, 1, 1, 'batch-p1-nv001-2', '2026-03-01', '2026-03-22', NULL, 'PENDING', 'NV001 lần 2 - ngày 22', 'tl_prod01'),
(5, 1, 1, 'batch-p1-nv001-2', '2026-03-01', '2026-03-28', NULL, 'PENDING', 'NV001 lần 2 - ngày 28', 'tl_prod01'),
(6, 1, 2, 'batch-p1-nv002-1', '2026-03-01', '2026-03-06', '2026-03-06', 'DONE', 'NV002 - ngày 6', 'tl_prod01'),
(7, 1, 2, 'batch-p1-nv002-1', '2026-03-01', '2026-03-20', NULL, 'PENDING', 'NV002 - ngày 20', 'tl_prod01'),
(8, 1, 3, 'batch-p1-nv003-1', '2026-03-01', '2026-03-03', NULL, 'MISSED', '[Đã nghỉ] NV003', 'tl_prod01'),
(9, 1, 3, 'batch-p1-nv003-1', '2026-03-01', '2026-03-15', NULL, 'PENDING', 'NV003 - ngày 15', 'tl_prod01'),
(10, 2, 1, 'batch-p2-nv001-1', '2026-04-01', '2026-04-05', NULL, 'PENDING', 'NV001 - ngày 5/4', 'tl_prod01'),
(11, 2, 1, 'batch-p2-nv001-1', '2026-04-01', '2026-04-15', NULL, 'PENDING', 'NV001 - ngày 15/4', 'tl_prod01'),
(12, 2, 2, 'batch-p2-nv002-1', '2026-04-01', '2026-04-10', NULL, 'PENDING', 'NV002 - ngày 10/4', 'tl_prod01'),
(13, 2, 3, 'batch-p2-nv003-1', '2026-04-01', '2026-04-08', NULL, 'PENDING', 'NV003 - ngày 8/4', 'tl_prod01'),
(14, 2, 3, 'batch-p2-nv003-1', '2026-04-01', '2026-04-22', NULL, 'PENDING', 'NV003 - ngày 22/4', 'tl_prod01'),
(15, 3, 4, 'batch-p3-nv004-1', '2026-03-01', '2026-03-07', NULL, 'PENDING', 'NV004 - ngày 7', 'tl_prod02'),
(16, 3, 4, 'batch-p3-nv004-1', '2026-03-01', '2026-03-14', NULL, 'PENDING', 'NV004 - ngày 14', 'tl_prod02'),
(17, 3, 4, 'batch-p3-nv004-1', '2026-03-01', '2026-03-21', NULL, 'PENDING', 'NV004 - ngày 21', 'tl_prod02'),
(18, 3, 5, 'batch-p3-nv005-1', '2026-03-01', '2026-03-10', NULL, 'PENDING', 'NV005 - ngày 10', 'tl_prod02'),
(19, 3, 5, 'batch-p3-nv005-1', '2026-03-01', '2026-03-24', NULL, 'PENDING', 'NV005 - ngày 24', 'tl_prod02'),
(20, 4, 7, 'batch-p4-nv007-1', '2026-03-01', '2026-03-10', NULL, 'PENDING', 'NV007 - ngày 10', 'tl_prod03'),
(21, 4, 7, 'batch-p4-nv007-1', '2026-03-01', '2026-03-20', NULL, 'PENDING', 'NV007 - ngày 20', 'tl_prod03'),
(22, 5, 4, 'batch-p5-nv004-1', '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV004 - ngày 5/2', 'tl_prod02'),
(23, 5, 4, 'batch-p5-nv004-1', '2026-02-01', '2026-02-18', '2026-02-18', 'DONE', 'NV004 - ngày 18/2', 'tl_prod02'),
(24, 5, 5, 'batch-p5-nv005-1', '2026-02-01', '2026-02-10', '2026-02-10', 'DONE', 'NV005 - ngày 10/2', 'tl_prod02'),
(25, 5, 5, 'batch-p5-nv005-1', '2026-02-01', '2026-02-25', '2026-02-25', 'DONE', 'NV005 - ngày 25/2', 'tl_prod02'),

-- ==== Dữ liệu từ 10 Plan thêm mới ====
(26, 6, 1, 'batch-p6-nv001-1', '2026-04-01', '2026-04-10', '2026-04-10', 'DONE', 'Tháng 4: NV001 đã xong', 'tl_prod01'),
(27, 6, 2, 'batch-p6-nv002-1', '2026-05-01', '2026-05-15', NULL, 'PENDING', 'Tháng 5: NV002 chưa học', 'tl_prod01'),
(28, 7, 4, 'batch-p7-nv004-1', '2026-05-01', '2026-05-20', '2026-05-20', 'DONE', 'Tháng 5: NV004 đã xong', 'tl_prod02'),
(29, 7, 5, 'batch-p7-nv005-1', '2026-06-01', '2026-06-10', NULL, 'PENDING', 'Tháng 6: NV005', 'tl_prod02'),
(30, 7, 4, 'batch-p7-nv004-2', '2026-07-01', '2026-07-05', NULL, 'PENDING', 'Tháng 7: NV004 (lần 2)', 'tl_prod02'),
(31, 8, 7, 'batch-p8-nv007-1', '2026-06-01', '2026-06-15', NULL, 'PENDING', 'NV007 T6', 'tl_prod03'),
(32, 8, 7, 'batch-p8-nv007-2', '2026-08-01', '2026-08-20', NULL, 'PENDING', 'NV007 T8', 'tl_prod03'),
(33, 9, 3, 'batch-p9-nv003-1', '2026-07-01', '2026-07-10', NULL, 'PENDING', 'NV003 T7', 'tl_prod01'),
(34, 9, 3, 'batch-p9-nv003-1', '2026-08-01', '2026-08-10', NULL, 'PENDING', 'NV003 T8', 'tl_prod01'),
(35, 10, 4, 'batch-p10-nv004-1', '2026-08-01', '2026-08-15', NULL, 'PENDING', 'Chờ tới T8', 'tl_prod02'),
(36, 10, 5, 'batch-p10-nv005-1', '2026-09-01', '2026-09-15', NULL, 'PENDING', 'Chờ tới T9', 'tl_prod02'),
(37, 10, 5, 'batch-p10-nv005-2', '2026-10-01', '2026-10-15', NULL, 'PENDING', 'Chờ tới T10', 'tl_prod02'),
(38, 11, 7, 'batch-p11-nv007-1', '2026-09-01', '2026-09-05', NULL, 'PENDING', 'T9', 'tl_prod03'),
(39, 11, 7, 'batch-p11-nv007-1', '2026-10-01', '2026-10-05', NULL, 'PENDING', 'T10', 'tl_prod03'),
(40, 12, 1, 'batch-p12-nv001-1', '2026-01-01', '2026-01-10', '2026-01-10', 'DONE', 'T1 xong', 'tl_prod01'),
(41, 12, 1, 'batch-p12-nv001-1', '2026-02-01', '2026-02-10', '2026-02-10', 'DONE', 'T2 xong', 'tl_prod01'),
(42, 12, 2, 'batch-p12-nv002-1', '2026-03-01', '2026-03-15', '2026-03-15', 'DONE', 'T3 xong', 'tl_prod01'),
(43, 12, 2, 'batch-p12-nv002-1', '2026-04-01', '2026-04-20', '2026-04-20', 'DONE', 'T4 xong', 'tl_prod01'),
(44, 13, 5, 'batch-p13-nv005-1', '2026-10-01', '2026-10-02', NULL, 'PENDING', 'Draft T10', 'tl_prod02'),
(45, 13, 5, 'batch-p13-nv005-1', '2026-11-01', '2026-11-02', NULL, 'PENDING', 'Draft T11', 'tl_prod02'),
(46, 14, 7, 'batch-p14-nv007-3', '2026-11-01', '2026-11-20', NULL, 'PENDING', 'Waiting T11', 'tl_prod03'),
(47, 14, 7, 'batch-p14-nv007-3', '2026-12-01', '2026-12-20', NULL, 'PENDING', 'Waiting T12', 'tl_prod03'),
(48, 14, 7, 'batch-p14-nv007-3', '2027-01-01', '2027-01-20', NULL, 'PENDING', 'Waiting T1', 'tl_prod03'),
(49, 15, 1, 'batch-p15-nv001-1', '2026-11-01', '2026-11-11', NULL, 'PENDING', 'Approved (T11)', 'tl_prod01'),
(50, 15, 3, 'batch-p15-nv003-1', '2026-12-01', '2026-12-12', NULL, 'PENDING', 'Approved (T12)', 'tl_prod01');


-- ============================================================================
-- 3. TRAINING PLAN HISTORY (Chỉ sinh với các kế hoạch APPROVED: 1, 5, 6, 7, 10, 12, 15)
-- ============================================================================
INSERT INTO training_plan_history (id, training_plan_id, title, version, form_code, month_start, month_end, team_id,
                                   line_id, note, recorded_at, created_by)
VALUES (1, 1, 'Kế hoạch huấn luyện T3/2026 - Line Tiện CNC', 1, 'TR_PLAN_TIEN_001', '2026-03-01', '2026-03-31', 1, 1,
        'Bản gốc', '2026-03-01 08:00:00', 'tl_prod01'),
       (2, 1, 'Kế hoạch huấn luyện T3/2026 - Line Tiện CNC', 2, 'TR_PLAN_TIEN_001', '2026-03-01', '2026-03-31', 1, 1,
        'Sau reschedule', '2026-03-04 10:30:00', 'tl_prod01'),
       (3, 5, 'Kế hoạch huấn luyện T2/2026 - Line Phay CNC', 1, 'TR_PLAN_PHAY_002', '2026-02-01', '2026-02-28', 2, 2,
        'Bản gốc', '2026-02-01 08:00:00', 'tl_prod02'),
       (4, 6, 'Kế hoạch huấn luyện T4-T5/2026 - Line Tiện', 1, 'TR_PLAN_TIEN_003', '2026-04-01', '2026-05-31', 1, 1,
        'Kế hoạch nhiều tháng', '2026-04-01 08:00:00', 'tl_prod01'),
       (5, 7, 'Kế hoạch huấn luyện T5-T7/2026 - Line Phay', 1, 'TR_PLAN_PHAY_003', '2026-05-01', '2026-07-31', 2, 2,
        'Bản gốc T5', '2026-05-01 08:00:00', 'tl_prod02'),
       (6, 10, 'Kế hoạch huấn luyện T8-T10/2026 - Line Phay', 1, 'TR_PLAN_PHAY_004', '2026-08-01', '2026-10-31', 2, 2,
        'Bản gốc T8', '2026-08-01 08:00:00', 'tl_prod02'),
       (7, 12, 'Kế hoạch huấn luyện T1-T4/2026 - Line Tiện', 1, 'TR_PLAN_TIEN_005', '2026-01-01', '2026-04-30', 1, 1,
        'Bản gốc năm 2026', '2026-01-01 08:00:00', 'tl_prod01'),
       (8, 15, 'Kế hoạch huấn luyện T11-T12/2026 - Line Tiện', 1, 'TR_PLAN_TIEN_006', '2026-11-01', '2026-12-31', 1, 1,
        'Bản gốc T11', '2026-11-01 08:00:00', 'tl_prod01');


-- ============================================================================
-- 4. TRAINING PLAN DETAIL HISTORY (Snapshot History Detail)
-- ============================================================================
INSERT INTO training_plan_detail_history (id, training_plan_history_id, batch_id, employee_id, target_month,
                                          planned_date, actual_date, status, note, created_by)
VALUES
-- History ID = 1 (Plan 1 - Ver 1)
(1, 1, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-05', NULL, 'PENDING', 'NV001 lần 1', 'tl_prod01'),
(2, 1, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-12', NULL, 'PENDING', 'NV001 lần 1', 'tl_prod01'),
(3, 1, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-19', NULL, 'PENDING', 'NV001 lần 1', 'tl_prod01'),
(4, 1, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-06', NULL, 'PENDING', 'NV002', 'tl_prod01'),
(5, 1, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-20', NULL, 'PENDING', 'NV002', 'tl_prod01'),
(6, 1, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-03', NULL, 'PENDING', 'NV003 gốc', 'tl_prod01'),
(7, 1, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-10', NULL, 'PENDING', 'NV003 gốc', 'tl_prod01'),

-- History ID = 2 (Plan 1 - Ver 2)
(8, 2, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-05', '2026-03-05', 'DONE', 'NV001', 'tl_prod01'),
(9, 2, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-12', '2026-03-12', 'DONE', 'NV001', 'tl_prod01'),
(10, 2, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-19', NULL, 'PENDING', 'NV001', 'tl_prod01'),
(11, 2, 'batch-p1-nv001-2', 1, '2026-03-01', '2026-03-22', NULL, 'PENDING', 'NV001 lần 2', 'tl_prod01'),
(12, 2, 'batch-p1-nv001-2', 1, '2026-03-01', '2026-03-28', NULL, 'PENDING', 'NV001 lần 2', 'tl_prod01'),
(13, 2, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-06', '2026-03-06', 'DONE', 'NV002', 'tl_prod01'),
(14, 2, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-20', NULL, 'PENDING', 'NV002', 'tl_prod01'),
(15, 2, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-03', NULL, 'MISSED', 'NV003 nghỉ', 'tl_prod01'),
(16, 2, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-15', NULL, 'PENDING', 'NV003 dời', 'tl_prod01'),

-- History ID = 3 (Plan 5)
(17, 3, 'batch-p5-nv004-1', 4, '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV004', 'tl_prod02'),
(18, 3, 'batch-p5-nv004-1', 4, '2026-02-01', '2026-02-18', '2026-02-18', 'DONE', 'NV004', 'tl_prod02'),
(19, 3, 'batch-p5-nv005-1', 5, '2026-02-01', '2026-02-10', '2026-02-10', 'DONE', 'NV005', 'tl_prod02'),
(20, 3, 'batch-p5-nv005-1', 5, '2026-02-01', '2026-02-25', '2026-02-25', 'DONE', 'NV005', 'tl_prod02'),

-- History ID = 4 (Plan 6)
(21, 4, 'batch-p6-nv001-1', 1, '2026-04-01', '2026-04-10', '2026-04-10', 'DONE', 'Tháng 4', 'tl_prod01'),
(22, 4, 'batch-p6-nv002-1', 2, '2026-05-01', '2026-05-15', NULL, 'PENDING', 'Tháng 5', 'tl_prod01'),

-- History ID = 5 (Plan 7)
(23, 5, 'batch-p7-nv004-1', 4, '2026-05-01', '2026-05-20', '2026-05-20', 'DONE', 'Tháng 5', 'tl_prod02'),
(24, 5, 'batch-p7-nv005-1', 5, '2026-06-01', '2026-06-10', NULL, 'PENDING', 'Tháng 6', 'tl_prod02'),
(25, 5, 'batch-p7-nv004-2', 4, '2026-07-01', '2026-07-05', NULL, 'PENDING', 'Tháng 7', 'tl_prod02'),

-- History ID = 6 (Plan 10)
(26, 6, 'batch-p10-nv004-1', 4, '2026-08-01', '2026-08-15', NULL, 'PENDING', 'T8', 'tl_prod02'),
(27, 6, 'batch-p10-nv005-1', 5, '2026-09-01', '2026-09-15', NULL, 'PENDING', 'T9', 'tl_prod02'),
(28, 6, 'batch-p10-nv005-2', 5, '2026-10-01', '2026-10-15', NULL, 'PENDING', 'T10', 'tl_prod02'),

-- History ID = 7 (Plan 12)
(29, 7, 'batch-p12-nv001-1', 1, '2026-01-01', '2026-01-10', '2026-01-10', 'DONE', 'T1', 'tl_prod01'),
(30, 7, 'batch-p12-nv001-1', 1, '2026-02-01', '2026-02-10', '2026-02-10', 'DONE', 'T2', 'tl_prod01'),
(31, 7, 'batch-p12-nv002-1', 2, '2026-03-01', '2026-03-15', '2026-03-15', 'DONE', 'T3', 'tl_prod01'),
(32, 7, 'batch-p12-nv002-1', 2, '2026-04-01', '2026-04-20', '2026-04-20', 'DONE', 'T4', 'tl_prod01'),

-- History ID = 8 (Plan 15)
(33, 8, 'batch-p15-nv001-1', 1, '2026-11-01', '2026-11-11', NULL, 'PENDING', 'T11', 'tl_prod01'),
(34, 8, 'batch-p15-nv003-1', 3, '2026-12-01', '2026-12-12', NULL, 'PENDING', 'T12', 'tl_prod01');


-- ============================================================================
-- 5. TRAINING RESULTS (Đại diện cho Cột mốc Ghi nhận kết quả của Plan APPROVED)
-- (Chỉ dùng status 'ON_GOING' cho chưa hoàn thành hoặc 'APPROVED' cho hoàn thành toàn bộ)
-- ============================================================================
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES (1, 1, 'Kết quả huấn luyện T3/2026 - Line Tiện', 'TR_RES_TIEN_001', 2026, 1, 1, 'ON_GOING', 1,
        'Đang ghi nhận dở dang', 'tl_prod01'),
       (2, 5, 'Kết quả huấn luyện T2/2026 - Line Phay', 'TR_RES_PHAY_002', 2026, 2, 2, 'APPROVED', 1,
        'Hoàn thành toàn bộ T2', 'tl_prod02'),
       (3, 6, 'Kết quả huấn luyện T4-T5/2026 - Line Tiện', 'TR_RES_TIEN_003', 2026, 1, 1, 'ON_GOING', 1,
        'Đang ghi nhận (Có 1 DONE, 1 PENDING)', 'tl_prod01'),
       (4, 7, 'Kết quả huấn luyện T5-T7/2026 - Line Phay', 'TR_RES_PHAY_003', 2026, 2, 2, 'ON_GOING', 1,
        'Mới ghi nhận tháng 5', 'tl_prod02'),
       (5, 10, 'Kế hoạch huấn luyện T8-T10/2026 - Line Phay', 'TR_RES_PHAY_004', 2026, 2, 2, 'ON_GOING', 1,
        'Chưa diễn ra, đang pending toàn bộ', 'tl_prod02'),
       (6, 12, 'Kết quả huấn luyện T1-T4/2026 - Line Tiện', 'TR_RES_TIEN_005', 2026, 1, 1, 'APPROVED', 1,
        'Đã hoàn thành xuất sắc 4 tháng', 'tl_prod01'),
       (7, 15, 'Kết quả huấn luyện T11-T12/2026 - Line Tiện', 'TR_RES_TIEN_006', 2026, 1, 1, 'ON_GOING', 1,
        'Chưa diễn ra', 'tl_prod01');


-- ============================================================================
-- 6. TRAINING RESULT DETAILS (Mapping 1-1 với ID của bảng Training Plan Details)
-- ============================================================================
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, process_id,
                                     training_sample_id, product_id, classification, training_topic, sample_code,
                                     cycle_time_standard,
                                     planned_date, actual_date, time_in, time_start_op, time_out, status,
                                     detection_time, is_pass, note, is_retrained,
                                     signature_pro_in, signature_fi_in, signature_pro_out, signature_fi_out, created_by)
VALUES
-- Mapping với Result 1 (Kế hoạch 1 gốc)
(1, 1, 1, 1, 2, 1, 2, NULL, 'Mẫu NG #62', 15.00, '2026-03-05', '2026-03-05', '08:00:00', '08:03:00', '08:18:00',
 'APPROVED', 14, TRUE, 'Thao tác OP10 tốt', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(1, 2, 1, 2, 1, 1, 1, NULL, 'Mẫu NG #55', 20.00, '2026-03-12', '2026-03-12', '09:00:00', '09:02:00', '09:22:00',
 'APPROVED', 19, TRUE, 'Soi đèn đúng góc', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(1, 3, 1, 1, NULL, NULL, NULL, 'Huấn luyện đột xuất', NULL, NULL, '2026-03-19', NULL, NULL, NULL, NULL, 'PENDING',
 NULL, NULL, 'Chờ ngày 19/3', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),
(1, 4, 1, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-22', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chờ ngày 22/3', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),
(1, 5, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-28', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chờ ngày 28/3', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),
(1, 6, 2, 1, 2, 1, 2, NULL, 'Mẫu NG #62', 15.00, '2026-03-06', '2026-03-06', '08:30:00', '08:33:00', '08:48:00',
 'APPROVED', 13, TRUE, 'NV002 chuẩn', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(1, 7, 2, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-20', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chờ ngày 20/3', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),
(1, 9, 3, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-03-15', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chờ ngày 15/3', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),

-- Mapping với Result 2 (Kế hoạch 5 gốc - APPROVED toàn bộ)
(2, 22, 4, 3, 8, 2, 3, NULL, 'Mẫu NG #70', 18.00, '2026-02-05', '2026-02-05', '08:00:00', '08:02:00', '08:20:00',
 'APPROVED', 16, TRUE, 'NV004 OK', FALSE, 6, 9, 6, 9, 'tl_prod02'),
(2, 23, 4, 3, 9, 2, 3, NULL, 'Mẫu NG #71', 18.00, '2026-02-18', '2026-02-18', '09:00:00', '09:03:00', '09:21:00',
 'APPROVED', 17, TRUE, 'NV004 lần 2', FALSE, 6, 9, 6, 9, 'tl_prod02'),
(2, 24, 5, 3, 10, 2, 3, NULL, 'Mẫu chuẩn #02', 18.00, '2026-02-10', '2026-02-10', '10:00:00', '10:02:00', '10:20:00',
 'APPROVED', 15, TRUE, 'NV005 tốt', FALSE, 6, 9, 6, 9, 'tl_prod02'),
(2, 25, 5, 3, 11, 2, 3, NULL, 'Mẫu chuẩn #03', 18.00, '2026-02-25', '2026-02-25', '08:30:00', '08:32:00', '08:50:00',
 'APPROVED', 16, TRUE, 'NV005 lần 2', FALSE, 6, 9, 6, 9, 'tl_prod02'),

-- Mapping với Result 3 (Kế hoạch 6)
(3, 26, 1, 1, 2, 1, 2, NULL, 'Mẫu NG #62', 15.00, '2026-04-10', '2026-04-10', '08:00:00', '08:05:00', '08:20:00',
 'APPROVED', 15, TRUE, 'NV001 hoàn thành T4', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(3, 27, 2, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-05-15', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chờ tới giữa tháng 5', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),

-- Mapping với Result 4 (Kế hoạch 7)
(4, 28, 4, 3, 1, 2, 3, NULL, 'Mẫu NG #55', 18.00, '2026-05-20', '2026-05-20', '09:00:00', '09:02:00', '09:20:00',
 'APPROVED', 18, TRUE, 'NV004 hoàn thành T5', FALSE, 6, 9, 6, 9, 'tl_prod02'),
(4, 29, 5, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2026-06-10', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL, 'Chờ T6',
 FALSE, NULL, NULL, NULL, NULL, 'tl_prod02'),
(4, 30, 4, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2026-07-05', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL, 'Chờ T7',
 FALSE, NULL, NULL, NULL, NULL, 'tl_prod02'),

-- Mapping với Result 5 (Kế hoạch 10)
(5, 35, 4, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2026-08-15', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chưa tới hạn', FALSE, NULL, NULL, NULL, NULL, 'tl_prod02'),
(5, 36, 5, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2026-09-15', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chưa tới hạn', FALSE, NULL, NULL, NULL, NULL, 'tl_prod02'),
(5, 37, 5, 3, NULL, NULL, NULL, NULL, NULL, NULL, '2026-10-15', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Chưa tới hạn', FALSE, NULL, NULL, NULL, NULL, 'tl_prod02'),

-- Mapping với Result 6 (Kế hoạch 12 - APPROVED toàn bộ 4 tháng đầu năm)
(6, 40, 1, 1, 1, 1, 1, NULL, 'Mẫu NG #55', 20.00, '2026-01-10', '2026-01-10', '08:00:00', '08:02:00', '08:22:00',
 'APPROVED', 20, TRUE, 'T1 xuất sắc', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(6, 41, 1, 2, 2, 1, 2, NULL, 'Mẫu NG #62', 15.00, '2026-02-10', '2026-02-10', '08:30:00', '08:32:00', '08:47:00',
 'APPROVED', 15, TRUE, 'T2 xuất sắc', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(6, 42, 2, 1, 1, 1, 1, NULL, 'Mẫu NG #55', 20.00, '2026-03-15', '2026-03-15', '09:00:00', '09:05:00', '09:25:00',
 'APPROVED', 20, TRUE, 'T3 xuất sắc', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(6, 43, 2, 2, 2, 1, 2, NULL, 'Mẫu NG #62', 15.00, '2026-04-20', '2026-04-20', '10:00:00', '10:01:00', '10:16:00',
 'APPROVED', 15, TRUE, 'T4 xuất sắc', FALSE, 5, 8, 5, 8, 'tl_prod01'),

-- Mapping với Result 7 (Kế hoạch 15)
(7, 49, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, '2026-11-11', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Lịch T11', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),
(7, 50, 3, 2, NULL, NULL, NULL, NULL, NULL, NULL, '2026-12-12', NULL, NULL, NULL, NULL, 'PENDING', NULL, NULL,
 'Lịch T12', FALSE, NULL, NULL, NULL, NULL, 'tl_prod01'),

-- Data for Pending Signatures
(1, 8, 3, 1, 2, 1, 2, NULL, 'Mẫu NG #62', 15.00, '2026-03-10', '2026-03-10', '10:00:00', '10:03:00', '10:18:00',
 'NEED_SIGN', 14, TRUE, 'Cần ký', FALSE, 5, 8, NULL, NULL, 'tl_prod01'),
(1, 11, 1, 2, 1, 1, 1, NULL, 'Mẫu NG #55', 20.00, '2026-04-15', '2026-04-15', '13:00:00', '13:02:00', '13:22:00',
 'NEED_SIGN', 19, TRUE, 'Chờ ký xác nhận ra', FALSE, 5, 8, NULL, NULL, 'tl_prod01'),
(2, 15, 4, 3, 8, 2, 3, NULL, 'Mẫu NG #70', 18.00, '2026-03-07', '2026-03-07', '14:00:00', '14:02:00', '14:20:00',
 'NEED_SIGN', 16, TRUE, 'Đã ký vào, chờ ký ra', FALSE, 6, 9, NULL, NULL, 'tl_prod02'),


-- Data for Failed Trainings
(1, 10, 1, 1, 2, 1, 2, NULL, 'Mẫu NG #62', 15.00, '2026-04-05', '2026-04-05', '11:00:00', '11:03:00', '11:20:00',
 'APPROVED', 16, FALSE, 'Trượt, cần đào tạo lại', FALSE, 5, 8, 5, 8, 'tl_prod01'),
(2, 16, 4, 3, 4, 2, 3, NULL, 'Mẫu NG #71', 18.00, '2026-03-14', '2026-03-14', '15:00:00', '15:03:00', '15:25:00',
 'APPROVED', 20, FALSE, 'Trượt, chưa có kế hoạch đào tạo lại', NULL, 6, 9, 6, 9, 'tl_prod02'),
(3, 20, 7, 4, 3, 3, 2, NULL, 'Mẫu chuẩn #01', 25.00, '2026-03-10', '2026-03-10', '16:00:00', '16:02:00', '16:30:00',
 'APPROVED', 27, FALSE, 'Trượt, chưa có kế hoạch', FALSE, 7, 8, 7, 8, 'tl_prod03'),
-- Counter-example (Failed but already retrained)
(4, 21, 7, 5, 1, 3, 1, NULL, 'Mẫu NG #55', 30.00, '2026-03-20', '2026-03-20', '17:00:00', '17:02:00', '17:35:00',
 'APPROVED', 32, FALSE, 'Trượt, đã có kế hoạch đào tạo lại', TRUE, 7, 8, 7, 8, 'tl_prod03');


-- ============================================================================
-- PART 6: NOTIFICATIONS & SYSTEM CONFIGS
-- ============================================================================

-- Notification Templates
INSERT INTO notification_templates (code, subject_template, body_template, description, created_by)
VALUES ('DEFECT_WAITING_SV', '[Hệ Thống Đào Tạo] Báo cáo lỗi cần xem xét', 'email/defect-waiting-approval',
        'TL gửi báo cáo lỗi, thông báo SV', 'ROLE_ADMIN'),
       ('DEFECT_WAITING_ROLE_MANAGER', '[Hệ Thống Đào Tạo] Báo cáo lỗi cần phê duyệt', 'email/defect-waiting-approval',
        'SV duyệt xong, thông báo ROLE_MANAGER', 'ROLE_ADMIN'),
       ('PLAN_WAITING_SV', '[Hệ Thống Đào Tạo] Kế hoạch cần phê duyệt: $${formCode}', 'email/plan-waiting-approval',
        'TL gửi kế hoạch, thông báo SV', 'ROLE_ADMIN');

-- Notification Settings
INSERT INTO notification_settings (template_code, is_enabled, remind_before_days, is_persistent,
                                   remind_interval_hours, max_reminders, preferred_send_time, escalate_after_days,
                                   created_by)
VALUES ('DEFECT_WAITING_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'ROLE_ADMIN'),
       ('DEFECT_WAITING_ROLE_MANAGER', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'ROLE_ADMIN'),
       ('PLAN_WAITING_SV', TRUE, 0, FALSE, 24, 1, '08:00:00', NULL, 'ROLE_ADMIN');


-- ============================================================================
-- PART 7: REJECTION & APPROVAL SUPPORT
-- ============================================================================

-- Reject Reasons
INSERT INTO reject_reasons (category_name, reason_name, created_by)
VALUES ('Dữ liệu', 'Thiếu thông tin mô tả lỗi chi tiết', 'ROLE_ADMIN'),
       ('Quy trình', 'Sai phân loại công đoạn đánh giá', 'ROLE_ADMIN'),
       ('Nội dung', 'Mẫu vật lý không đạt tiêu chuẩn', 'ROLE_ADMIN');

-- Required Actions
INSERT INTO required_actions (action_name, created_by)
VALUES ('Vui lòng bổ sung thêm thông tin', 'ROLE_ADMIN'),
       ('Yêu cầu làm lại mẫu NG mới', 'ROLE_ADMIN'),
       ('Trình bày lại báo cáo sự cố', 'ROLE_ADMIN');


-- ============================================================================
-- PART 8: ANNUAL REVIEW CONFIG
-- ============================================================================

INSERT INTO training_sample_review_configs (product_line_id, trigger_month, trigger_day, due_days, assignee_id,
                                            is_active, created_by)
VALUES (1, 3, 1, 30, 5, TRUE, 'ROLE_ADMIN'), -- Review định kỳ vào tháng 3 cho Line Tiện
       (2, 3, 1, 30, 6, TRUE, 'ROLE_ADMIN');

INSERT INTO approval_flow_steps (entity_type, step_order, approver_role, is_active, created_by)
VALUES
-- DEFECT_REPORT: SV -> ROLE_MANAGER
('DEFECT_REPORT', 1, 'ROLE_SUPERVISOR', TRUE, 'system'),
('DEFECT_REPORT', 2, 'ROLE_MANAGER', TRUE, 'system'),

-- TRAINING_TOPIC_REPORT: SV -> ROLE_MANAGER
('TRAINING_TOPIC_REPORT', 1, 'ROLE_SUPERVISOR', TRUE, 'system'),
('TRAINING_TOPIC_REPORT', 2, 'ROLE_MANAGER', TRUE, 'system'),

-- TRAINING_PLAN: SV -> ROLE_MANAGER
('TRAINING_PLAN', 1, 'ROLE_SUPERVISOR', TRUE, 'system'),
('TRAINING_PLAN', 2, 'ROLE_MANAGER', TRUE, 'system');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- END OF SAMPLE DATA
-- ============================================================================
