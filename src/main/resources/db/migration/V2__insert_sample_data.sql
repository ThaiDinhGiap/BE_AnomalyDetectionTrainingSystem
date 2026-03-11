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
INSERT INTO users
(id, username, email, password_hash, full_name, role, is_active, created_by, employee_code)
VALUES
-- Admin
(1, 'admin', 'admin@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Quản Trị Hệ Thống', 'ADMIN', TRUE, 'system', 'EMP000'),

-- Manager
(2, 'manager01', 'manager01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Nguyễn Văn Quản Lý', 'MANAGER', TRUE, 'system', 'EMP001'),

-- Supervisors
(3, 'supervisor01', 'supervisor01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Trần Văn Giám Sát', 'SUPERVISOR', TRUE, 'system', 'EMP002'),

(4, 'supervisor02', 'supervisor02@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Lê Thị Giám Sát', 'SUPERVISOR', TRUE, 'system', 'EMP003'),

-- Team Leaders (Production)
(5, 'tl_prod01', 'tl_prod01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Phạm Văn Trưởng Ca 1', 'TEAM_LEADER', TRUE, 'system', 'EMP004'),

(6, 'tl_prod02', 'tl_prod02@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Hoàng Văn Trưởng Ca 2', 'TEAM_LEADER', TRUE, 'system', 'EMP005'),

(7, 'tl_prod03', 'tl_prod03@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Võ Thị Trưởng Ca 3', 'TEAM_LEADER', TRUE, 'system', 'EMP006'),

-- Final Inspection
(8, 'tl_fi01', 'tl_fi01@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Đỗ Văn Kiểm Tra 1', 'FINAL_INSPECTION', TRUE, 'system', 'EMP007'),

(9, 'tl_fi02', 'tl_fi02@congty.com',
 '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
 'Ngô Thị Kiểm Tra 2', 'FINAL_INSPECTION', TRUE, 'system', 'EMP008');

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
INSERT INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by)
VALUES (1, 1, 'VALID', '2023-01-15', '2026-01-15', 'admin'),
       (1, 2, 'PENDING_REVIEW', '2023-03-20', '2026-03-20', 'admin'),
       (2, 1, 'REVOKED', '2023-02-10', '2026-02-10', 'admin'),
       (3, 1, 'REVOKED', '2022-06-01', '2025-06-01', 'admin'),
       (4, 3, 'PENDING_REVIEW', '2023-05-01', '2026-05-01', 'admin'),
       (5, 3, 'PENDING_REVIEW', '2023-07-01', '2026-07-01', 'admin'),
       (7, 4, 'VALID', '2023-04-15', '2026-04-15', 'admin'),
       (7, 5, 'VALID', '2023-06-20', '2026-06-20', 'admin');


-- ============================================================================
-- PART 3: DEFECT MANAGEMENT (Lỗi quá khứ & Báo cáo)
-- ============================================================================
INSERT INTO defects (
    defect_code,
    defect_description,
    process_id,
    detected_date,
    is_escaped,
    note,
    origin_cause,
    outflow_cause,
    cause_point,
    created_by
)
VALUES
    ('DF001', 'Xước bề mặt trục', 1, '2023-09-01', FALSE, 'Phát hiện tại OP10', 'Dao cụ mòn', 'Không kiểm tra dao định kỳ', 'Tiện', 'system'),
    ('DF002', 'Sai dung sai đường kính', 1, '2023-09-02', TRUE, 'Lọt lắp ráp', 'Sai offset', 'QC không kiểm 100%', 'Tiện', 'system'),
    ('DF003', 'Bavia chưa xử lý', 1, '2023-09-03', FALSE, 'Phát hiện QC', 'Thiếu bước deburr', 'Không có checklist', 'Hoàn thiện', 'system'),
    ('DF004', 'Sai vị trí lỗ', 1, '2023-09-04', FALSE, 'Máy lệch tâm', 'Đồ gá không cố định', 'Không xác nhận đầu ca', 'Khoan', 'system'),
    ('DF005', 'Biến dạng sau ép', 1, '2023-09-05', TRUE, 'Lọt công đoạn sau', 'Áp lực ép lớn', 'Không kiểm tra lực ép', 'Ép', 'system'),

    ('DF006', 'Nứt bề mặt', 1, '2023-09-06', FALSE, 'Kiểm tra từ tính', 'Nhiệt luyện sai', 'Không kiểm soát nhiệt độ', 'Nhiệt luyện', 'system'),
    ('DF007', 'Sai độ nhám', 1, '2023-09-07', TRUE, 'Khách hàng phản hồi', 'Thông số sai', 'Thiếu kiểm tra cuối line', 'Tiện', 'system'),
    ('DF008', 'Ren bị mòn', 1, '2023-09-08', FALSE, 'Dao ren mòn', 'Dao quá tuổi thọ', 'Không thay dao định kỳ', 'Tiện ren', 'system'),
    ('DF009', 'Sai kích thước then', 1, '2023-09-09', FALSE, 'Sai bản vẽ', 'Cập nhật nhầm version', 'Không review bản vẽ', 'Phay', 'system'),
    ('DF010', 'Trầy xước nội bộ', 1, '2023-09-10', TRUE, 'Vận chuyển nội bộ', 'Không có khay đựng', 'Không kiểm tra packaging', 'Vận chuyển', 'system'),

    ('DF011', 'Sai lực siết vít', 1, '2023-09-11', FALSE, 'Torque sai', 'Chưa calibrate', 'Không kiểm tra torque định kỳ', 'Lắp ráp', 'system'),
    ('DF012', 'Sai vị trí rãnh', 1, '2023-09-12', FALSE, 'Lệch dao', 'Set dao sai', 'Không kiểm tra first piece', 'Phay', 'system'),
    ('DF013', 'Sai chiều dài tổng', 1, '2023-09-13', TRUE, 'Lọt QC', 'Offset sai', 'Không kiểm tra 3 mẫu đầu', 'Tiện', 'system'),
    ('DF014', 'Rỗ bề mặt', 1, '2023-09-14', FALSE, 'Vật liệu lỗi', 'Nguyên liệu kém', 'Không kiểm incoming', 'Tiện', 'system'),
    ('DF015', 'Cong vênh chi tiết', 1, '2023-09-15', TRUE, 'Biến dạng', 'Nhiệt luyện sai', 'Không kiểm nhiệt độ', 'Nhiệt luyện', 'system'),

    ('DF016', 'Tràn keo terminal', 2, '2023-10-01', FALSE, 'Keo quá mức', 'Cài đặt sai', 'Không hiệu chỉnh đầu ca', 'Bơm keo', 'system'),
    ('DF017', 'Thiếu lượng keo', 2, '2023-10-02', TRUE, 'Test fail', 'Bơm không ổn định', 'Không kiểm tra định lượng', 'Bơm keo', 'system'),
    ('DF018', 'Sai cực linh kiện', 2, '2023-10-03', FALSE, 'Lắp ngược', 'Thiếu đào tạo', 'Không kiểm tra visual', 'Lắp ráp', 'system'),
    ('DF019', 'Hở mối hàn', 2, '2023-10-04', TRUE, 'AOI bỏ sót', 'Nhiệt hàn thấp', 'Không kiểm nhiệt độ', 'Hàn', 'system'),
    ('DF020', 'Cháy linh kiện', 2, '2023-10-05', TRUE, 'Sai điện áp', 'Cài đặt máy sai', 'Không kiểm setup', 'Test điện', 'system'),

    ('DF021', 'Lệch vị trí bắt vít', 2, '2023-10-06', FALSE, 'Template sai', 'Không cố định jig', 'Không kiểm tra đầu ca', 'Lắp ráp', 'system'),
    ('DF022', 'Bong keo test nhiệt', 2, '2023-10-07', TRUE, 'Khách hàng trả về', 'Keo kém chất lượng', 'Không kiểm vật liệu đầu vào', 'Bơm keo', 'system'),
    ('DF023', 'Thiếu linh kiện nhỏ', 2, '2023-10-08', FALSE, 'Sót linh kiện', 'Không check BOM', 'Không kiểm final', 'Lắp ráp', 'system'),
    ('DF024', 'Nứt chân linh kiện', 2, '2023-10-09', FALSE, 'Va chạm', 'Handling sai', 'Không đào tạo thao tác', 'Lắp ráp', 'system'),
    ('DF025', 'Sai barcode', 2, '2023-10-10', TRUE, 'In sai mã', 'Template lỗi', 'Không review file in', 'In nhãn', 'system'),

    ('DF026', 'Chạm chập mạch', 2, '2023-10-11', TRUE, 'Khách hàng phản hồi', 'Hàn dư thiếc', 'Không kiểm AOI', 'Hàn', 'system'),
    ('DF027', 'Sai thông số điện trở', 2, '2023-10-12', FALSE, 'Chọn nhầm part', 'Thiếu xác nhận linh kiện', 'Không check BOM', 'Lắp ráp', 'system'),
    ('DF028', 'Bể vỏ khi ép', 2, '2023-10-13', TRUE, 'Lực ép lớn', 'Cài đặt sai', 'Không kiểm lực ép', 'Ép vỏ', 'system'),
    ('DF029', 'Hở gioăng', 2, '2023-10-14', FALSE, 'Lắp lệch', 'Thiếu guide', 'Không kiểm cuối line', 'Lắp ráp', 'system'),
    ('DF030', 'Sai thứ tự lắp ráp', 2, '2023-10-15', TRUE, 'Lọt QC', 'Không theo WI', 'Không giám sát', 'Assembly', 'system');
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
INSERT INTO training_samples (
    id,
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
    created_by
)
VALUES
    (1, 2, 1, 1, 'Lỗi Ngoại Quan - Xước Mẻ',
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
     'Sau khi ép ron phải kiểm tra độ kín sơ bộ bằng mắt trước khi chuyển công đoạn.',
     1, 'Mẫu chuẩn #09', 'TS0029', 1, 3, 3, 'Rà soát trước test', 'system'),

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
-- PART 5: TRAINING PLAN & RESULTS (Kế hoạch & Kết quả huấn luyện)
-- ============================================================================
-- Plan 1: APPROVED  — team 1, line 1 (Tiện CNC), tl_prod01 — có 2 versions history
-- Plan 2: DRAFT     — team 1, line 1 (Tiện CNC), tl_prod01
-- Plan 3: WAITING_SV — team 2, line 2 (Phay CNC), tl_prod02
-- Plan 4: REJECTED_BY_SV — team 3, line 3 (Lắp Ráp), tl_prod03
-- Plan 5: APPROVED  — team 2, line 2 (Phay CNC), tl_prod02 — hoàn thành hết

INSERT INTO training_plans (id, form_code, title, month_start, month_end, team_id, line_id, status, current_version, note, created_by)
VALUES
(1, 'TR_PLAN_TIEN_001', 'Kế hoạch huấn luyện T3/2026 - Line Tiện CNC',
 '2026-03-01', '2026-03-31', 1, 1, 'APPROVED', 2,
 'Đã duyệt. NV001 có 2 lần thêm (2 batch khác nhau).', 'tl_prod01'),
(2, 'TR_PLAN_TIEN_002', 'Kế hoạch huấn luyện T4/2026 - Line Tiện CNC',
 '2026-04-01', '2026-04-30', 1, 1, 'DRAFT', 1,
 'Đang soạn thảo, chưa submit.', 'tl_prod01'),
(3, 'TR_PLAN_PHAY_001', 'Kế hoạch huấn luyện T3/2026 - Line Phay CNC',
 '2026-03-01', '2026-03-31', 2, 2, 'WAITING_SV', 1,
 'Đã submit, chờ Supervisor duyệt.', 'tl_prod02'),
(4, 'TR_PLAN_LAP_001', 'Kế hoạch huấn luyện T3/2026 - Line Lắp Ráp',
 '2026-03-01', '2026-03-31', 3, 3, 'REJECTED_BY_SV', 1,
 'Bị SV trả lại vì thiếu lịch cho NV007.', 'tl_prod03'),
(5, 'TR_PLAN_PHAY_002', 'Kế hoạch huấn luyện T2/2026 - Line Phay CNC',
 '2026-02-01', '2026-02-28', 2, 2, 'APPROVED', 1,
 'Tháng 2, đã duyệt và hoàn thành.', 'tl_prod02');

-- ===== PLAN 1 DETAILS (APPROVED) — team 1, employees 1,2,3 =====
-- NV001 lần 1 (batch): 3 ngày
-- NV001 lần 2 (batch khác): 2 ngày → row khác trên FE
-- NV002: 2 ngày
-- NV003: 2 ngày (1 MISSED)
INSERT INTO training_plan_details (id, training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date, status, note, created_by)
VALUES
(1,  1, 1, 'batch-p1-nv001-1', '2026-03-01', '2026-03-05', '2026-03-05', 'DONE',    'NV001 lần 1 - ngày 5',  'tl_prod01'),
(2,  1, 1, 'batch-p1-nv001-1', '2026-03-01', '2026-03-12', '2026-03-12', 'DONE',    'NV001 lần 1 - ngày 12', 'tl_prod01'),
(3,  1, 1, 'batch-p1-nv001-1', '2026-03-01', '2026-03-19', NULL,         'PENDING', 'NV001 lần 1 - ngày 19', 'tl_prod01'),
(4,  1, 1, 'batch-p1-nv001-2', '2026-03-01', '2026-03-22', NULL,         'PENDING', 'NV001 lần 2 - ngày 22', 'tl_prod01'),
(5,  1, 1, 'batch-p1-nv001-2', '2026-03-01', '2026-03-28', NULL,         'PENDING', 'NV001 lần 2 - ngày 28', 'tl_prod01'),
(6,  1, 2, 'batch-p1-nv002-1', '2026-03-01', '2026-03-06', '2026-03-06', 'DONE',    'NV002 - ngày 6',        'tl_prod01'),
(7,  1, 2, 'batch-p1-nv002-1', '2026-03-01', '2026-03-20', NULL,         'PENDING', 'NV002 - ngày 20',       'tl_prod01'),
(8,  1, 3, 'batch-p1-nv003-1', '2026-03-01', '2026-03-03', NULL,         'MISSED',  '[Đã nghỉ] NV003 ngày 3','tl_prod01'),
(9,  1, 3, 'batch-p1-nv003-1', '2026-03-01', '2026-03-15', NULL,         'PENDING', 'NV003 - ngày 15',       'tl_prod01');

-- ===== PLAN 2 DETAILS (DRAFT) — team 1, employees 1,2,3 =====
INSERT INTO training_plan_details (id, training_plan_id, employee_id, batch_id, target_month, planned_date, status, note, created_by)
VALUES
(10, 2, 1, 'batch-p2-nv001-1', '2026-04-01', '2026-04-05', 'PENDING', 'NV001 - ngày 5/4',  'tl_prod01'),
(11, 2, 1, 'batch-p2-nv001-1', '2026-04-01', '2026-04-15', 'PENDING', 'NV001 - ngày 15/4', 'tl_prod01'),
(12, 2, 2, 'batch-p2-nv002-1', '2026-04-01', '2026-04-10', 'PENDING', 'NV002 - ngày 10/4', 'tl_prod01'),
(13, 2, 3, 'batch-p2-nv003-1', '2026-04-01', '2026-04-08', 'PENDING', 'NV003 - ngày 8/4',  'tl_prod01'),
(14, 2, 3, 'batch-p2-nv003-1', '2026-04-01', '2026-04-22', 'PENDING', 'NV003 - ngày 22/4', 'tl_prod01');

-- ===== PLAN 3 DETAILS (WAITING_SV) — team 2, employees 4,5 =====
INSERT INTO training_plan_details (id, training_plan_id, employee_id, batch_id, target_month, planned_date, status, note, created_by)
VALUES
(15, 3, 4, 'batch-p3-nv004-1', '2026-03-01', '2026-03-07', 'PENDING', 'NV004 - ngày 7',  'tl_prod02'),
(16, 3, 4, 'batch-p3-nv004-1', '2026-03-01', '2026-03-14', 'PENDING', 'NV004 - ngày 14', 'tl_prod02'),
(17, 3, 4, 'batch-p3-nv004-1', '2026-03-01', '2026-03-21', 'PENDING', 'NV004 - ngày 21', 'tl_prod02'),
(18, 3, 5, 'batch-p3-nv005-1', '2026-03-01', '2026-03-10', 'PENDING', 'NV005 - ngày 10', 'tl_prod02'),
(19, 3, 5, 'batch-p3-nv005-1', '2026-03-01', '2026-03-24', 'PENDING', 'NV005 - ngày 24', 'tl_prod02');

-- ===== PLAN 4 DETAILS (REJECTED) — team 3, employee 7 =====
INSERT INTO training_plan_details (id, training_plan_id, employee_id, batch_id, target_month, planned_date, status, note, created_by)
VALUES
(20, 4, 7, 'batch-p4-nv007-1', '2026-03-01', '2026-03-10', 'PENDING', 'NV007 - ngày 10', 'tl_prod03'),
(21, 4, 7, 'batch-p4-nv007-1', '2026-03-01', '2026-03-20', 'PENDING', 'NV007 - ngày 20', 'tl_prod03');

-- ===== PLAN 5 DETAILS (APPROVED, hoàn thành) — team 2, employees 4,5 =====
INSERT INTO training_plan_details (id, training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date, status, note, created_by)
VALUES
(22, 5, 4, 'batch-p5-nv004-1', '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV004 - ngày 5/2',  'tl_prod02'),
(23, 5, 4, 'batch-p5-nv004-1', '2026-02-01', '2026-02-18', '2026-02-18', 'DONE', 'NV004 - ngày 18/2', 'tl_prod02'),
(24, 5, 5, 'batch-p5-nv005-1', '2026-02-01', '2026-02-10', '2026-02-10', 'DONE', 'NV005 - ngày 10/2', 'tl_prod02'),
(25, 5, 5, 'batch-p5-nv005-1', '2026-02-01', '2026-02-25', '2026-02-25', 'DONE', 'NV005 - ngày 25/2', 'tl_prod02');

-- ===== TRAINING PLAN HISTORY (Snapshot cho APPROVED plans) =====
-- Plan 1: version 1 (gốc), version 2 (sau reschedule)
-- Plan 5: version 1
INSERT INTO training_plan_history (id, training_plan_id, title, version, form_code, month_start, month_end, team_id, line_id, note, recorded_at, created_by)
VALUES
(1, 1, 'Kế hoạch huấn luyện T3/2026 - Line Tiện CNC', 1, 'TR_PLAN_TIEN_001',
 '2026-03-01', '2026-03-31', 1, 1, 'Bản gốc trước reschedule', '2026-03-01 08:00:00', 'tl_prod01'),
(2, 1, 'Kế hoạch huấn luyện T3/2026 - Line Tiện CNC', 2, 'TR_PLAN_TIEN_001',
 '2026-03-01', '2026-03-31', 1, 1, 'Sau reschedule: NV003 dời, NV001 thêm lần 2', '2026-03-04 10:30:00', 'tl_prod01'),
(3, 5, 'Kế hoạch huấn luyện T2/2026 - Line Phay CNC', 1, 'TR_PLAN_PHAY_002',
 '2026-02-01', '2026-02-28', 2, 2, 'Bản gốc, hoàn thành', '2026-02-01 08:00:00', 'tl_prod02');

-- ===== PLAN 1 DETAIL HISTORY — Version 1 (bản gốc, NV003 lên lịch ngày 3 và 10) =====
INSERT INTO training_plan_detail_history (id, training_plan_history_id, batch_id, employee_id, target_month, planned_date, actual_date, status, note, created_by)
VALUES
(1, 1, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-05', NULL, 'PENDING', 'NV001 lần 1 - ngày 5',  'tl_prod01'),
(2, 1, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-12', NULL, 'PENDING', 'NV001 lần 1 - ngày 12', 'tl_prod01'),
(3, 1, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-19', NULL, 'PENDING', 'NV001 lần 1 - ngày 19', 'tl_prod01'),
(4, 1, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-06', NULL, 'PENDING', 'NV002 - ngày 6',        'tl_prod01'),
(5, 1, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-20', NULL, 'PENDING', 'NV002 - ngày 20',       'tl_prod01'),
(6, 1, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-03', NULL, 'PENDING', 'NV003 - ngày 3 (gốc)',  'tl_prod01'),
(7, 1, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-10', NULL, 'PENDING', 'NV003 - ngày 10 (gốc)', 'tl_prod01');

-- ===== PLAN 1 DETAIL HISTORY — Version 2 (sau reschedule) =====
INSERT INTO training_plan_detail_history (id, training_plan_history_id, batch_id, employee_id, target_month, planned_date, actual_date, status, note, created_by)
VALUES
(8,  2, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-05', '2026-03-05', 'DONE',    'NV001 lần 1 - ngày 5',  'tl_prod01'),
(9,  2, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-12', '2026-03-12', 'DONE',    'NV001 lần 1 - ngày 12', 'tl_prod01'),
(10, 2, 'batch-p1-nv001-1', 1, '2026-03-01', '2026-03-19', NULL,         'PENDING', 'NV001 lần 1 - ngày 19', 'tl_prod01'),
(11, 2, 'batch-p1-nv001-2', 1, '2026-03-01', '2026-03-22', NULL,         'PENDING', 'NV001 lần 2 - ngày 22', 'tl_prod01'),
(12, 2, 'batch-p1-nv001-2', 1, '2026-03-01', '2026-03-28', NULL,         'PENDING', 'NV001 lần 2 - ngày 28', 'tl_prod01'),
(13, 2, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-06', '2026-03-06', 'DONE',    'NV002 - ngày 6',        'tl_prod01'),
(14, 2, 'batch-p1-nv002-1', 2, '2026-03-01', '2026-03-20', NULL,         'PENDING', 'NV002 - ngày 20',       'tl_prod01'),
(15, 2, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-03', NULL,         'MISSED',  '[Đã nghỉ] NV003 ngày 3','tl_prod01'),
(16, 2, 'batch-p1-nv003-1', 3, '2026-03-01', '2026-03-15', NULL,         'PENDING', 'NV003 - ngày 15 (dời)', 'tl_prod01');

-- ===== PLAN 5 DETAIL HISTORY — Version 1 =====
INSERT INTO training_plan_detail_history (id, training_plan_history_id, batch_id, employee_id, target_month, planned_date, actual_date, status, note, created_by)
VALUES
(17, 3, 'batch-p5-nv004-1', 4, '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV004 - ngày 5/2',  'tl_prod02'),
(18, 3, 'batch-p5-nv004-1', 4, '2026-02-01', '2026-02-18', '2026-02-18', 'DONE', 'NV004 - ngày 18/2', 'tl_prod02'),
(19, 3, 'batch-p5-nv005-1', 5, '2026-02-01', '2026-02-10', '2026-02-10', 'DONE', 'NV005 - ngày 10/2', 'tl_prod02'),
(20, 3, 'batch-p5-nv005-1', 5, '2026-02-01', '2026-02-25', '2026-02-25', 'DONE', 'NV005 - ngày 25/2', 'tl_prod02');

-- ===== TRAINING RESULTS =====
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version, note, created_by)
VALUES
(1, 1, 'Kết quả huấn luyện T3/2026 - Line Tiện', 'TR_RESULT_TIEN_001', 2026, 1, 1, 'ON_GOING', 1, 'Đang ghi nhận', 'tl_prod01'),
(2, 5, 'Kết quả huấn luyện T2/2026 - Line Phay', 'TR_RESULT_PHAY_002', 2026, 2, 2, 'APPROVED', 1, 'Hoàn thành',    'tl_prod02');

-- ===== TRAINING RESULT DETAILS — Plan 1 =====
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, process_id,
    training_sample_id, product_id, classification, cycle_time_standard,
    planned_date, actual_date, time_in, time_start_op, time_out, status,
    detection_time, is_pass, note, is_retrained,
    signature_pro_in, signature_fi_in, signature_pro_out, signature_fi_out, created_by)
VALUES
-- NV001 lần 1 ngày 5: DONE
(1, 1, 1, 1, 2, 1, 2, 15.00,
 '2026-03-05','2026-03-05','08:00:00','08:03:00','08:18:00','APPROVED',14,TRUE,'Thao tác OP10 tốt',FALSE,5,8,5,8,'tl_prod01'),
-- NV001 lần 1 ngày 12: DONE
(1, 2, 1, 2, 1, 1, 1, 20.00,
 '2026-03-12','2026-03-12','09:00:00','09:02:00','09:22:00','APPROVED',19,TRUE,'Soi đèn đúng góc',FALSE,5,8,5,8,'tl_prod01'),
-- NV001 lần 1 ngày 19: PENDING
(1, 3, 1, 1, NULL, NULL, NULL, NULL,
 '2026-03-19',NULL,NULL,NULL,NULL,'PENDING',NULL,NULL,'Chờ ngày 19/3',FALSE,NULL,NULL,NULL,NULL,'tl_prod01'),
-- NV001 lần 2 ngày 22: PENDING
(1, 4, 1, 2, NULL, NULL, NULL, NULL,
 '2026-03-22',NULL,NULL,NULL,NULL,'PENDING',NULL,NULL,'Chờ ngày 22/3',FALSE,NULL,NULL,NULL,NULL,'tl_prod01'),
-- NV001 lần 2 ngày 28: PENDING
(1, 5, 1, 1, NULL, NULL, NULL, NULL,
 '2026-03-28',NULL,NULL,NULL,NULL,'PENDING',NULL,NULL,'Chờ ngày 28/3',FALSE,NULL,NULL,NULL,NULL,'tl_prod01'),
-- NV002 ngày 6: DONE
(1, 6, 2, 1, 2, 1, 2, 15.00,
 '2026-03-06','2026-03-06','08:30:00','08:33:00','08:48:00','APPROVED',13,TRUE,'NV002 chuẩn',FALSE,5,8,5,8,'tl_prod01'),
-- NV002 ngày 20: PENDING
(1, 7, 2, 1, NULL, NULL, NULL, NULL,
 '2026-03-20',NULL,NULL,NULL,NULL,'PENDING',NULL,NULL,'Chờ ngày 20/3',FALSE,NULL,NULL,NULL,NULL,'tl_prod01'),
-- NV003 ngày 15: PENDING
(1, 9, 3, 1, NULL, NULL, NULL, NULL,
 '2026-03-15',NULL,NULL,NULL,NULL,'PENDING',NULL,NULL,'Chờ ngày 15/3',FALSE,NULL,NULL,NULL,NULL,'tl_prod01');

-- ===== TRAINING RESULT DETAILS — Plan 5 (tất cả DONE) =====
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, process_id,
    training_sample_id, product_id, classification, cycle_time_standard,
    planned_date, actual_date, time_in, time_start_op, time_out, status,
    detection_time, is_pass, note, is_retrained,
    signature_pro_in, signature_fi_in, signature_pro_out, signature_fi_out, created_by)
VALUES
(2,22,4,3,NULL,2,3,18.00,'2026-02-05','2026-02-05','08:00:00','08:02:00','08:20:00','APPROVED',16,TRUE,'NV004 OK',FALSE,6,9,6,9,'tl_prod02'),
(2,23,4,3,NULL,2,3,18.00,'2026-02-18','2026-02-18','09:00:00','09:03:00','09:21:00','APPROVED',17,TRUE,'NV004 lần 2',FALSE,6,9,6,9,'tl_prod02'),
(2,24,5,3,NULL,2,3,18.00,'2026-02-10','2026-02-10','10:00:00','10:02:00','10:20:00','APPROVED',15,TRUE,'NV005 tốt',FALSE,6,9,6,9,'tl_prod02'),
(2,25,5,3,NULL,2,3,18.00,'2026-02-25','2026-02-25','08:30:00','08:32:00','08:50:00','APPROVED',16,TRUE,'NV005 lần 2',FALSE,6,9,6,9,'tl_prod02');


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