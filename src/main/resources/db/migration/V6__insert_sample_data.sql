-- ============================================================================
-- V8__fake_data_full.sql
-- Thay thế V2 (sample data) + V4 (RBAC seed)
-- Dữ liệu đầy đủ, đa dạng, phong phú hơn V2+V4 cộng lại
-- Priority Policy được thiết kế cho entity_type = PROCESS
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- TRUNCATE (đúng thứ tự FK ngược)
-- ============================================================================
TRUNCATE TABLE priority_snapshot_details;
TRUNCATE TABLE priority_snapshots;
TRUNCATE TABLE metric_classifications;
TRUNCATE TABLE priority_tier_filters;
TRUNCATE TABLE priority_tiers;
TRUNCATE TABLE priority_policies;
TRUNCATE TABLE computed_metrics;

TRUNCATE TABLE approval_actions;
TRUNCATE TABLE approval_flow_steps;

TRUNCATE TABLE training_sample_reviews;
TRUNCATE TABLE training_sample_review_configs;

TRUNCATE TABLE training_result_detail_history;
TRUNCATE TABLE training_result_history;
TRUNCATE TABLE training_result_details;
TRUNCATE TABLE training_results;

TRUNCATE TABLE training_plan_detail_history;
TRUNCATE TABLE training_plan_history;
TRUNCATE TABLE training_plan_details;
TRUNCATE TABLE training_plans;

TRUNCATE TABLE training_sample_proposal_detail_history;
TRUNCATE TABLE training_sample_proposal_history;
TRUNCATE TABLE training_sample_proposal_details;
TRUNCATE TABLE training_sample_proposals;
TRUNCATE TABLE training_samples;

TRUNCATE TABLE defect_proposal_detail_history;
TRUNCATE TABLE defect_proposal_history;
TRUNCATE TABLE defect_proposal_details;
TRUNCATE TABLE defect_proposals;
TRUNCATE TABLE defects;

TRUNCATE TABLE product_process;
TRUNCATE TABLE products;
TRUNCATE TABLE processes;
TRUNCATE TABLE product_lines;
TRUNCATE TABLE employee_skills;
TRUNCATE TABLE employees;
TRUNCATE TABLE teams;
TRUNCATE TABLE `groups`;
TRUNCATE TABLE sections;

TRUNCATE TABLE import_histories;
TRUNCATE TABLE attachments;

TRUNCATE TABLE role_permissions;
TRUNCATE TABLE user_roles;
TRUNCATE TABLE permissions;
TRUNCATE TABLE modules;
TRUNCATE TABLE roles;
TRUNCATE TABLE refresh_tokens;
TRUNCATE TABLE users;

-- ============================================================================
-- PART 1: USERS & ROLES
-- Password: Password@123 (BCrypt encoded)
-- ============================================================================

INSERT INTO users (id, username, email, password_hash, full_name, is_active, created_by, employee_code)
VALUES (1, 'admin', 'admin@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Quản Trị Hệ Thống', TRUE, 'system', 'SYS000'),
       (2, 'nguyen.quanly', 'nguyen.ql@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Nguyễn Văn Quản Lý', TRUE, 'system', 'MGR001'),
       (3, 'tran.giamsat1', 'tran.gs1@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Trần Thị Giám Sát', TRUE, 'system', 'SV001'),
       (4, 'le.giamsat2', 'le.gs2@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Lê Văn Giám Sát', TRUE, 'system', 'SV002'),
       (5, 'pham.giamsat3', 'pham.gs3@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Phạm Thị Giám Sát', TRUE, 'system', 'SV003'),
       (6, 'tl_tien01', 'tl_tien01@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Hoàng Văn Trưởng Tổ Tiện', TRUE, 'system', 'TL001'),
       (7, 'tl_phay01', 'tl_phay01@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Võ Thị Trưởng Tổ Phay', TRUE, 'system', 'TL002'),
       (8, 'tl_laprap01', 'tl_laprap01@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Đặng Văn Trưởng Tổ Lắp', TRUE, 'system', 'TL003'),
       (9, 'tl_dongco01', 'tl_dongco01@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Bùi Thị Trưởng Tổ ĐC', TRUE, 'system', 'TL004'),
       (10, 'tl_hanlap01', 'tl_hanlap01@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Ngô Văn Trưởng Tổ Hàn', TRUE, 'system', 'TL005'),
       (11, 'tl_kcs01', 'tl_kcs01@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Đinh Thị Trưởng KCS', TRUE, 'system', 'TL006'),
       (12, 'fi_user01', 'fi01@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Trịnh Văn Kiểm Tra Cuối', TRUE, 'system', 'FI001'),
       (13, 'fi_user02', 'fi02@dmvn.com', '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'Lý Thị Kiểm Tra Cuối', TRUE, 'system', 'FI002');

INSERT INTO roles (id, role_code, display_name, description, is_system, is_active, created_by)
VALUES (1, 'ROLE_ADMIN', 'Quản trị', 'Quyền truy cập toàn hệ thống', TRUE, TRUE, 'system'),
       (2, 'ROLE_MANAGER', 'Quản lý', 'Phê duyệt cấp quản lý', TRUE, TRUE, 'system'),
       (3, 'ROLE_SUPERVISOR', 'Giám sát', 'Phê duyệt cấp giám sát', TRUE, TRUE, 'system'),
       (4, 'ROLE_TEAM_LEADER', 'Trưởng tổ', 'Khởi tạo và nhập liệu', TRUE, TRUE, 'system'),
       (5, 'ROLE_FINAL_INSPECTION', 'KCS Cuối', 'Kiểm tra cuối dây chuyền', TRUE, TRUE, 'system');

INSERT INTO user_roles (user_id, role_id)
VALUES (1, 1),
       (2, 2),
       (3, 3),
       (4, 3),
       (5, 3),
       (6, 4),
       (7, 4),
       (8, 4),
       (9, 4),
       (10, 4),
       (11, 4),
       (12, 5),
       (13, 5);

-- ============================================================================
-- PART 2: MODULES & PERMISSIONS (từ V4)
-- ============================================================================

INSERT INTO modules (id, module_code, display_name, description, sort_order, created_by)
VALUES (1, 'defect_report', 'Báo cáo lỗi sản phẩm', 'Quản lý báo cáo lỗi', 1, 'system'),
       (2, 'training_sample', 'Chủ đề đào tạo', 'Quản lý chủ đề đào tạo', 2, 'system'),
       (3, 'training_plan', 'Kế hoạch đào tạo', 'Quản lý kế hoạch đào tạo', 3, 'system'),
       (4, 'training_result', 'Kết quả đào tạo', 'Quản lý kết quả đào tạo', 4, 'system'),
       (5, 'employee', 'Nhân viên', 'Quản lý thông tin nhân viên', 5, 'system'),
       (6, 'user', 'Tài khoản người dùng', 'Quản lý tài khoản hệ thống', 6, 'system'),
       (7, 'role', 'Vai trò & Phân quyền', 'Quản lý vai trò và phân quyền', 7, 'system'),
       (8, 'master_data', 'Dữ liệu danh mục', 'Quản lý dữ liệu danh mục', 8, 'system'),
       (9, 'scoring', 'Chấm điểm ưu tiên', 'Quản lý chính sách chấm điểm', 9, 'system'),
       (10, 'dashboard', 'Dashboard & Báo cáo', 'Xem dashboard và báo cáo tổng hợp', 10, 'system'),
       (11, 'system', 'Cài đặt hệ thống', 'Cài đặt và cấu hình hệ thống', 11, 'system'),
       (12, 'staff_organization', 'Tổ chức nhân sự', 'Quản lí danh sách nhân sự công ty', 12, 'system'),
       (13, 'manufacturing_line', 'Tổ chức sản xuất', 'Quản lí danh sách tổ chức dây chuyền', 13, 'system'),
       (14, 'attachment', 'Quản lý File & Ảnh', 'Upload và xóa tệp MinIO', 14, 'system'),
       (15, 'action_item', 'Việc cần làm', 'Quản lý các mục cần xử lý', 15, 'system');

INSERT INTO permissions (id, permission_code, display_name, module_id, action, sort_order, is_system, created_by)
VALUES (1, 'defect_proposal.view', 'Xem báo cáo lỗi', 1, 'view', 1, TRUE, 'system'),
       (2, 'defect_proposal.create', 'Tạo báo cáo lỗi', 1, 'create', 2, TRUE, 'system'),
       (3, 'defect_proposal.edit', 'Sửa báo cáo lỗi', 1, 'edit', 3, TRUE, 'system'),
       (4, 'defect_proposal.delete', 'Xoá báo cáo lỗi', 1, 'delete', 4, TRUE, 'system'),
       (5, 'defect_proposal.approve', 'Phê duyệt báo cáo lỗi', 1, 'approve', 5, TRUE, 'system'),
       (6, 'training_sample_proposal.view', 'Xem chủ đề đào tạo', 2, 'view', 1, TRUE, 'system'),
       (7, 'training_sample_proposal.create', 'Tạo chủ đề đào tạo', 2, 'create', 2, TRUE, 'system'),
       (8, 'training_sample_proposal.edit', 'Sửa chủ đề đào tạo', 2, 'edit', 3, TRUE, 'system'),
       (9, 'training_sample_proposal.delete', 'Xoá chủ đề đào tạo', 2, 'delete', 4, TRUE, 'system'),
       (10, 'training_plan.view', 'Xem kế hoạch đào tạo', 3, 'view', 1, TRUE, 'system'),
       (11, 'training_plan.create', 'Tạo kế hoạch đào tạo', 3, 'create', 2, TRUE, 'system'),
       (12, 'training_plan.edit', 'Sửa kế hoạch đào tạo', 3, 'edit', 3, TRUE, 'system'),
       (13, 'training_plan.delete', 'Xoá kế hoạch đào tạo', 3, 'delete', 4, TRUE, 'system'),
       (14, 'training_plan.approve', 'Phê duyệt kế hoạch đào tạo', 3, 'approve', 5, TRUE, 'system'),
       (15, 'training_result.view', 'Xem kết quả đào tạo', 4, 'view', 1, TRUE, 'system'),
       (16, 'training_result.edit', 'Cập nhật kết quả đào tạo', 4, 'edit', 2, TRUE, 'system'),
       (17, 'training_result.approve', 'Phê duyệt kết quả đào tạo', 4, 'approve', 3, TRUE, 'system'),
       (18, 'employee.view', 'Xem thông tin nhân viên', 5, 'view', 1, TRUE, 'system'),
       (19, 'employee.create', 'Thêm nhân viên', 5, 'create', 2, TRUE, 'system'),
       (20, 'employee.edit', 'Sửa thông tin nhân viên', 5, 'edit', 3, TRUE, 'system'),
       (21, 'employee.delete', 'Xoá nhân viên', 5, 'delete', 4, TRUE, 'system'),
       (22, 'user.view', 'Xem tài khoản người dùng', 6, 'view', 1, TRUE, 'system'),
       (23, 'user.create', 'Tạo tài khoản người dùng', 6, 'create', 2, TRUE, 'system'),
       (24, 'user.edit', 'Sửa tài khoản người dùng', 6, 'edit', 3, TRUE, 'system'),
       (25, 'user.delete', 'Xoá tài khoản người dùng', 6, 'delete', 4, TRUE, 'system'),
       (26, 'user.assign_role', 'Gán vai trò cho người dùng', 6, 'assign_role', 5, TRUE, 'system'),
       (27, 'role.view', 'Xem vai trò', 7, 'view', 1, TRUE, 'system'),
       (28, 'role.create', 'Tạo vai trò', 7, 'create', 2, TRUE, 'system'),
       (29, 'role.edit', 'Sửa vai trò', 7, 'edit', 3, TRUE, 'system'),
       (30, 'role.delete', 'Xoá vai trò', 7, 'delete', 4, TRUE, 'system'),
       (31, 'role.assign_permission', 'Gán quyền cho vai trò', 7, 'assign_permission', 5, TRUE, 'system'),
       (32, 'master_data.view', 'Xem dữ liệu danh mục', 8, 'view', 1, TRUE, 'system'),
       (33, 'master_data.create', 'Thêm dữ liệu danh mục', 8, 'create', 2, TRUE, 'system'),
       (34, 'master_data.edit', 'Sửa dữ liệu danh mục', 8, 'edit', 3, TRUE, 'system'),
       (35, 'master_data.delete', 'Xoá dữ liệu danh mục', 8, 'delete', 4, TRUE, 'system'),
       (36, 'scoring.view', 'Xem chính sách chấm điểm', 9, 'view', 1, TRUE, 'system'),
       (37, 'scoring.create', 'Tạo chính sách chấm điểm', 9, 'create', 2, TRUE, 'system'),
       (38, 'scoring.edit', 'Sửa chính sách chấm điểm', 9, 'edit', 3, TRUE, 'system'),
       (39, 'scoring.delete', 'Xoá chính sách chấm điểm', 9, 'delete', 4, TRUE, 'system'),
       (40, 'dashboard.view', 'Xem dashboard', 10, 'view', 1, TRUE, 'system'),
       (41, 'dashboard.export', 'Xuất báo cáo', 10, 'export', 2, TRUE, 'system'),
       (42, 'system.config', 'Cấu hình hệ thống', 11, 'config', 1, TRUE, 'system'),
       (43, 'staff_organization.view', 'Xem cấu trúc nhân sự', 12, 'view', 1, TRUE, 'system'),
       (44, 'staff_organization.create', 'Tạo cấu trúc nhân sự', 12, 'create', 2, TRUE, 'system'),
       (45, 'staff_organization.edit', 'Sửa cấu trúc nhân sự', 12, 'edit', 3, TRUE, 'system'),
       (46, 'staff_organization.delete', 'Xoá cấu trúc nhân sự', 12, 'delete', 4, TRUE, 'system'),
       (47, 'manufacturing_line.view', 'Xem cấu trúc dây chuyền', 13, 'view', 1, TRUE, 'system'),
       (48, 'manufacturing_line.create', 'Tạo cấu trúc dây chuyền', 13, 'create', 2, TRUE, 'system'),
       (49, 'manufacturing_line.edit', 'Sửa cấu trúc dây chuyền', 13, 'edit', 3, TRUE, 'system'),
       (50, 'manufacturing_line.delete', 'Xoá cấu trúc dây chuyền', 13, 'delete', 4, TRUE, 'system'),
       (56, 'attachment.view', 'Xem file/ảnh đính kèm', 14, 'view', 1, TRUE, 'system'),
       (57, 'attachment.create', 'Upload file/ảnh mới', 14, 'create', 2, TRUE, 'system'),
       (58, 'attachment.delete', 'Xóa file/ảnh đính kèm', 14, 'delete', 3, TRUE, 'system'),
       (59, 'action_item.view', 'Xem các mục cần làm', 15, 'view', 1, TRUE, 'system'),
       (100, 'training_sample.view', 'Xem danh sách mẫu huấn luyện', 2, 'view', 5, TRUE, 'system'),
       (101, 'training_sample.detail', 'Xem chi tiết mẫu huấn luyện', 2, 'view', 6, TRUE, 'system'),
       (102, 'training_sample.import', 'Nhập danh sách mẫu huấn luyện', 2, 'create', 7, TRUE, 'system'),
       (111, 'defect.view', 'Xem danh sách lỗi quá khứ', 1, 'view', 6, TRUE, 'system'),
       (112, 'defect.detail', 'Xem chi tiết lỗi quá khứ', 1, 'view', 7, TRUE, 'system'),
       (113, 'defect.import', 'Nhập danh sách lỗi từ file', 1, 'create', 8, TRUE, 'system'),
       (200, 'manufacturing_line.import', 'Import dây chuyền từ file', 13, 'import', 5, TRUE, 'system');

-- ADMIN: tất cả quyền
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id
FROM permissions;

-- MANAGER
INSERT INTO role_permissions (role_id, permission_id)
VALUES (2, 1),
       (2, 2),
       (2, 3),
       (2, 5),
       (2, 6),
       (2, 7),
       (2, 8),
       (2, 10),
       (2, 11),
       (2, 12),
       (2, 14),
       (2, 15),
       (2, 16),
       (2, 17),
       (2, 18),
       (2, 19),
       (2, 20),
       (2, 22),
       (2, 27),
       (2, 36),
       (2, 37),
       (2, 38),
       (2, 40),
       (2, 41),
       (2, 43),
       (2, 44),
       (2, 45),
       (2, 46),
       (2, 56),
       (2, 57),
       (2, 58),
       (2, 59);

-- SUPERVISOR
INSERT INTO role_permissions (role_id, permission_id)
VALUES (3, 1),
       (3, 2),
       (3, 3),
       (3, 5),
       (3, 6),
       (3, 7),
       (3, 8),
       (3, 10),
       (3, 11),
       (3, 12),
       (3, 14),
       (3, 15),
       (3, 16),
       (3, 18),
       (3, 19),
       (3, 20),
       (3, 36),
       (3, 40),
       (3, 56),
       (3, 57),
       (3, 58),
       (3, 59);

-- TEAM LEADER
INSERT INTO role_permissions (role_id, permission_id)
VALUES (4, 1),
       (4, 2),
       (4, 3),
       (4, 4),
       (4, 5),
       (4, 6),
       (4, 7),
       (4, 8),
       (4, 9),
       (4, 10),
       (4, 11),
       (4, 12),
       (4, 13),
       (4, 14),
       (4, 15),
       (4, 16),
       (4, 17),
       (4, 40),
       (4, 47),
       (4, 56),
       (4, 57),
       (4, 58),
       (4, 59),
       (4, 100),
       (4, 101),
       (4, 102),
       (4, 111),
       (4, 112),
       (4, 113),
       (4, 200);

-- FINAL INSPECTION
INSERT INTO role_permissions (role_id, permission_id)
VALUES (5, 1),
       (5, 2),
       (5, 3),
       (5, 15),
       (5, 16),
       (5, 18),
       (5, 40),
       (5, 56),
       (5, 57),
       (5, 58),
       (5, 59);

-- ============================================================================
-- PART 3: ORGANIZATION STRUCTURE
-- 3 Xưởng, 6 Khu vực, 6 Tổ, 30 Nhân viên
-- ============================================================================

INSERT INTO sections (id, code, name, manager_id, created_by)
VALUES (1, 'SEC-CK', 'Xưởng Gia Công Cơ Khí', 2, 'admin'),
       (2, 'SEC-LA', 'Xưởng Lắp Ráp & Đóng Gói', 2, 'admin'),
       (3, 'SEC-QC', 'Xưởng Kiểm Định Chất Lượng', 2, 'admin');

INSERT INTO `groups` (id, section_id, name, supervisor_id, created_by)
VALUES (1, 1, 'Khu Vực Tiện CNC', 3, 'admin'),
       (2, 1, 'Khu Vực Phay CNC', 3, 'admin'),
       (3, 1, 'Khu Vực Hàn & Gia Công Nhiệt', 4, 'admin'),
       (4, 2, 'Khu Vực Lắp Ráp Máy Bơm', 4, 'admin'),
       (5, 2, 'Khu Vực Lắp Ráp Động Cơ', 5, 'admin'),
       (6, 3, 'Khu Vực KCS & Kiểm Tra Cuối', 5, 'admin');

INSERT INTO product_lines (id, code, name, group_id, created_by)
VALUES (1, 'PL-TIEN-P1', 'Dòng Máy Bơm Nước P-Series (Tiện)', 1, 'admin'),
       (2, 'PL-PHAY-H1', 'Dòng Bơm Thủy Lực H-Series (Phay)', 2, 'admin'),
       (3, 'PL-HAN-W1', 'Dòng Bơm Ly Tâm W-Series (Hàn)', 3, 'admin'),
       (4, 'PL-LA-E1', 'Dây Chuyền Lắp Ráp Động Cơ E-Series', 5, 'admin'),
       (5, 'PL-LA-B1', 'Dây Chuyền Lắp Ráp Máy Bơm B-Series', 4, 'admin');

INSERT INTO teams (id, group_id, name, team_leader_id, final_inspection_id, created_by)
VALUES (1, 1, 'Tổ Tiện Ca Ngày', 6, 12, 'admin'),
       (2, 2, 'Tổ Phay Ca Ngày', 7, 12, 'admin'),
       (3, 3, 'Tổ Hàn & Nhiệt Luyện', 10, 12, 'admin'),
       (4, 4, 'Tổ Lắp Ráp Bơm Ca Sáng', 8, 13, 'admin'),
       (5, 5, 'Tổ Lắp Ráp Động Cơ', 9, 13, 'admin'),
       (6, 6, 'Tổ KCS & Kiểm Cuối', 11, 13, 'admin');

INSERT INTO employees (id, employee_code, full_name, team_id, status, created_by)
VALUES
-- Tổ Tiện (team 1) - 6 người
(1, 'NV001', 'Nguyễn Văn An', 1, 'ACTIVE', 'admin'),
(2, 'NV002', 'Trần Thị Bình', 1, 'ACTIVE', 'admin'),
(3, 'NV003', 'Lê Văn Cường', 1, 'ACTIVE', 'admin'),
(4, 'NV004', 'Võ Thị Dung', 1, 'ACTIVE', 'admin'),
(5, 'NV005', 'Đặng Văn Em', 1, 'MATERNITY_LEAVE', 'admin'),
(6, 'NV006', 'Bùi Văn Phúc', 1, 'ACTIVE', 'admin'),
-- Tổ Phay (team 2) - 6 người
(7, 'NV007', 'Phạm Thị Giỏi', 2, 'ACTIVE', 'admin'),
(8, 'NV008', 'Hoàng Văn Hải', 2, 'ACTIVE', 'admin'),
(9, 'NV009', 'Ngô Thị Lan', 2, 'ACTIVE', 'admin'),
(10, 'NV010', 'Đinh Văn Khánh', 2, 'ACTIVE', 'admin'),
(11, 'NV011', 'Trương Thị Lan', 2, 'RESIGNED', 'admin'),
(12, 'NV012', 'Lưu Văn Minh', 2, 'ACTIVE', 'admin'),
-- Tổ Hàn (team 3) - 4 người
(13, 'NV013', 'Chu Thị Nga', 3, 'ACTIVE', 'admin'),
(14, 'NV014', 'Vũ Văn Oanh', 3, 'ACTIVE', 'admin'),
(15, 'NV015', 'Tô Thị Phương', 3, 'ACTIVE', 'admin'),
(16, 'NV016', 'Mai Văn Quang', 3, 'ACTIVE', 'admin'),
-- Tổ Lắp Ráp Bơm (team 4) - 5 người
(17, 'NV017', 'Hồ Thị Rồng', 4, 'ACTIVE', 'admin'),
(18, 'NV018', 'Lương Văn Sơn', 4, 'ACTIVE', 'admin'),
(19, 'NV019', 'Kiều Thị Thu', 4, 'ACTIVE', 'admin'),
(20, 'NV020', 'Trần Văn Thắng', 4, 'ACTIVE', 'admin'),
(21, 'NV021', 'Đỗ Thị Uyên', 4, 'MATERNITY_LEAVE', 'admin'),
-- Tổ Lắp Ráp Động Cơ (team 5) - 5 người
(22, 'NV022', 'Nguyễn Văn Vinh', 5, 'ACTIVE', 'admin'),
(23, 'NV023', 'Phan Thị Xuân', 5, 'ACTIVE', 'admin'),
(24, 'NV024', 'Lê Văn Yên', 5, 'ACTIVE', 'admin'),
(25, 'NV025', 'Hoàng Thị Yến', 5, 'ACTIVE', 'admin'),
(26, 'NV026', 'Trịnh Văn Tùng', 5, 'ACTIVE', 'admin'),
-- Tổ KCS (team 6) - 4 người
(27, 'NV027', 'Vương Thị Hà', 6, 'ACTIVE', 'admin'),
(28, 'NV028', 'Mạc Văn Bình', 6, 'ACTIVE', 'admin'),
(29, 'NV029', 'Tăng Thị Chi', 6, 'ACTIVE', 'admin'),
(30, 'NV030', 'Quách Văn Dương', 6, 'ACTIVE', 'admin');

-- ============================================================================
-- PART 4: PRODUCTS & PROCESSES
-- 5 Product Lines, 20 Processes (phân loại 1-4), 20 Products
-- ============================================================================

INSERT INTO products (id, code, name, description, created_by)
VALUES (1, 'BOM-P100', 'Bơm Nước Dân Dụng P100', 'Công suất 0.75kW – 1HP, lưu lượng 30L/min', 'admin'),
       (2, 'BOM-P200', 'Bơm Nước Dân Dụng P200', 'Công suất 1.1kW – 1.5HP, lưu lượng 45L/min', 'admin'),
       (3, 'BOM-P300', 'Bơm Nước Công Nghiệp P300', 'Công suất 2.2kW – 3HP, lưu lượng 80L/min', 'admin'),
       (4, 'BOM-P500', 'Bơm Nước Công Nghiệp P500', 'Công suất 3.7kW – 5HP, lưu lượng 120L/min', 'admin'),
       (5, 'BOM-H150', 'Bơm Thủy Lực H150', 'Áp suất 150 bar, lưu lượng 28cc/rev', 'admin'),
       (6, 'BOM-H250', 'Bơm Thủy Lực H250', 'Áp suất 250 bar, lưu lượng 45cc/rev', 'admin'),
       (7, 'BOM-H350', 'Bơm Thủy Lực Cao Áp H350', 'Áp suất 350 bar, lưu lượng 60cc/rev', 'admin'),
       (8, 'BOM-W100', 'Bơm Ly Tâm W100', 'Cột áp 20m, lưu lượng 100L/min', 'admin'),
       (9, 'BOM-W200', 'Bơm Ly Tâm W200', 'Cột áp 35m, lưu lượng 200L/min', 'admin'),
       (10, 'BOM-W400', 'Bơm Ly Tâm Công Nghiệp W400', 'Cột áp 50m, lưu lượng 400L/min', 'admin'),
       (11, 'MOT-E100', 'Động Cơ Xăng E100', 'Động cơ 4 thì, 100cc, 4.5HP', 'admin'),
       (12, 'MOT-E200', 'Động Cơ Xăng E200', 'Động cơ 4 thì, 200cc, 8HP', 'admin'),
       (13, 'MOT-E400', 'Động Cơ Diesel E400', 'Động cơ 4 thì diesel, 400cc, 12HP', 'admin'),
       (14, 'BOM-B100', 'Bơm Bùn B100', 'Hạt rắn ≤50mm, lưu lượng 80L/min', 'admin'),
       (15, 'BOM-B200', 'Bơm Bùn B200', 'Hạt rắn ≤80mm, lưu lượng 150L/min', 'admin'),
       (16, 'BOM-P150', 'Bơm Nước Dân Dụng P150', 'Công suất 1.0kW – 1.3HP, lưu lượng 38L/min', 'admin'),
       (17, 'BOM-H200', 'Bơm Thủy Lực H200', 'Áp suất 200 bar, lưu lượng 36cc/rev', 'admin'),
       (18, 'MOT-E300', 'Động Cơ Xăng E300', 'Động cơ 4 thì, 300cc, 10HP', 'admin'),
       (19, 'BOM-W300', 'Bơm Ly Tâm W300', 'Cột áp 42m, lưu lượng 300L/min', 'admin'),
       (20, 'BOM-B300', 'Bơm Bùn B300', 'Hạt rắn ≤100mm, lưu lượng 250L/min', 'admin');

INSERT INTO processes (id, code, name, description, classification, standard_time_jt, product_line_id, created_by)
VALUES
-- Line Tiện CNC (PL 1) – classification 1,2
(1, 'TI-01', 'Tiện Thô Trục Bơm', 'Gia công tiện thô trục bơm đạt IT11', 1, 45.00, 1, 'admin'),
(2, 'TI-02', 'Tiện Tinh Trục Bơm', 'Gia công tiện tinh đạt Ra 1.6, IT8', 1, 60.00, 1, 'admin'),
(3, 'TI-03', 'Tiện Ren & Lỗ Tâm', 'Tiện ren ngoài M20x2 và lỗ tâm 60°', 2, 35.00, 1, 'admin'),
(4, 'TI-04', 'Tiện Cắt Đứt & Vát Mép', 'Cắt đứt đạt chiều dài, vát mép 1x45°', 3, 20.00, 1, 'admin'),
-- Line Phay CNC (PL 2) – classification 1,2,3
(5, 'PH-01', 'Phay Mặt Phẳng Vỏ Bơm', 'Phay mặt trên đạt Ra 3.2, phẳng ±0.1', 1, 55.00, 2, 'admin'),
(6, 'PH-02', 'Phay Hốc Buồng Bơm', 'Phay hốc đạt kích thước ±0.05mm', 1, 90.00, 2, 'admin'),
(7, 'PH-03', 'Khoan & Doa Lỗ Lắp Ghép', 'Khoan φ20, doa đạt H7, vuông góc 0.02', 2, 75.00, 2, 'admin'),
(8, 'PH-04', 'Phay Rãnh Then & Rãnh Dầu', 'Phay rãnh then 6x6, rãnh dầu R2', 3, 40.00, 2, 'admin'),
(9, 'PH-05', 'Taro Ren Lỗ Vít', 'Taro M6, M8, M10 đạt 6H', 4, 25.00, 2, 'admin'),
-- Line Hàn W-Series (PL 3) – classification 1,2,3,4
(10, 'HA-01', 'Hàn MIG Thân Bơm', 'Hàn MIG vòng bích đạt tiêu chuẩn ISO 5817-B', 1, 120.00, 3, 'admin'),
(11, 'HA-02', 'Hàn TIG Đường Ống Nội', 'Hàn TIG đường ống nội, không thấm rò', 1, 150.00, 3, 'admin'),
(12, 'HA-03', 'Hàn Điểm Giá Đỡ', 'Hàn điểm giá đỡ motor, đạt lực kéo ≥500N', 2, 30.00, 3, 'admin'),
(13, 'HA-04', 'Làm Sạch Mối Hàn', 'Mài + chà nhám mối hàn đạt Ra 6.3', 4, 40.00, 3, 'admin'),
-- Line Lắp Ráp Động Cơ (PL 4) – classification 1,2,3
(14, 'LA-DC-01', 'Lắp Trục Khuỷu & Piston', 'Lắp trục khuỷu, piston đúng chiều, khe hở ≤0.05', 1, 180.00, 4, 'admin'),
(15, 'LA-DC-02', 'Lắp Nắp Máy & Ron Nắp', 'Lắp ron đa lớp, xiết bu-lông theo moment 35Nm', 1, 90.00, 4, 'admin'),
(16, 'LA-DC-03', 'Căn Chỉnh & Cân Bằng', 'Cân bằng động, độ lệch ≤5g·mm', 2, 60.00, 4, 'admin'),
(17, 'LA-DC-04', 'Test Nổ Máy & Điều Chỉnh', 'Test chạy thử 30 phút, đo áp suất dầu ≥3bar', 1, 200.00, 4, 'admin'),
-- Line Lắp Ráp Bơm (PL 5) – classification 1,2,3,4
(18, 'LA-B-01', 'Lắp Bánh Công Tác', 'Lắp bánh công tác đúng hướng, khe hở 0.3-0.5mm', 1, 80.00, 5, 'admin'),
(19, 'LA-B-02', 'Lắp Ron Cao Su & Gioăng', 'Lắp ron đúng chiều, không xoắn, kiểm kín 100%', 1, 45.00, 5, 'admin'),
(20, 'LA-B-03', 'Test Áp Lực Thủy Lực', 'Test áp 1.5x áp danh định, giữ 5 phút không rò', 1, 60.00, 5, 'admin'),
(21, 'LA-B-04', 'Đóng Gói & Dán Nhãn', 'Đóng hộp carton, dán nhãn đúng model và serial', 4, 15.00, 5, 'admin');

INSERT INTO product_process (product_id, process_id, standard_time_jt, created_by)
VALUES
-- P-Series dùng Line Tiện
(1, 1, 45.0, 'admin'),
(1, 2, 60.0, 'admin'),
(1, 3, 35.0, 'admin'),
(1, 4, 20.0, 'admin'),
(2, 1, 48.0, 'admin'),
(2, 2, 65.0, 'admin'),
(2, 3, 38.0, 'admin'),
(2, 4, 22.0, 'admin'),
(3, 1, 50.0, 'admin'),
(3, 2, 70.0, 'admin'),
(3, 3, 40.0, 'admin'),
(3, 4, 25.0, 'admin'),
(4, 1, 55.0, 'admin'),
(4, 2, 75.0, 'admin'),
(4, 3, 42.0, 'admin'),
(4, 4, 28.0, 'admin'),
(16, 1, 46.0, 'admin'),
(16, 2, 62.0, 'admin'),
(16, 3, 36.0, 'admin'),
(16, 4, 21.0, 'admin'),
-- H-Series dùng Line Phay
(5, 5, 55.0, 'admin'),
(5, 6, 90.0, 'admin'),
(5, 7, 75.0, 'admin'),
(5, 8, 40.0, 'admin'),
(5, 9, 25.0, 'admin'),
(6, 5, 58.0, 'admin'),
(6, 6, 95.0, 'admin'),
(6, 7, 80.0, 'admin'),
(6, 8, 42.0, 'admin'),
(6, 9, 27.0, 'admin'),
(7, 5, 60.0, 'admin'),
(7, 6, 100.0, 'admin'),
(7, 7, 85.0, 'admin'),
(7, 8, 45.0, 'admin'),
(7, 9, 30.0, 'admin'),
(17, 5, 56.0, 'admin'),
(17, 6, 92.0, 'admin'),
(17, 7, 78.0, 'admin'),
(17, 8, 41.0, 'admin'),
(17, 9, 26.0, 'admin'),
-- W-Series dùng Line Hàn
(8, 10, 120.0, 'admin'),
(8, 11, 150.0, 'admin'),
(8, 12, 30.0, 'admin'),
(8, 13, 40.0, 'admin'),
(9, 10, 125.0, 'admin'),
(9, 11, 155.0, 'admin'),
(9, 12, 32.0, 'admin'),
(9, 13, 42.0, 'admin'),
(10, 10, 130.0, 'admin'),
(10, 11, 160.0, 'admin'),
(10, 12, 35.0, 'admin'),
(10, 13, 45.0, 'admin'),
(19, 10, 122.0, 'admin'),
(19, 11, 152.0, 'admin'),
(19, 12, 31.0, 'admin'),
(19, 13, 41.0, 'admin'),
-- E-Series (Động Cơ)
(11, 14, 180.0, 'admin'),
(11, 15, 90.0, 'admin'),
(11, 16, 60.0, 'admin'),
(11, 17, 200.0, 'admin'),
(12, 14, 190.0, 'admin'),
(12, 15, 95.0, 'admin'),
(12, 16, 65.0, 'admin'),
(12, 17, 210.0, 'admin'),
(13, 14, 200.0, 'admin'),
(13, 15, 100.0, 'admin'),
(13, 16, 70.0, 'admin'),
(13, 17, 220.0, 'admin'),
(18, 14, 195.0, 'admin'),
(18, 15, 98.0, 'admin'),
(18, 16, 68.0, 'admin'),
(18, 17, 215.0, 'admin'),
-- B-Series (Bơm Bùn)
(14, 18, 80.0, 'admin'),
(14, 19, 45.0, 'admin'),
(14, 20, 60.0, 'admin'),
(14, 21, 15.0, 'admin'),
(15, 18, 85.0, 'admin'),
(15, 19, 48.0, 'admin'),
(15, 20, 65.0, 'admin'),
(15, 21, 16.0, 'admin'),
(20, 18, 90.0, 'admin'),
(20, 19, 50.0, 'admin'),
(20, 20, 70.0, 'admin'),
(20, 21, 17.0, 'admin');

-- ============================================================================
-- PART 5: DEFECTS (40 lỗi quá khứ đa dạng)
-- ============================================================================

-- defects schema: (defect_code, defect_description, process_id, detected_date,
--   defect_type, origin_measures, outflow_measures, origin_cause, outflow_cause,
--   cause_point, customer, quantity, conclusion, note, created_by)
INSERT INTO defects (defect_code, defect_description, process_id, detected_date,
                     defect_type, origin_measures, outflow_measures, origin_cause,
                     outflow_cause, cause_point, customer, quantity, conclusion, note, created_by)
VALUES
-- Line Tiện (process 1-4)
('DF001', 'Xước bề mặt trục bơm do dao cụ mòn', 2, '2025-01-10', 'DEFECTIVE_GOODS', 'Thay dao theo schedule',
 'Kiểm 100% đầu ca', 'Mòn đột xuất', 'Không SOI đèn', 'Đài dao tiện', 'Nội bộ', 5, 'Lập SOP thay dao định kỳ', NULL,
 'system'),
('DF002', 'Kích thước đường kính ngoài sai dung sai IT8', 1, '2025-01-18', 'CLAIM', 'Hiệu chỉnh máy CNC',
 'Đào tạo lại đo lường', 'Lỗi operator – Setup sai G54', 'Đo sai phương pháp', 'Tại kẹp phôi', 'KH Yamaha', 3,
 'Chuẩn hóa quy trình setup', NULL, 'system'),
('DF003', 'Chiều dài trục ngắn hơn bản vẽ 0.3mm', 1, '2025-02-05', 'DEFECTIVE_GOODS', 'Kiểm bù offset tool',
 'Kiểm 100% cuối ca', 'Thao tác reset offset Z', 'Không dùng dưỡng đo', 'Tool setting', 'Nội bộ', 8,
 'Training lại tool setting', NULL, 'system'),
('DF004', 'Ba via ren M20 không đạt 6g', 3, '2025-02-20', 'CLAIM', 'Thay mũi taro mới', 'Kiểm caliper ren',
 'Taro mòn vượt giới hạn', 'Đo sai hướng ren', 'Trạm taro', 'KH Honda', 2, 'Thiết lập giới hạn mòn taro', NULL,
 'system'),
('DF005', 'Độ nhám bề mặt tiện tinh Ra vượt 3.2', 2, '2025-03-08', 'DEFECTIVE_GOODS', 'Mài dao tiện CBN',
 'Đo nhám Mitutoyo', 'Dao tù – bỏ qua KPT', 'Không đo hết lô', 'Đài tiện tinh', 'Nội bộ', 12,
 'Lịch mài dao mỗi 200 pcs', NULL, 'system'),
('DF006', 'Lỗ tâm 60° bị lệch >0.05mm', 3, '2025-04-02', 'CLAIM', 'Kiểm tra đồ gá', 'Đo CMM 100%',
 'Đồ gá mòn – không cân', 'CMM bỏ qua bước đo', 'Máy khoan tâm', 'KH Suzuki', 1, 'Cân chuẩn đồ gá 1 tuần/lần', NULL,
 'system'),
-- Line Phay (process 5-9)
('DF007', 'Độ phẳng mặt vỏ bơm vượt 0.1mm', 5, '2025-01-15', 'DEFECTIVE_GOODS', 'Thay dao phay mặt đầu',
 'Kiểm phẳng đồng hồ so', 'Dao rung – tốc độ cắt sai', 'Bỏ qua đo giữa ca', 'Bàn phay', 'Nội bộ', 6,
 'Giảm tốc độ ăn dao 20%', NULL, 'system'),
('DF008', 'Hốc buồng bơm rộng hơn +0.08mm', 6, '2025-02-01', 'CLAIM', 'Đo lại với CMM', 'Kiểm 100% trước ghép',
 'Lỗi CAM path – lập trình sai', 'Bỏ qua first piece', 'Trung tâm gia công', 'KH Mitsubishi', 4,
 'Kiểm tra CAM trước khi sản xuất', NULL, 'system'),
('DF009', 'Lỗ doa H7 bị côn 0.02mm', 7, '2025-02-25', 'CLAIM', 'Kiểm dưỡng trục H7', 'Đo sau gia công 100%',
 'Doa mòn – không track TT', 'Bỏ qua go/no-go', 'Trạm khoan doa', 'KH Kawasaki', 3, 'SOP kiểm doa mỗi 50 lỗ', NULL,
 'system'),
('DF010', 'Rãnh then lệch tâm >0.03mm', 8, '2025-03-10', 'DEFECTIVE_GOODS', 'Dùng đồ gá phay rãnh',
 'Kiểm đồ gá trước phay', 'Kẹp lỏng – thao tác nhanh', 'Bỏ qua kiểm vị trí', 'Trạm phay rãnh', 'Nội bộ', 7,
 'Training kẹp chi tiết đúng lực', NULL, 'system'),
('DF011', 'Ren M8 taro vỡ kẹt trong lỗ', 9, '2025-03-22', 'DEFECTIVE_GOODS', 'Thay loại taro HSS-E',
 'Kiểm trước khi lắp ghép', 'Phoi kẹt – không dùng dầu cắt', 'Bỏ qua kiểm phoi', 'Trạm taro', 'Nội bộ', 15,
 'SOP dùng dầu cắt & phun khí', NULL, 'system'),
('DF012', 'Độ song song 2 mặt phay >0.05mm', 5, '2025-04-15', 'CLAIM', 'Hiệu chỉnh trục máy', 'Kiểm bằng CMM 3D',
 'Trục máy lỏng – bảo trì trễ', 'Chỉ kiểm 1 mặt', 'Máy phay 3 trục', 'KH Bosch', 2, 'Lịch bảo trì trục máy 3 tháng/lần',
 NULL, 'system'),
-- Line Hàn (process 10-13)
('DF013', 'Rỗ khí mối hàn MIG thân bơm', 10, '2025-01-20', 'CLAIM', 'Điều chỉnh lưu lượng khí', 'RT/PT kiểm tra',
 'Bình khí hết – không kiểm', 'Ngoại quan bỏ qua', 'Trạm hàn MIG', 'KH Pentax', 2,
 'Lắp cảm biến báo động lưu lượng khí', NULL, 'system'),
('DF014', 'Nứt mối hàn TIG đường ống nội', 11, '2025-02-10', 'CLAIM', 'Preheating 150°C', 'UT 100% mối hàn',
 'Vật liệu thiếu Mn – sai vật tư', 'Ngoại quan bỏ qua', 'Trạm hàn TIG', 'KH Atlas', 1,
 'Kiểm chứng chỉ vật liệu đầu vào', NULL, 'system'),
('DF015', 'Mối hàn điểm giá đỡ bong tróc khi test kéo', 12, '2025-03-01', 'DEFECTIVE_GOODS', 'Tăng dòng hàn 20%',
 'Test lực kéo mẫu', 'Dòng hàn thấp – thợ mới', 'Sample test sai', 'Trạm hàn điểm', 'Nội bộ', 4,
 'Training thợ hàn điểm', NULL, 'system'),
('DF016', 'Biến dạng nhiệt vỏ bơm sau hàn MIG', 10, '2025-03-18', 'DEFECTIVE_GOODS', 'Dùng đồ gá giữ form',
 'Kiểm phẳng sau hàn', 'Hàn thiếu đường kẹp', 'Chỉ đo 1 điểm', 'Jig hàn', 'Nội bộ', 9, 'Thiết kế jig chống vênh nhiệt',
 NULL, 'system'),
('DF017', 'Vảy hàn bắn vào buồng bơm', 10, '2025-04-05', 'DEFECTIVE_GOODS', 'Dùng chắn nhiệt TIG', 'Kiểm nội bộ 100%',
 'Thợ bỏ qua quy trình che chắn', 'Ngoại quan bỏ sót', 'Trạm hàn buồng', 'Nội bộ', 11,
 'Bắt buộc che chắn buồng bơm khi hàn', NULL, 'system'),
-- Line Lắp Ráp Động Cơ (process 14-17)
('DF018', 'Piston lắp ngược chiều mũi tên', 14, '2025-01-25', 'CLAIM', 'Đồ gá hướng piston', 'Soi đèn nội bộ',
 'Thiếu POK marking – thao tác nhầm', 'Bỏ qua kiểm chiều', 'Trạm lắp piston', 'KH Komatsu', 3,
 'Gắn poka-yoke cho jig lắp piston', NULL, 'system'),
('DF019', 'Bu-lông nắp máy không đạt moment 35Nm', 15, '2025-02-14', 'CLAIM', 'Hiệu chỉnh torque wrench',
 'Kiểm moment sau lắp', 'Cần vặn mòn – không hiệu chuẩn', 'Bỏ qua verify moment', 'Trạm lắp nắp máy', 'KH CAT', 4,
 'Lịch hiệu chuẩn dụng cụ đo moment', NULL, 'system'),
('DF020', 'Cân bằng động vượt 15g·mm sau lắp', 16, '2025-03-05', 'DEFECTIVE_GOODS', 'Cân lại bánh đà',
 'Test run 3000rpm 5p', 'Bánh đà sai khối lượng – vật tư sai spec', 'Bỏ qua cân bằng tĩnh', 'Máy cân bằng động',
 'Nội bộ', 2, 'Thêm bước cân bằng tĩnh trước CĐ', NULL, 'system'),
('DF021', 'Áp suất dầu bôi trơn <3bar khi test', 17, '2025-03-20', 'CLAIM', 'Kiểm bơm dầu', 'Test run trước xuất xưởng',
 'Bơm dầu mòn – vượt TT', 'Bỏ qua đo áp dầu', 'Trạm test động cơ', 'KH JCB', 1, 'Track tuổi thọ bơm dầu theo giờ', NULL,
 'system'),
('DF022', 'Rò dầu ron nắp máy khi test chạy nóng', 15, '2025-04-10', 'CLAIM', 'Thay ron cùng mã OEM',
 'Test leakdown 5 phút', 'Ron sai part number – lỗi MRP picking', 'Ngoại quan bỏ qua rò', 'Trạm lắp ron nắp',
 'KH Volvo', 2, 'Barcode verify ron trước lắp', NULL, 'system'),
('DF023', 'Tiếng gõ động cơ bất thường sau 10 phút test', 14, '2025-04-22', 'DEFECTIVE_GOODS',
 'Kiểm khe hở cặp vòng bi', 'Test run 30 phút', 'Vòng bi sai cấp chính xác – vật tư sai', 'Test run ngắn',
 'Trạm lắp vòng bi', 'Nội bộ', 1, 'SOP kiểm marking vòng bi đầu vào', NULL, 'system'),
-- Line Lắp Ráp Bơm (process 18-21)
('DF024', 'Rò rỉ ron cao su khi test áp lực', 19, '2025-01-12', 'CLAIM', 'Đồ gá ép ron', 'Test thủy lực 100%',
 'Ron bị rách – lực ép tay không đều', 'Bỏ qua test áp', 'Trạm ép ron', 'KH Ebara', 5,
 'Training thao tác ép ron đúng lực', NULL, 'system'),
('DF025', 'Bánh công tác lắp ngược gây rung >3mm/s', 18, '2025-01-28', 'CLAIM', 'Jig hướng bánh CK', 'Đo rung sau lắp',
 'Không có marking hướng', 'Bỏ qua đo rung', 'Trạm lắp bánh CK', 'KH Grundfos', 2, 'Khắc mũi tên chỉ hướng lên bánh CK',
 NULL, 'system'),
('DF026', 'Test áp 1.5x thất bại tại mặt bích', 20, '2025-02-08', 'CLAIM', 'Kiểm ren bích', 'RT mặt bích 100%',
 'Ren bích thiếu đường ren – dao taro cùn', 'Bỏ qua kiểm ren', 'Trạm taro bích', 'KH Xylem', 3,
 'SOP kiểm sau taro bích', NULL, 'system'),
('DF027', 'Gioăng cao su bị vênh do tồn kho quá 2 năm', 19, '2025-02-25', 'DEFECTIVE_GOODS', 'Nhập kho theo FIFO',
 'Kiểm hạn sử dụng', 'FIFO kho sai – không tuân thủ WMS', 'Bỏ qua kiểm hạn', 'Kho vật tư', 'Nội bộ', 8,
 'Enforce FIFO trên WMS', NULL, 'system'),
('DF028', 'Nhãn model dán sai sản phẩm', 21, '2025-03-12', 'CLAIM', 'Hệ thống in nhãn auto', 'Kiểm nhãn 100%',
 'Cuộn nhãn nhầm – kho lấy sai', 'Bỏ qua scan barcode', 'Trạm dán nhãn', 'KH Pedrollo', 1,
 'Barcode scan nhãn tự động trước dán', NULL, 'system'),
('DF029', 'Bulong kẹp nắp thiếu 1 con', 21, '2025-03-28', 'DEFECTIVE_GOODS', 'Checklist đếm bulong', 'Kiểm đếm final',
 'Sót bulong – thao tác vội', 'Không đếm final', 'Trạm lắp nắp bơm', 'Nội bộ', 6, 'Lắp poka-yoke đếm bulong', NULL,
 'system'),
('DF030', 'Tiếng ồn bất thường >70dB khi vận hành test', 18, '2025-04-18', 'CLAIM', 'Kiểm độ đồng tâm',
 'Đo dB trước đóng gói', 'Lệch tâm trục – không cân', 'Bỏ qua đo dB', 'Trạm cân trục', 'KH Wilo', 2,
 'Thêm bước đo dB trước đóng gói', NULL, 'system'),
-- Cross-line
('DF031', 'Bề mặt mạ kẽm không đều, bong tróc sau 3 tháng', 4, '2025-05-03', 'CLAIM', 'Kiểm pre-treatment',
 'Salt spray 96h test', 'Bể hoá chất pha sai', 'Ngoại quan bỏ qua', 'Trạm mạ', 'KH Hitachi', 10,
 'Kiểm nồng độ bể mạ trước mỗi ca', NULL, 'system'),
('DF032', 'Ký hiệu sản phẩm khắc laser sai vị trí', 9, '2025-05-10', 'DEFECTIVE_GOODS', 'Kiểm fixture laser',
 'Kiểm toàn bộ lô', 'Fixture bị dịch – không set zero', 'Bỏ qua first article', 'Trạm laser', 'Nội bộ', 20,
 'SOP set zero laser mỗi đầu ca', NULL, 'system'),
('DF033', 'Vòng O-ring bị bục khi test áp 2.0x', 19, '2025-05-20', 'CLAIM', 'Thay O-ring Shore 70', 'Test 100% sau lắp',
 'O-ring Shore thấp – vật tư sai', 'Kiểm chỉ ngoại quan', 'Trạm lắp O-ring', 'KH Flowserve', 4,
 'Kiểm cứng Shore trước sử dụng', NULL, 'system'),
('DF034', 'Momen xoắn đầu ra thấp hơn spec 15%', 16, '2025-06-01', 'CLAIM', 'Kiểm cam timing', 'Dyno test 100%',
 'Timing cam lệch – thao tác lắp sai', 'Test ngắn bỏ sót', 'Trạm lắp cam', 'KH Cummins', 2,
 'Dụng cụ timing chuyên dụng', NULL, 'system'),
('DF035', 'Hở mối ghép thân bơm lô 250 chiếc', 20, '2025-06-15', 'STARTLED_CLAIM', 'Kiểm khoảng cách mặt bích',
 'Test áp toàn lô', 'Phôi đúc sai dung sai', 'Pass giấy tờ bỏ qua đo', 'Trạm lắp ráp thân', 'KH Grundfos', 250,
 'Audit nhà cung cấp đúc', NULL, 'system'),
('DF036', 'Nước vào hộp số trong điều kiện IP55', 15, '2025-07-02', 'CLAIM', 'Kiểm chuỗi gioăng IP',
 'IP55 test toàn bộ', 'Gioăng thiếu – lỗi thiết kế lắp', 'Bỏ qua IP test', 'Trạm lắp gioăng IP', 'KH Siemens', 3,
 'Thiết kế lại chuỗi gioăng IP', NULL, 'system'),
('DF037', 'Vật tư sai mác thép 40Cr thay vì 45', 1, '2025-07-15', 'DEFECTIVE_GOODS', 'Kiểm PMI mác thép',
 'CMM hardness test', 'Sai vật tư – lỗi PO nhập kho', 'Bỏ qua cert thép', 'Kho nhập', 'Nội bộ', 30,
 'Bắt buộc kiểm PMI đầu vào', NULL, 'system'),
('DF038', 'Lớp sơn phủ bong sau thử nghiệm mù muối', 13, '2025-07-28', 'CLAIM', 'Tăng độ dày sơn lót',
 'Salt spray 240h', 'Sơn lót mỏng – thiếu pass sơn', 'Kiểm chỉ bằng mắt', 'Buồng sơn', 'KH Sulzer', 5,
 'Đo độ dày sơn sau mỗi pass', NULL, 'system'),
('DF039', 'Dấu cân bằng trên bánh đà bị mờ không đọc được', 17, '2025-08-05', 'DEFECTIVE_GOODS', 'Dùng mực khắc laser',
 'Kiểm trước xuất kho', 'Mực yếu – thay mực giá rẻ', 'Bỏ qua kiểm dấu', 'Trạm đánh dấu', 'Nội bộ', 15,
 'Bắt buộc laser marking', NULL, 'system'),
('DF040', 'Tốc độ bơm thấp hơn spec 10% khi test đầy tải', 18, '2025-08-20', 'CLAIM', 'Kiểm khe hở bánh CK',
 'Performance test 100%', 'Khe hở bánh CK rộng – mòn đột xuất', 'Test tải nhẹ bỏ sót', 'Trạm test tải', 'KH Flygt', 4,
 'Thêm test đầy tải trước đóng gói', NULL, 'system');

-- ============================================================================
-- PART 6: DEFECT PROPOSALS (8 proposals, đa dạng trạng thái)
-- ============================================================================

INSERT INTO defect_proposals (id, product_line_id, status, current_version, form_code, created_by)
VALUES (1, 1, 'APPROVED', 2, 'DEF-2025-001', 'tl_tien01'),
       (2, 2, 'APPROVED', 1, 'DEF-2025-002', 'tl_phay01'),
       (3, 3, 'WAITING_SV', 1, 'DEF-2025-003', 'tl_hanlap01'),
       (4, 4, 'REJECTED_BY_SV', 1, 'DEF-2025-004', 'tl_dongco01'),
       (5, 5, 'WAITING_MANAGER', 1, 'DEF-2025-005', 'tl_laprap01'),
       (6, 1, 'APPROVED', 1, 'DEF-2026-001', 'tl_tien01'),
       (7, 4, 'WAITING_SV', 1, 'DEF-2026-002', 'tl_dongco01'),
       (8, 5, 'DRAFT', 1, 'DEF-2026-003', 'tl_laprap01');

INSERT INTO defect_proposal_details
(defect_proposal_id, defect_id, proposal_type, defect_description, process_id,
 detected_date, defect_type, origin_measures, outflow_measures, note,
 origin_cause, outflow_cause, cause_point, created_by)
VALUES (1, 1, 'CREATE', 'Xước bề mặt trục bơm do dao cụ mòn', 2, '2025-01-10', 'DEFECTIVE_GOODS',
        'Thay dao theo schedule', 'Kiểm 100% đầu ca', 'Đã duyệt', 'Dao mòn', 'Không SOI đèn', 'Đài dao tiện',
        'tl_tien01'),
       (1, 2, 'CREATE', 'Kích thước đường kính ngoài sai dung sai', 1, '2025-01-18', 'CLAIM', 'Hiệu chỉnh máy CNC',
        'Đào tạo lại', 'Đã duyệt', 'Setup sai G54', 'Đo sai', 'Tại kẹp phôi', 'tl_tien01'),
       (2, 7, 'CREATE', 'Độ phẳng mặt vỏ bơm vượt 0.1mm', 5, '2025-01-15', 'DEFECTIVE_GOODS', 'Thay dao phay đầu',
        'Kiểm phẳng đồng hồ', 'OK', 'Dao rung', 'Bỏ qua đo giữa ca', 'Bàn phay', 'tl_phay01'),
       (2, 8, 'CREATE', 'Hốc buồng bơm rộng hơn +0.08mm', 6, '2025-02-01', 'CLAIM', 'Đo lại với CMM', 'Kiểm trước ghép',
        'OK', 'CAM path sai', 'Bỏ qua first piece', 'Trung tâm GC', 'tl_phay01'),
       (3, 13, 'CREATE', 'Rỗ khí mối hàn MIG thân bơm', 10, '2025-01-20', 'CLAIM', 'Điều chỉnh lưu lượng khí',
        'RT/PT kiểm tra', 'Chờ SV', 'Khí bảo vệ thiếu', 'Ngoại quan bỏ qua', 'Trạm MIG', 'tl_hanlap01'),
       (4, 18, 'CREATE', 'Piston lắp ngược chiều mũi tên', 14, '2025-01-25', 'CLAIM', 'Đồ gá hướng piston',
        'Soi đèn nội bộ', 'Bị trả', 'Thiếu POK marking', 'Bỏ qua kiểm chiều', 'Trạm piston', 'tl_dongco01'),
       (5, 24, 'CREATE', 'Rò rỉ ron cao su khi test áp lực', 19, '2025-01-12', 'CLAIM', 'Đồ gá ép ron', 'Test thủy lực',
        'Chờ MG', 'Ron bị rách', 'Lực ép không đều', 'Trạm ép ron', 'tl_laprap01'),
       (5, 25, 'CREATE', 'Bánh công tác lắp ngược gây rung >3mm/s', 18, '2025-01-28', 'CLAIM', 'Jig hướng bánh',
        'Đo rung sau lắp', 'Chờ MG', 'Không có marking', 'Bỏ qua đo rung', 'Trạm lắp bánh', 'tl_laprap01'),
       (6, 3, 'CREATE', 'Chiều dài trục ngắn hơn bản vẽ 0.3mm', 1, '2025-02-05', 'DEFECTIVE_GOODS',
        'Kiểm bù offset tool', 'Kiểm 100% cuối ca', 'OK', 'Offset Z sai', 'Không dùng dưỡng', 'Tool setting',
        'tl_tien01'),
       (7, 19, 'CREATE', 'Bu-lông nắp máy không đạt moment 35Nm', 15, '2025-02-14', 'CLAIM', 'Hiệu chỉnh torque',
        'Kiểm moment sau lắp', 'Chờ SV', 'Cần vặn mòn', 'Bỏ qua verify', 'Trạm lắp nắp', 'tl_dongco01'),
       (8, 29, 'CREATE', 'Bulong kẹp nắp thiếu 1 con', 21, '2025-03-28', 'DEFECTIVE_GOODS', 'Checklist count',
        'Kiểm đếm final', 'Draft', 'Sót bulong', 'Không đếm final', 'Trạm lắp nắp', 'tl_laprap01');

-- ============================================================================
-- PART 7: TRAINING SAMPLES (30 mẫu đa dạng)
-- ============================================================================

INSERT INTO training_samples (id, process_id, product_line_id, defect_id, category_name,
                              training_description, product_id, training_sample_code, training_code,
                              process_order, category_order, content_order, note, created_by)
VALUES
-- Line Tiện (process 1-4, PL 1)
(1, 2, 1, 1, 'Lỗi Ngoại Quan - Xước Bề Mặt', 'Soi đèn góc 45° để phát hiện vết xước trục bơm. Loại bỏ nếu vết > 0.1mm.',
 1, 'M-TI-01', 'TS0001', 1, 1, 1, 'Lỗi phổ biến nhất Line Tiện', 'system'),
(2, 1, 1, 2, 'Lỗi Kích Thước - Đường Kính', 'Đo 3 điểm (đầu/giữa/cuối) bằng panme điện tử. Sai số cho phép ±0.015mm.',
 1, 'M-TI-02', 'TS0002', 1, 2, 1, 'Đo trước khi chuyển tiếp', 'system'),
(3, 3, 1, 4, 'Lỗi Ren - Kiểm Tra Ren Ngoài', 'Dùng ring gauge GO/NO-GO kiểm ren M20x2. Hướng vặn chiều kim đồng hồ.', 1,
 'M-TI-03', 'TS0003', 1, 3, 1, 'Cần ring gauge đúng chuẩn', 'system'),
(4, 2, 1, 5, 'Lỗi Nhám - Kiểm Tra Ra', 'Đo nhám Mitutoyo SJ-210 tại 3 vị trí. Ra ≤ 1.6 mới đạt.', 2, 'M-TI-04',
 'TS0004', 2, 1, 1, 'Đo sau tiện tinh', 'system'),
(5, 4, 1, 1, 'Vát Mép - Kiểm Cạnh Sắc', 'Dùng dưỡng bán kính kiểm vát mép 1x45°. Không có cạnh sắc để ráy da.', 1,
 'M-TI-05', 'TS0005', 2, 2, 1, 'Safety handling', 'system'),
(6, 1, 1, 3, 'Lỗi Kích Thước - Chiều Dài', 'Dùng thước cặp điện tử đo chiều dài tổng. Sai số ±0.3mm mới đạt.', 1,
 'M-TI-06', 'TS0006', 1, 2, 2, 'Đo sau cắt đứt', 'system'),
-- Line Phay (process 5-9, PL 2)
(7, 5, 2, 7, 'Lỗi Phẳng - Mặt Vỏ Bơm', 'Kiểm độ phẳng bằng đồng hồ so trên bàn đá. Cho phép ≤ 0.05mm toàn diện.', 5,
 'M-PH-01', 'TS0007', 1, 1, 1, 'Đo sau phay mặt đầu', 'system'),
(8, 6, 2, 8, 'Lỗi Kích Thước - Hốc Buồng Bơm', 'Đo hốc bằng thước trụ nội bộ. Kích thước danh định ±0.05mm.', 5,
 'M-PH-02', 'TS0008', 1, 2, 1, 'Kiểm first article', 'system'),
(9, 7, 2, 9, 'Lỗi Lỗ - Doa H7', 'Dùng pin gauge go/no-go kiểm lỗ doa φ20H7. Kiểm từng lỗ không bỏ sót.', 6, 'M-PH-03',
 'TS0009', 2, 1, 1, 'Go/No-Go bắt buộc', 'system'),
(10, 8, 2, 10, 'Lỗi Rãnh Then - Vị Trí Rãnh', 'Dùng thước đo vị trí rãnh then so bản vẽ. Lệch vị trí ≤ 0.03mm.', 5,
 'M-PH-04', 'TS0010', 2, 2, 1, 'So đối xứng từ tâm', 'system'),
(11, 9, 2, 11, 'Lỗi Ren Lỗ - Ren M8', 'Dùng plug gauge GO/NO-GO kiểm ren M8 sau taro. Vặn đủ chiều dài ren.', 6,
 'M-PH-05', 'TS0011', 3, 1, 1, 'Không lực vào NO-GO', 'system'),
(12, 5, 2, 12, 'Lỗi Song Song - 2 Mặt Phay', 'Đặt chi tiết trên bàn đá đo 4 góc + tâm bằng đồng hồ. ≤ 0.05mm.', 7,
 'M-PH-06', 'TS0012', 1, 3, 1, 'Bắt buộc cho HF series', 'system'),
-- Line Hàn (process 10-13, PL 3)
(13, 10, 3, 13, 'Hàn MIG - Kiểm Rỗ Khí', 'Quan sát mối hàn bằng mắt + kính lúp 10x tìm lỗ khí ≥ 0.5mm.', 8, 'M-HA-01',
 'TS0013', 1, 1, 1, 'Kiểm toàn bộ mối hàn', 'system'),
(14, 11, 3, 14, 'Hàn TIG - Kiểm Nứt Mối Hàn', 'Dùng thuốc PT (penetrant test) phát hiện nứt trên mối TIG đường ống.', 9,
 'M-HA-02', 'TS0014', 1, 2, 1, 'PT: apply → wait 10min → wipe → developer', 'system'),
(15, 12, 3, 15, 'Hàn Điểm - Test Lực Kéo', 'Mẫu thử kéo ≥ 500N trên máy kéo. Ghi nhận lực phá huỷ vào log.', 8,
 'M-HA-03', 'TS0015', 2, 1, 1, '1 mẫu/50 điểm hàn', 'system'),
(16, 10, 3, 16, 'Biến Dạng Nhiệt - Kiểm Phẳng Sau Hàn', 'Đo phẳng bằng thước thẳng sau hàn. Vênh > 0.5mm phải nắn lại.',
 8, 'M-HA-04', 'TS0016', 1, 3, 1, 'Kiểm khi còn ấm ~50°C', 'system'),
(17, 13, 3, 17, 'Làm Sạch - Kiểm Vảy Hàn', 'Dùng đèn soi kiểm vảy hàn bên trong buồng bơm. Bằng 0 mới đạt.', 9,
 'M-HA-05', 'TS0017', 3, 1, 1, 'Vảy hàn gây hỏng bánh CK', 'system'),
-- Line Lắp Ráp Động Cơ (process 14-17, PL 4)
(18, 14, 4, 18, 'Lắp Piston - Hướng Mũi Tên', 'Mũi tên trên piston PHẢI hướng về phía đầu trục. Kiểm bằng soi đèn.', 11,
 'M-DC-01', 'TS0018', 1, 1, 1, 'Lỗi nghiêm trọng nếu sai', 'system'),
(19, 15, 4, 19, 'Lắp Nắp Máy - Kiểm Moment', 'Xiết bu-lông theo pattern chữ X, 3 lần, moment cuối 35Nm. Đánh dấu đầu.',
 11, 'M-DC-02', 'TS0019', 2, 1, 1, 'Pattern xiết bắt buộc', 'system'),
(20, 16, 4, 20, 'Cân Bằng Động - Đọc Máy', 'Đặt bánh đà vào máy cân bằng, đọc giá trị g·mm. Yêu cầu ≤ 5g·mm.', 12,
 'M-DC-03', 'TS0020', 3, 1, 1, 'Ghi nhận vào traveller card', 'system'),
(21, 17, 4, 21, 'Test Nổ Máy - Đo Áp Dầu', 'Khởi động, chạy không tải 5 phút. Đo áp dầu ≥ 3 bar tại 1500 rpm.', 12,
 'M-DC-04', 'TS0021', 4, 1, 1, 'Ngừng ngay nếu áp < 2.5bar', 'system'),
(22, 15, 4, 22, 'Lắp Ron Nắp - Kiểm Rò Dầu', 'Sau lắp, chạy 30 phút, dùng giấy trắng kiểm rò dầu quanh nắp máy.', 11,
 'M-DC-05', 'TS0022', 2, 2, 1, 'Quan sát toàn chu vi', 'system'),
-- Line Lắp Ráp Bơm (process 18-21, PL 5)
(23, 19, 5, 24, 'Lắp Ron Cao Su - Thao Tác',
 'Lắp ron theo chiều kim đồng hồ, không xoắn mép. Dùng tay không dùng vật sắc.', 14, 'M-B-01', 'TS0023', 2, 1, 1,
 'Bôi dầu mỏng trước lắp', 'system'),
(24, 18, 5, 25, 'Lắp Bánh CK - Hướng Cánh', 'Hướng cánh bánh công tác PHẢI theo chiều dòng chảy (nhìn từ cửa hút).', 14,
 'M-B-02', 'TS0024', 1, 1, 1, 'Sai hướng gây rung nghiêm trọng', 'system'),
(25, 20, 5, 26, 'Test Áp - Quy Trình Bơm', 'Nâng áp từ từ đến 1.5x áp danh định. Giữ 5 phút. Zero rò rỉ mới đạt.', 15,
 'M-B-03', 'TS0025', 3, 1, 1, 'Ghi nhận vào test sheet', 'system'),
(26, 19, 5, 27, 'Gioăng - Kiểm Hạn Sử Dụng', 'Đọc date code trên gioăng. Không dùng hàng tồn kho > 24 tháng.', 14,
 'M-B-04', 'TS0026', 2, 2, 1, 'FIFO nghiêm ngặt', 'system'),
(27, 21, 5, 28, 'Dán Nhãn - Kiểm Đúng Model',
 'Quét barcode nhãn so sánh với serial traveller trước khi dán. Match 100%.', 15, 'M-B-05', 'TS0027', 4, 1, 1,
 'Không dán tay không scan', 'system'),
(28, 18, 5, 29, 'Lắp Nắp - Đếm Bulong', 'Đếm đủ số bulong theo BOM trước và sau lắp. Ghi nhận vào checklist.', 14,
 'M-B-06', 'TS0028', 2, 3, 1, 'Checklist bắt buộc', 'system'),
(29, 20, 5, 30, 'Test Tiếng Ồn - Đo dB', 'Dùng máy đo tiếng ồn tại 1m từ bơm. Tải bình thường ≤ 68dB mới đạt.', 15,
 'M-B-07', 'TS0029', 3, 2, 1, 'Ghi nhận tại test sheet', 'system'),
(30, 19, 5, 33, 'O-Ring - Kiểm Độ Cứng Shore', 'Dùng Shore A durometer đo độ cứng O-ring. Yêu cầu 65-75 Shore A.', 14,
 'M-B-08', 'TS0030', 2, 4, 1, '1 mẫu/50 O-ring kiểm cơ tính', 'system');

-- ============================================================================
-- PART 8: TRAINING PLANS (15 kế hoạch, đa dạng trạng thái và thời gian)
-- ============================================================================

INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status,
                            current_version, note, min_training_per_day, max_training_per_day, created_by)
VALUES (1, 'TP-TI-2026-001', 'Kế hoạch HLV T1/2026 - Tổ Tiện Ca Ngày', '2026-01-05', '2026-01-31', 1, 1, 'APPROVED', 1,
        'Tháng 1 đã hoàn thành toàn bộ.', 1, 3, 'tl_tien01'),
       (2, 'TP-TI-2026-002', 'Kế hoạch HLV T2/2026 - Tổ Tiện Ca Ngày', '2026-02-03', '2026-02-28', 1, 1, 'APPROVED', 1,
        'Tháng 2 đã hoàn thành.', 1, 3, 'tl_tien01'),
       (3, 'TP-TI-2026-003', 'Kế hoạch HLV T3/2026 - Tổ Tiện Ca Ngày', '2026-03-03', '2026-03-31', 1, 1, 'APPROVED', 2,
        'Phiên bản 2 sau khi bổ sung NV004.', 1, 3, 'tl_tien01'),
       (4, 'TP-TI-2026-004', 'Kế hoạch HLV T4/2026 - Tổ Tiện Ca Ngày', '2026-04-01', '2026-04-30', 1, 1, 'DRAFT', 1,
        'Đang soạn thảo.', 1, 3, 'tl_tien01'),
       (5, 'TP-PH-2026-001', 'Kế hoạch HLV T1/2026 - Tổ Phay Ca Ngày', '2026-01-05', '2026-01-31', 2, 2, 'APPROVED', 1,
        'Tháng 1 đã hoàn thành.', 1, 3, 'tl_phay01'),
       (6, 'TP-PH-2026-002', 'Kế hoạch HLV T2/2026 - Tổ Phay Ca Ngày', '2026-02-03', '2026-02-28', 2, 2, 'APPROVED', 1,
        'Tháng 2 đã hoàn thành.', 1, 3, 'tl_phay01'),
       (7, 'TP-PH-2026-003', 'Kế hoạch HLV T3/2026 - Tổ Phay Ca Ngày', '2026-03-03', '2026-03-31', 2, 2, 'WAITING_SV',
        1, 'Chờ giám sát Lê duyệt.', 1, 3, 'tl_phay01'),
       (8, 'TP-HA-2026-001', 'Kế hoạch HLV T3/2026 - Tổ Hàn & Nhiệt', '2026-03-03', '2026-03-31', 3, 3,
        'REJECTED_BY_SV', 1, 'Bị trả vì thiếu NV016 trong lịch.', 1, 3, 'tl_hanlap01'),
       (9, 'TP-B-2026-001', 'Kế hoạch HLV T2/2026 - Tổ Lắp Ráp Bơm', '2026-02-03', '2026-02-28', 4, 5, 'APPROVED', 1,
        'Tháng 2 lắp ráp bơm hoàn tất.', 1, 3, 'tl_laprap01'),
       (10, 'TP-B-2026-002', 'Kế hoạch HLV T3/2026 - Tổ Lắp Ráp Bơm', '2026-03-03', '2026-03-31', 4, 5,
        'WAITING_MANAGER', 1, 'Đã qua SV, chờ MG duyệt cuối.', 1, 3, 'tl_laprap01'),
       (11, 'TP-DC-2026-001', 'Kế hoạch HLV T1/2026 - Tổ Lắp Ráp Động Cơ', '2026-01-05', '2026-01-31', 5, 4, 'APPROVED',
        1, 'Tháng 1 hoàn tất.', 1, 2, 'tl_dongco01'),
       (12, 'TP-DC-2026-002', 'Kế hoạch HLV T2/2026 - Tổ Lắp Ráp Động Cơ', '2026-02-03', '2026-02-28', 5, 4, 'APPROVED',
        1, 'Tháng 2 hoàn tất.', 1, 2, 'tl_dongco01'),
       (13, 'TP-DC-2026-003', 'Kế hoạch HLV T3/2026 - Tổ Lắp Ráp Động Cơ', '2026-03-03', '2026-03-31', 5, 4,
        'WAITING_SV', 1, 'Chờ SV Phạm duyệt.', 1, 2, 'tl_dongco01'),
       (14, 'TP-KCS-2026-001', 'Kế hoạch HLV T2/2026 - Tổ KCS', '2026-02-03', '2026-02-28', 6, 5, 'APPROVED', 1,
        'Tổ KCS tháng 2.', 1, 2, 'tl_kcs01'),
       (15, 'TP-KCS-2026-002', 'Kế hoạch HLV T3/2026 - Tổ KCS', '2026-03-03', '2026-03-31', 6, 5, 'DRAFT', 1,
        'Đang soạn thảo.', 1, 2, 'tl_kcs01');

-- Training Plan Details (60 dòng chi tiết)
INSERT INTO training_plan_details (id, training_plan_id, employee_id, batch_id, target_month,
                                   planned_date, actual_date, status, note, created_by)
VALUES
-- Plan 1: Tổ Tiện T1/2026 (APPROVED, T1)
(1, 1, 1, 'bp1-nv001-a', '2026-01-01', '2026-01-07', '2026-01-07', 'DONE', 'NV001 - TS0001 đạt', 'tl_tien01'),
(2, 1, 2, 'bp1-nv002-a', '2026-01-01', '2026-01-08', '2026-01-08', 'DONE', 'NV002 - TS0001 đạt', 'tl_tien01'),
(3, 1, 3, 'bp1-nv003-a', '2026-01-01', '2026-01-09', '2026-01-09', 'DONE', 'NV003 - TS0002 đạt', 'tl_tien01'),
(4, 1, 4, 'bp1-nv004-a', '2026-01-01', '2026-01-10', NULL, 'MISSED', 'NV004 nghỉ ốm', 'tl_tien01'),
(5, 1, 6, 'bp1-nv006-a', '2026-01-01', '2026-01-14', '2026-01-14', 'DONE', 'NV006 - TS0003 đạt', 'tl_tien01'),
-- Plan 2: Tổ Tiện T2/2026 (APPROVED, T2)
(6, 2, 1, 'bp2-nv001-a', '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV001 đạt lần 2', 'tl_tien01'),
(7, 2, 2, 'bp2-nv002-a', '2026-02-01', '2026-02-06', '2026-02-06', 'DONE', 'NV002 đạt lần 2', 'tl_tien01'),
(8, 2, 3, 'bp2-nv003-a', '2026-02-01', '2026-02-10', '2026-02-10', 'DONE', 'NV003 đạt', 'tl_tien01'),
(9, 2, 4, 'bp2-nv004-a', '2026-02-01', '2026-02-12', '2026-02-12', 'DONE', 'NV004 bổ sung T2', 'tl_tien01'),
(10, 2, 6, 'bp2-nv006-a', '2026-02-01', '2026-02-18', '2026-02-18', 'DONE', 'NV006 đạt', 'tl_tien01'),
-- Plan 3: Tổ Tiện T3/2026 (APPROVED, T3)
(11, 3, 1, 'bp3-nv001-a', '2026-03-01', '2026-03-05', '2026-03-05', 'DONE', 'NV001 lần 3', 'tl_tien01'),
(12, 3, 2, 'bp3-nv002-a', '2026-03-01', '2026-03-06', '2026-03-06', 'DONE', 'NV002 lần 3', 'tl_tien01'),
(13, 3, 3, 'bp3-nv003-a', '2026-03-01', '2026-03-11', NULL, 'PENDING', 'NV003 - chưa làm', 'tl_tien01'),
(14, 3, 4, 'bp3-nv004-a', '2026-03-01', '2026-03-12', NULL, 'PENDING', 'NV004 lần bổ sung', 'tl_tien01'),
(15, 3, 6, 'bp3-nv006-a', '2026-03-01', '2026-03-07', '2026-03-07', 'DONE', 'NV006 đạt', 'tl_tien01'),
-- Plan 5: Tổ Phay T1/2026 (APPROVED)
(16, 5, 7, 'bp5-nv007-a', '2026-01-01', '2026-01-07', '2026-01-07', 'DONE', 'NV007 - TS0007 đạt', 'tl_phay01'),
(17, 5, 8, 'bp5-nv008-a', '2026-01-01', '2026-01-08', '2026-01-08', 'DONE', 'NV008 - TS0007 đạt', 'tl_phay01'),
(18, 5, 9, 'bp5-nv009-a', '2026-01-01', '2026-01-09', '2026-01-09', 'DONE', 'NV009 đạt', 'tl_phay01'),
(19, 5, 10, 'bp5-nv010-a', '2026-01-01', '2026-01-14', NULL, 'MISSED', 'NV010 nghỉ phép', 'tl_phay01'),
(20, 5, 12, 'bp5-nv012-a', '2026-01-01', '2026-01-15', '2026-01-15', 'DONE', 'NV012 đạt', 'tl_phay01'),
-- Plan 6: Tổ Phay T2/2026 (APPROVED)
(21, 6, 7, 'bp6-nv007-a', '2026-02-01', '2026-02-04', '2026-02-04', 'DONE', 'NV007 lần 2', 'tl_phay01'),
(22, 6, 8, 'bp6-nv008-a', '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV008 lần 2', 'tl_phay01'),
(23, 6, 9, 'bp6-nv009-a', '2026-02-01', '2026-02-11', '2026-02-11', 'DONE', 'NV009 lần 2', 'tl_phay01'),
(24, 6, 10, 'bp6-nv010-a', '2026-02-01', '2026-02-18', '2026-02-18', 'DONE', 'NV010 bổ sung T2', 'tl_phay01'),
(25, 6, 12, 'bp6-nv012-a', '2026-02-01', '2026-02-20', '2026-02-20', 'DONE', 'NV012 lần 2', 'tl_phay01'),
-- Plan 7: Tổ Phay T3/2026 (WAITING_SV)
(26, 7, 7, 'bp7-nv007-a', '2026-03-01', '2026-03-06', NULL, 'PENDING', 'Chờ duyệt kế hoạch', 'tl_phay01'),
(27, 7, 8, 'bp7-nv008-a', '2026-03-01', '2026-03-07', NULL, 'PENDING', 'Chờ duyệt kế hoạch', 'tl_phay01'),
(28, 7, 9, 'bp7-nv009-a', '2026-03-01', '2026-03-12', NULL, 'PENDING', 'Chờ duyệt kế hoạch', 'tl_phay01'),
(29, 7, 10, 'bp7-nv010-a', '2026-03-01', '2026-03-13', NULL, 'PENDING', 'Chờ duyệt kế hoạch', 'tl_phay01'),
(30, 7, 12, 'bp7-nv012-a', '2026-03-01', '2026-03-14', NULL, 'PENDING', 'Chờ duyệt kế hoạch', 'tl_phay01'),
-- Plan 9: Tổ Lắp Ráp Bơm T2/2026 (APPROVED)
(31, 9, 17, 'bp9-nv017-a', '2026-02-01', '2026-02-04', '2026-02-04', 'DONE', 'NV017 - TS0023 đạt', 'tl_laprap01'),
(32, 9, 18, 'bp9-nv018-a', '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV018 đạt', 'tl_laprap01'),
(33, 9, 19, 'bp9-nv019-a', '2026-02-01', '2026-02-06', '2026-02-06', 'DONE', 'NV019 đạt', 'tl_laprap01'),
(34, 9, 20, 'bp9-nv020-a', '2026-02-01', '2026-02-11', '2026-02-11', 'DONE', 'NV020 đạt', 'tl_laprap01'),
-- Plan 10: Tổ Lắp Ráp Bơm T3/2026 (WAITING_MANAGER)
(35, 10, 17, 'bp10-nv017-a', '2026-03-01', '2026-03-05', NULL, 'PENDING', 'Chờ MG duyệt', 'tl_laprap01'),
(36, 10, 18, 'bp10-nv018-a', '2026-03-01', '2026-03-06', NULL, 'PENDING', 'Chờ MG duyệt', 'tl_laprap01'),
(37, 10, 19, 'bp10-nv019-a', '2026-03-01', '2026-03-07', NULL, 'PENDING', 'Chờ MG duyệt', 'tl_laprap01'),
(38, 10, 20, 'bp10-nv020-a', '2026-03-01', '2026-03-10', NULL, 'PENDING', 'Chờ MG duyệt', 'tl_laprap01'),
-- Plan 11: Tổ Động Cơ T1/2026 (APPROVED)
(39, 11, 22, 'bp11-nv022-a', '2026-01-01', '2026-01-07', '2026-01-07', 'DONE', 'NV022 - TS0018 đạt', 'tl_dongco01'),
(40, 11, 23, 'bp11-nv023-a', '2026-01-01', '2026-01-08', '2026-01-08', 'DONE', 'NV023 đạt', 'tl_dongco01'),
(41, 11, 24, 'bp11-nv024-a', '2026-01-01', '2026-01-09', '2026-01-09', 'DONE', 'NV024 đạt', 'tl_dongco01'),
(42, 11, 25, 'bp11-nv025-a', '2026-01-01', '2026-01-10', '2026-01-10', 'DONE', 'NV025 đạt', 'tl_dongco01'),
(43, 11, 26, 'bp11-nv026-a', '2026-01-01', '2026-01-14', NULL, 'MISSED', 'NV026 vắng không lý do', 'tl_dongco01'),
-- Plan 12: Tổ Động Cơ T2/2026 (APPROVED)
(44, 12, 22, 'bp12-nv022-a', '2026-02-01', '2026-02-04', '2026-02-04', 'DONE', 'NV022 lần 2', 'tl_dongco01'),
(45, 12, 23, 'bp12-nv023-a', '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV023 lần 2', 'tl_dongco01'),
(46, 12, 24, 'bp12-nv024-a', '2026-02-01', '2026-02-11', '2026-02-11', 'DONE', 'NV024 lần 2', 'tl_dongco01'),
(47, 12, 25, 'bp12-nv025-a', '2026-02-01', '2026-02-12', '2026-02-12', 'DONE', 'NV025 lần 2', 'tl_dongco01'),
(48, 12, 26, 'bp12-nv026-a', '2026-02-01', '2026-02-18', '2026-02-18', 'DONE', 'NV026 bổ sung T2', 'tl_dongco01'),
-- Plan 14: Tổ KCS T2/2026 (APPROVED)
(49, 14, 27, 'bp14-nv027-a', '2026-02-01', '2026-02-04', '2026-02-04', 'DONE', 'NV027 đạt', 'tl_kcs01'),
(50, 14, 28, 'bp14-nv028-a', '2026-02-01', '2026-02-05', '2026-02-05', 'DONE', 'NV028 đạt', 'tl_kcs01'),
(51, 14, 29, 'bp14-nv029-a', '2026-02-01', '2026-02-06', '2026-02-06', 'DONE', 'NV029 đạt', 'tl_kcs01'),
(52, 14, 30, 'bp14-nv030-a', '2026-02-01', '2026-02-11', '2026-02-11', 'DONE', 'NV030 đạt', 'tl_kcs01'),
-- Plan 8: Tổ Hàn (REJECTED_BY_SV)
(53, 8, 13, 'bp8-nv013-a', '2026-03-01', '2026-03-05', NULL, 'PENDING', 'Kế hoạch bị trả', 'tl_hanlap01'),
(54, 8, 14, 'bp8-nv014-a', '2026-03-01', '2026-03-06', NULL, 'PENDING', 'Kế hoạch bị trả', 'tl_hanlap01'),
(55, 8, 15, 'bp8-nv015-a', '2026-03-01', '2026-03-07', NULL, 'PENDING', 'Kế hoạch bị trả', 'tl_hanlap01'),
(56, 8, 16, 'bp8-nv016-a', '2026-03-01', '2026-03-10', NULL, 'PENDING', 'NV016 thiếu trong lịch', 'tl_hanlap01'),
-- Plan 13: Tổ Động Cơ T3 (WAITING_SV)
(57, 13, 22, 'bp13-nv022-a', '2026-03-01', '2026-03-05', NULL, 'PENDING', 'Chờ SV', 'tl_dongco01'),
(58, 13, 23, 'bp13-nv023-a', '2026-03-01', '2026-03-06', NULL, 'PENDING', 'Chờ SV', 'tl_dongco01'),
(59, 13, 24, 'bp13-nv024-a', '2026-03-01', '2026-03-12', NULL, 'PENDING', 'Chờ SV', 'tl_dongco01'),
(60, 13, 26, 'bp13-nv026-a', '2026-03-01', '2026-03-13', NULL, 'PENDING', 'Chờ SV', 'tl_dongco01');

-- ============================================================================
-- PART 9: TRAINING RESULTS (cho các plan APPROVED đã có DONE details)
-- ============================================================================

-- training_results schema: id, training_plan_id, title, form_code, year(NOT NULL),
--   team_id, line_id, status(ON_GOING|DONE|WAITING_MANAGER|REJECTED_BY_MANAGER|APPROVED),
--   current_version, note, created_by
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id,
                              status, current_version, note, created_by)
VALUES (1, 1, 'Kết quả HLV T1/2026 – Tổ Tiện', 'TR-RES-TI-001', 2026, 1, 1, 'APPROVED', 1,
        'Tổ Tiện T1/2026 hoàn thành. 4/5 nhân viên đạt.', 'tl_tien01'),
       (2, 2, 'Kết quả HLV T2/2026 – Tổ Tiện', 'TR-RES-TI-002', 2026, 1, 1, 'APPROVED', 1,
        'Tổ Tiện T2/2026 hoàn thành. 5/5 đạt.', 'tl_tien01'),
       (3, 5, 'Kết quả HLV T1/2026 – Tổ Phay', 'TR-RES-PH-001', 2026, 2, 2, 'APPROVED', 1,
        'Tổ Phay T1/2026 hoàn thành. 4/5 đạt.', 'tl_phay01'),
       (4, 6, 'Kết quả HLV T2/2026 – Tổ Phay', 'TR-RES-PH-002', 2026, 2, 2, 'APPROVED', 1,
        'Tổ Phay T2/2026 hoàn thành. 5/5 đạt.', 'tl_phay01'),
       (5, 9, 'Kết quả HLV T2/2026 – Tổ Lắp Bơm', 'TR-RES-B-001', 2026, 4, 5, 'APPROVED', 1,
        'Tổ Lắp Ráp Bơm T2/2026 hoàn thành. 4/4 đạt.', 'tl_laprap01'),
       (6, 11, 'Kết quả HLV T1/2026 – Tổ Động Cơ', 'TR-RES-DC-001', 2026, 5, 4, 'APPROVED', 1,
        'Tổ ĐC T1/2026. 4/5 đạt (NV026 missed).', 'tl_dongco01'),
       (7, 12, 'Kết quả HLV T2/2026 – Tổ Động Cơ', 'TR-RES-DC-002', 2026, 5, 4, 'APPROVED', 1,
        'Tổ ĐC T2/2026 hoàn thành. 5/5 đạt.', 'tl_dongco01'),
       (8, 14, 'Kết quả HLV T2/2026 – Tổ KCS', 'TR-RES-KCS-001', 2026, 6, 5, 'APPROVED', 1,
        'Tổ KCS T2/2026 hoàn thành. 4/4 đạt.', 'tl_kcs01'),
       (9, 3, 'Kết quả HLV T3/2026 – Tổ Tiện', 'TR-RES-TI-003', 2026, 1, 1, 'WAITING_MANAGER', 1,
        'Tổ Tiện T3/2026 đã nộp, chờ Manager duyệt.', 'tl_tien01');

-- training_result_details schema: id, training_result_id, training_plan_detail_id,
--   employee_id, process_id, training_sample_id, planned_date(NOT NULL), actual_date,
--   status(PENDING|DONE|NEED_SIGN|WAITING_SV|REJECTED_BY_SV|APPROVED), is_pass, note, created_by
-- NOTE: không có cột 'score'
INSERT INTO training_result_details (id, training_result_id, training_plan_detail_id, employee_id,
                                     training_sample_id, planned_date, actual_date, status, is_pass, note, created_by)
VALUES
-- Result 1: Tổ Tiện T1
(1, 1, 1, 1, 1, '2026-01-07', '2026-01-07', 'APPROVED', TRUE, 'Đạt yêu cầu', 'tl_tien01'),
(2, 1, 2, 2, 1, '2026-01-08', '2026-01-08', 'APPROVED', TRUE, 'Đạt yêu cầu', 'tl_tien01'),
(3, 1, 3, 3, 2, '2026-01-09', '2026-01-09', 'APPROVED', TRUE, 'Xuất sắc', 'tl_tien01'),
(4, 1, 5, 6, 3, '2026-01-14', '2026-01-14', 'APPROVED', TRUE, 'Đạt', 'tl_tien01'),
-- Result 2: Tổ Tiện T2
(5, 2, 6, 1, 4, '2026-02-05', '2026-02-05', 'APPROVED', TRUE, 'Đạt', 'tl_tien01'),
(6, 2, 7, 2, 4, '2026-02-06', '2026-02-06', 'APPROVED', TRUE, 'Đạt', 'tl_tien01'),
(7, 2, 8, 3, 5, '2026-02-10', '2026-02-10', 'APPROVED', TRUE, 'Đạt', 'tl_tien01'),
(8, 2, 9, 4, 1, '2026-02-12', '2026-02-12', 'APPROVED', TRUE, 'Đạt bổ sung', 'tl_tien01'),
(9, 2, 10, 6, 6, '2026-02-18', '2026-02-18', 'APPROVED', TRUE, 'Đạt', 'tl_tien01'),
-- Result 3: Tổ Phay T1
(10, 3, 16, 7, 7, '2026-01-07', '2026-01-07', 'APPROVED', TRUE, 'Xuất sắc', 'tl_phay01'),
(11, 3, 17, 8, 7, '2026-01-08', '2026-01-08', 'APPROVED', TRUE, 'Đạt', 'tl_phay01'),
(12, 3, 18, 9, 8, '2026-01-09', '2026-01-09', 'APPROVED', TRUE, 'Đạt', 'tl_phay01'),
(13, 3, 20, 12, 9, '2026-01-15', '2026-01-15', 'APPROVED', FALSE, 'Chưa đạt – tái huấn luyện', 'tl_phay01'),
-- Result 4: Tổ Phay T2
(14, 4, 21, 7, 10, '2026-02-04', '2026-02-04', 'APPROVED', TRUE, 'Đạt', 'tl_phay01'),
(15, 4, 22, 8, 11, '2026-02-05', '2026-02-05', 'APPROVED', TRUE, 'Đạt', 'tl_phay01'),
(16, 4, 23, 9, 12, '2026-02-11', '2026-02-11', 'APPROVED', TRUE, 'Đạt', 'tl_phay01'),
(17, 4, 24, 10, 7, '2026-02-18', '2026-02-18', 'APPROVED', TRUE, 'Bổ sung T2 đạt', 'tl_phay01'),
(18, 4, 25, 12, 8, '2026-02-20', '2026-02-20', 'APPROVED', TRUE, 'Đạt', 'tl_phay01'),
-- Result 5: Tổ Lắp Ráp Bơm T2
(19, 5, 31, 17, 23, '2026-02-04', '2026-02-04', 'APPROVED', TRUE, 'Đạt', 'tl_laprap01'),
(20, 5, 32, 18, 24, '2026-02-05', '2026-02-05', 'APPROVED', TRUE, 'Đạt', 'tl_laprap01'),
(21, 5, 33, 19, 25, '2026-02-06', '2026-02-06', 'APPROVED', TRUE, 'Đạt', 'tl_laprap01'),
(22, 5, 34, 20, 23, '2026-02-11', '2026-02-11', 'APPROVED', TRUE, 'Đạt', 'tl_laprap01'),
-- Result 6: Tổ ĐC T1
(23, 6, 39, 22, 18, '2026-01-07', '2026-01-07', 'APPROVED', TRUE, 'Xuất sắc', 'tl_dongco01'),
(24, 6, 40, 23, 19, '2026-01-08', '2026-01-08', 'APPROVED', TRUE, 'Đạt', 'tl_dongco01'),
(25, 6, 41, 24, 20, '2026-01-09', '2026-01-09', 'APPROVED', TRUE, 'Đạt', 'tl_dongco01'),
(26, 6, 42, 25, 21, '2026-01-10', '2026-01-10', 'APPROVED', TRUE, 'Đạt', 'tl_dongco01'),
-- Result 7: Tổ ĐC T2
(27, 7, 44, 22, 22, '2026-02-04', '2026-02-04', 'APPROVED', TRUE, 'Đạt', 'tl_dongco01'),
(28, 7, 45, 23, 18, '2026-02-05', '2026-02-05', 'APPROVED', TRUE, 'Đạt', 'tl_dongco01'),
(29, 7, 46, 24, 19, '2026-02-11', '2026-02-11', 'APPROVED', TRUE, 'Đạt', 'tl_dongco01'),
(30, 7, 47, 25, 20, '2026-02-12', '2026-02-12', 'APPROVED', TRUE, 'Đạt', 'tl_dongco01'),
(31, 7, 48, 26, 21, '2026-02-18', '2026-02-18', 'APPROVED', FALSE, 'Chưa đạt – NV026 tái đào tạo', 'tl_dongco01'),
-- Result 8: Tổ KCS T2
(32, 8, 49, 27, 25, '2026-02-04', '2026-02-04', 'APPROVED', TRUE, 'Đạt', 'tl_kcs01'),
(33, 8, 50, 28, 26, '2026-02-05', '2026-02-05', 'APPROVED', TRUE, 'Đạt', 'tl_kcs01'),
(34, 8, 51, 29, 27, '2026-02-06', '2026-02-06', 'APPROVED', TRUE, 'Xuất sắc', 'tl_kcs01'),
(35, 8, 52, 30, 28, '2026-02-11', '2026-02-11', 'APPROVED', TRUE, 'Đạt', 'tl_kcs01'),
-- Result 9: Tổ Tiện T3 (WAITING_MANAGER – partial)
(36, 9, 11, 1, 2, '2026-03-05', '2026-03-05', 'DONE', TRUE, 'Đạt T3', 'tl_tien01'),
(37, 9, 12, 2, 4, '2026-03-06', '2026-03-06', 'DONE', TRUE, 'Đạt T3', 'tl_tien01'),
(38, 9, 15, 6, 5, '2026-03-07', '2026-03-07', 'DONE', TRUE, 'Đạt T3', 'tl_tien01');

-- ============================================================================
-- PART 10: PRIORITY SYSTEM – PROCESS ENTITY
-- Chính sách ưu tiên cho Công đoạn (PROCESS)
-- Logic: Ưu tiên dựa trên tầm quan trọng + số lỗi + đặc điểm kỹ thuật
-- ============================================================================

-- 10.1 COMPUTED METRICS cho PROCESS
INSERT INTO computed_metrics (metric_name, display_name, entity_type, compute_method,
                              compute_definition, return_type, unit, description, is_active, created_by)
VALUES ('process_classification',
        'Phân loại công đoạn',
        'PROCESS',
        'CLASSIFICATION',
        'classification_level',
        'INT', 'Cấp',
        'Phân loại mức độ quan trọng: 1=FI bắt buộc, 2=KCS kiểm, 3=Tự kiểm, 4=Thường',
        TRUE, 'system'),

       ('total_defects_6m',
        'Số lỗi 6 tháng gần nhất',
        'PROCESS',
        'SQL',
        'SELECT COUNT(*) FROM defects d JOIN defect_proposal_details dpd ON dpd.defect_id = d.id WHERE dpd.process_id = :entityId AND d.detected_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) AND d.delete_flag = FALSE',
        'INT', 'Lỗi',
        'Tổng số lỗi ghi nhận tại công đoạn trong 6 tháng gần nhất',
        TRUE, 'system'),

       ('total_defects_all',
        'Tổng số lỗi lịch sử',
        'PROCESS',
        'SQL',
        'SELECT COUNT(*) FROM defects d JOIN defect_proposal_details dpd ON dpd.defect_id = d.id WHERE dpd.process_id = :entityId AND d.delete_flag = FALSE',
        'INT', 'Lỗi',
        'Tổng số lỗi tất cả thời gian tại công đoạn',
        TRUE, 'system'),

       ('claim_defect_count',
        'Số lỗi khách hàng (CLAIM)',
        'PROCESS',
        'SQL',
        'SELECT COUNT(*) FROM defects d JOIN defect_proposal_details dpd ON dpd.defect_id = d.id WHERE dpd.process_id = :entityId AND d.defect_type = ''CLAIM'' AND d.delete_flag = FALSE',
        'INT', 'Lỗi',
        'Số lỗi từ khiếu nại khách hàng tại công đoạn',
        TRUE, 'system'),

       ('days_since_last_training',
        'Ngày kể từ lần đào tạo cuối',
        'PROCESS',
        'SQL',
        'SELECT DATEDIFF(CURDATE(), MAX(trd.actual_date)) FROM training_result_details trd JOIN training_plan_details tpd ON trd.training_plan_detail_id = tpd.id JOIN training_samples ts ON trd.training_sample_id = ts.id WHERE ts.process_id = :entityId AND trd.is_pass = TRUE AND trd.delete_flag = FALSE',
        'INT', 'Ngày',
        'Số ngày kể từ lần đào tạo mẫu huấn luyện đạt gần nhất cho công đoạn này',
        TRUE, 'system'),

       ('fail_rate_process',
        'Tỷ lệ trượt đào tạo tại công đoạn',
        'PROCESS',
        'SQL',
        'SELECT ROUND(COUNT(CASE WHEN trd.is_pass = FALSE THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 2) FROM training_result_details trd JOIN training_samples ts ON trd.training_sample_id = ts.id WHERE ts.process_id = :entityId AND trd.delete_flag = FALSE',
        'DECIMAL', '%',
        'Tỷ lệ % nhân viên không đạt khi học mẫu HLV của công đoạn này',
        TRUE, 'system'),

       ('active_employee_count',
        'Số nhân viên đang làm tại công đoạn',
        'PROCESS',
        'SQL',
        'SELECT COUNT(DISTINCT es.employee_id) FROM employee_skills es JOIN employees e ON es.employee_id = e.id WHERE es.process_id = :entityId AND e.status = ''ACTIVE'' AND e.delete_flag = FALSE',
        'INT', 'Người',
        'Số nhân viên ACTIVE được phân công tại công đoạn',
        TRUE, 'system'),

       ('standard_time_jt',
        'Thời gian tiêu chuẩn (giây)',
        'PROCESS',
        'PROPERTY',
        'process.standardTimeJt',
        'DECIMAL', 'Giây',
        'Thời gian gia công tiêu chuẩn của công đoạn theo JT',
        TRUE, 'system');

-- 10.2 METRIC CLASSIFICATIONS (cho process_classification)
INSERT INTO metric_classifications (classification_name, metric_source, condition_expression,
                                    output_level, output_label, priority, is_active, created_by)
VALUES ('process_importance', 'process_classification', 'classification = 1', 1, 'Đặc biệt quan trọng – FI ký', 1, TRUE,
        'system'),
       ('process_importance', 'process_classification', 'classification = 2', 2, 'Quan trọng – KCS kiểm tra', 2, TRUE,
        'system'),
       ('process_importance', 'process_classification', 'classification = 3', 3, 'Trung bình – Tự kiểm', 3, TRUE,
        'system'),
       ('process_importance', 'process_classification', 'classification = 4', 4, 'Thường – Không ưu tiên', 4, TRUE,
        'system');

-- 10.3 PRIORITY POLICIES (2 chính sách PROCESS)
INSERT INTO priority_policies (id, policy_code, policy_name, entity_type,
                               effective_date, expiration_date, status, description, created_by)
VALUES (1, 'PROC-RISK-2026',
        'Chính sách ưu tiên huấn luyện theo Rủi ro Công đoạn 2026',
        'PROCESS',
        '2026-01-01', '2026-12-31',
        'ACTIVE',
        'Xếp hạng công đoạn theo tầm quan trọng kỹ thuật kết hợp lịch sử lỗi. '
            'Ưu tiên cao nhất cho công đoạn classification 1 có nhiều lỗi khách hàng.',
        'nguyen.quanly'),
       (2, 'PROC-DEFECT-FOCUS-2026',
        'Chính sách ưu tiên tập trung giảm lỗi khách hàng 2026',
        'PROCESS',
        '2026-01-01', '2026-12-31',
        'ACTIVE',
        'Tập trung vào các công đoạn có nhiều CLAIM khách hàng và tỷ lệ trượt đào tạo cao. '
            'Bổ sung tiêu chí ngày chưa đào tạo để phát hiện vùng trắng.',
        'nguyen.quanly');

-- 10.4 PRIORITY TIERS (cho Policy 1: PROC-RISK-2026)
-- Tier 1: Công đoạn classification 1 (đặc biệt quan trọng) → ưu tiên tuyệt đối
-- Tier 2: Cls 1 hoặc 2 có ≥ 3 lỗi KH
-- Tier 3: Còn lại cls 2-3 có lỗi trong 6T
-- Tier 4: Tất cả cls 3-4 còn lại
INSERT INTO priority_tiers (id, policy_id, tier_order, tier_name, filter_logic,
                            ranking_metric, ranking_direction, secondary_metric, secondary_direction, is_active,
                            created_by)
VALUES (1, 1, 1, 'Tier 1 – Công đoạn FI (Đặc biệt quan trọng + Có lỗi KH)',
        'AND', 'claim_defect_count', 'DESC', 'total_defects_6m', 'DESC', TRUE, 'system'),
       (2, 1, 2, 'Tier 2 – Công đoạn KCS (Quan trọng + Lỗi cao)',
        'AND', 'total_defects_6m', 'DESC', 'claim_defect_count', 'DESC', TRUE, 'system'),
       (3, 1, 3, 'Tier 3 – Công đoạn Tự Kiểm + Đào tạo lâu ngày',
        'OR', 'days_since_last_training', 'DESC', 'total_defects_all', 'DESC', TRUE, 'system'),
       (4, 1, 4, 'Tier 4 – Công đoạn Thường (Ưu tiên cuối)',
        'AND', 'standard_time_jt', 'DESC', NULL, NULL, TRUE, 'system');

-- 10.5 PRIORITY TIER FILTERS (Policy 1)
INSERT INTO priority_tier_filters (tier_id, metric_name, operator, filter_value, filter_unit, filter_order, created_by)
VALUES
-- Tier 1: classification = 1 VÀ có ít nhất 1 lỗi CLAIM
(1, 'process_classification', 'EQ', '1', 'Cấp', 1, 'system'),
(1, 'claim_defect_count', 'GTE', '1', 'Lỗi', 2, 'system'),
-- Tier 2: classification = 2 VÀ tổng lỗi 6T >= 2
(2, 'process_classification', 'EQ', '2', 'Cấp', 1, 'system'),
(2, 'total_defects_6m', 'GTE', '2', 'Lỗi', 2, 'system'),
-- Tier 3: (classification IN 2,3) OR (ngày chưa train > 90 ngày)
(3, 'process_classification', 'LTE', '3', 'Cấp', 1, 'system'),
(3, 'days_since_last_training', 'GTE', '90', 'Ngày', 2, 'system'),
-- Tier 4: classification = 4 (thường)
(4, 'process_classification', 'EQ', '4', 'Cấp', 1, 'system');

-- 10.6 PRIORITY TIERS (cho Policy 2: PROC-DEFECT-FOCUS-2026)
INSERT INTO priority_tiers (id, policy_id, tier_order, tier_name, filter_logic,
                            ranking_metric, ranking_direction, secondary_metric, secondary_direction, is_active,
                            created_by)
VALUES (5, 2, 1, 'Focus Tier 1 – CLAIM cao + Tỷ lệ trượt HLV cao',
        'AND', 'claim_defect_count', 'DESC', 'fail_rate_process', 'DESC', TRUE, 'system'),
       (6, 2, 2, 'Focus Tier 2 – Lỗi nhiều + Chưa đào tạo lâu',
        'AND', 'total_defects_6m', 'DESC', 'days_since_last_training', 'DESC', TRUE, 'system'),
       (7, 2, 3, 'Focus Tier 3 – Công đoạn quan trọng chưa ổn định',
        'OR', 'fail_rate_process', 'DESC', 'claim_defect_count', 'DESC', TRUE, 'system');

INSERT INTO priority_tier_filters (tier_id, metric_name, operator, filter_value, filter_unit, filter_order, created_by)
VALUES
-- Focus Tier 1: claim >= 3 VÀ fail_rate >= 15%
(5, 'claim_defect_count', 'GTE', '3', 'Lỗi', 1, 'system'),
(5, 'fail_rate_process', 'GTE', '15', '%', 2, 'system'),
-- Focus Tier 2: total_defects_6m >= 2 VÀ ngày chưa train >= 60
(6, 'total_defects_6m', 'GTE', '2', 'Lỗi', 1, 'system'),
(6, 'days_since_last_training', 'GTE', '60', 'Ngày', 2, 'system'),
-- Focus Tier 3: fail_rate > 10% OR claim > 1
(7, 'fail_rate_process', 'GT', '10', '%', 1, 'system'),
(7, 'claim_defect_count', 'GT', '1', 'Lỗi', 2, 'system');

-- 10.7 PRIORITY SNAPSHOTS (Kết quả xếp hạng giả lập cho Tổ Lắp Ráp Bơm T3/2026)
INSERT INTO priority_snapshots (id, team_id, policy_id, policy_snapshot, training_plan_id, created_by)
VALUES (1, 4, 1,
        JSON_OBJECT(
                'policy_code', 'PROC-RISK-2026',
                'policy_name', 'Chính sách ưu tiên huấn luyện theo Rủi ro Công đoạn 2026',
                'snapshot_at', '2026-03-01',
                'tiers', JSON_ARRAY(
                        JSON_OBJECT('tier_order', 1, 'tier_name', 'Tier 1 – FI + Lỗi KH', 'filter_logic', 'AND'),
                        JSON_OBJECT('tier_order', 2, 'tier_name', 'Tier 2 – KCS + Lỗi cao', 'filter_logic', 'AND'),
                        JSON_OBJECT('tier_order', 3, 'tier_name', 'Tier 3 – Tự Kiểm + Lâu ngày', 'filter_logic', 'OR'),
                        JSON_OBJECT('tier_order', 4, 'tier_name', 'Tier 4 – Thường', 'filter_logic', 'AND')
                         )
        ),
        10,
        'system'),

       (2, 5, 2,
        JSON_OBJECT(
                'policy_code', 'PROC-DEFECT-FOCUS-2026',
                'policy_name', 'Chính sách ưu tiên tập trung giảm lỗi khách hàng 2026',
                'snapshot_at', '2026-03-01',
                'tiers', JSON_ARRAY(
                        JSON_OBJECT('tier_order', 1, 'tier_name', 'Focus Tier 1 – CLAIM + Fail Rate', 'filter_logic',
                                    'AND'),
                        JSON_OBJECT('tier_order', 2, 'tier_name', 'Focus Tier 2 – Lỗi + Chưa train', 'filter_logic',
                                    'AND'),
                        JSON_OBJECT('tier_order', 3, 'tier_name', 'Focus Tier 3 – Không ổn định', 'filter_logic', 'OR')
                         )
        ),
        13,
        'system');

-- 10.8 PRIORITY SNAPSHOT DETAILS (xếp hạng 21 công đoạn theo Policy 1)
-- process_id → tier_order, sort_rank, metric_values (giả lập thực tế)
INSERT INTO priority_snapshot_details (snapshot_id, employee_id, employee_code, full_name,
                                       tier_order, tier_name, sort_rank, priority_tags, metric_values, created_by)
VALUES
-- NOTE: Ở đây employee_id/code/full_name đang được dùng để lưu PROCESS (bảng thiết kế cũ employee-based)
-- Để phù hợp schema, ta map process_id → employee_id (giữ nguyên FK employee nhưng note là PROCESS context)
-- Snapshot 1 – Policy PROC-RISK-2026, Team 4 (Lắp Ráp Bơm), Line 5
(1, 1, 'PROC-LA-B-03', 'Test Áp Lực Thủy Lực',
 1, 'Tier 1 – Công đoạn FI', 1,
 JSON_ARRAY('FI_REQUIRED', 'HIGH_CLAIM', 'URGENT'),
 JSON_OBJECT('process_classification', 1, 'claim_defect_count', 4, 'total_defects_6m', 5, 'days_since_last_training',
             72, 'fail_rate_process', 14.3, 'standard_time_jt', 60),
 'system'),
(1, 2, 'PROC-LA-B-02', 'Lắp Ron Cao Su & Gioăng',
 1, 'Tier 1 – Công đoạn FI', 2,
 JSON_ARRAY('FI_REQUIRED', 'HIGH_CLAIM'),
 JSON_OBJECT('process_classification', 1, 'claim_defect_count', 3, 'total_defects_6m', 4, 'days_since_last_training',
             45, 'fail_rate_process', 8.0, 'standard_time_jt', 45),
 'system'),
(1, 3, 'PROC-LA-B-01', 'Lắp Bánh Công Tác',
 1, 'Tier 1 – Công đoạn FI', 3,
 JSON_ARRAY('FI_REQUIRED', 'MEDIUM_CLAIM'),
 JSON_OBJECT('process_classification', 1, 'claim_defect_count', 2, 'total_defects_6m', 3, 'days_since_last_training',
             38, 'fail_rate_process', 5.0, 'standard_time_jt', 80),
 'system'),
(1, 4, 'PROC-LA-B-04', 'Đóng Gói & Dán Nhãn',
 4, 'Tier 4 – Công đoạn Thường', 4,
 JSON_ARRAY('LOW_PRIORITY'),
 JSON_OBJECT('process_classification', 4, 'claim_defect_count', 1, 'total_defects_6m', 1, 'days_since_last_training',
             30, 'fail_rate_process', 0.0, 'standard_time_jt', 15),
 'system'),

-- Snapshot 2 – Policy PROC-DEFECT-FOCUS-2026, Team 5 (Lắp Ráp Động Cơ), Line 4
(2, 5, 'PROC-DC-04', 'Test Nổ Máy & Điều Chỉnh',
 1, 'Focus Tier 1 – CLAIM + Fail Rate', 1,
 JSON_ARRAY('FI_REQUIRED', 'HIGH_CLAIM', 'FAIL_RATE_HIGH'),
 JSON_OBJECT('process_classification', 1, 'claim_defect_count', 5, 'total_defects_6m', 6, 'days_since_last_training',
             88, 'fail_rate_process', 20.0, 'standard_time_jt', 200),
 'system'),
(2, 6, 'PROC-DC-01', 'Lắp Trục Khuỷu & Piston',
 1, 'Focus Tier 1 – CLAIM + Fail Rate', 2,
 JSON_ARRAY('FI_REQUIRED', 'HIGH_CLAIM'),
 JSON_OBJECT('process_classification', 1, 'claim_defect_count', 4, 'total_defects_6m', 5, 'days_since_last_training',
             62, 'fail_rate_process', 16.7, 'standard_time_jt', 180),
 'system'),
(2, 7, 'PROC-DC-02', 'Lắp Nắp Máy & Ron Nắp',
 2, 'Focus Tier 2 – Lỗi + Chưa train', 3,
 JSON_ARRAY('KCS_REQUIRED', 'NEEDS_TRAINING'),
 JSON_OBJECT('process_classification', 1, 'claim_defect_count', 2, 'total_defects_6m', 4, 'days_since_last_training',
             95, 'fail_rate_process', 12.5, 'standard_time_jt', 90),
 'system'),
(2, 8, 'PROC-DC-03', 'Căn Chỉnh & Cân Bằng',
 3, 'Focus Tier 3 – Không ổn định', 4,
 JSON_ARRAY('WATCH'),
 JSON_OBJECT('process_classification', 2, 'claim_defect_count', 1, 'total_defects_6m', 2, 'days_since_last_training',
             55, 'fail_rate_process', 11.0, 'standard_time_jt', 60),
 'system');

-- ============================================================================
-- EMPLOYEE PRIORITY POLICIES
-- Logic: Xếp hạng nhân viên cần được huấn luyện ưu tiên dựa trên
--        thời gian chưa huấn luyện + tỷ lệ trượt + watchlist
-- ============================================================================

-- ── computed_metrics cho EMPLOYEE (bổ sung vào V8 đã có) ──────────────────
-- Các metric này đã seed trong V3, chỉ cần đảm bảo tồn tại:
--   days_since_last_training  (EMPLOYEE)
--   fail_rate                 (EMPLOYEE)
--   years_of_service          (EMPLOYEE)
--   is_on_watchlist           (EMPLOYEE)

-- ── Policy ────────────────────────────────────────────────────────────────

INSERT INTO priority_policies (id, policy_code, policy_name, entity_type,
                               effective_date, expiration_date, status, description, created_by)
VALUES (3, 'EMP-BASIC-2026',
        'Chính sách ưu tiên nhân viên cần huấn luyện 2026',
        'EMPLOYEE',
        '2026-01-01', '2026-12-31',
        'ACTIVE',
        'Xếp hạng nhân viên theo mức độ cần huấn luyện: ưu tiên nhân viên trong watchlist, '
            'tiếp theo là nhân viên chưa học lâu hoặc tỷ lệ trượt cao.',
        'nguyen.quanly');

-- ── Tiers ─────────────────────────────────────────────────────────────────
-- Tier 1: Nhân viên trong watchlist   → xử lý ngay
-- Tier 2: Chưa huấn luyện > 60 ngày  → ưu tiên cao
-- Tier 3: Tỷ lệ trượt >= 30%         → cần ôn lại
-- Tier 4: Còn lại                     → theo thâm niên

INSERT INTO priority_tiers (id, policy_id, tier_order, tier_name, filter_logic,
                            ranking_metric, ranking_direction, secondary_metric, secondary_direction,
                            is_active, created_by)
VALUES (8, 3, 1, 'Tier 1 – Nhân viên trong danh sách theo dõi',
        'AND', 'days_since_last_training', 'DESC', 'fail_rate', 'DESC', TRUE, 'system'),

       (9, 3, 2, 'Tier 2 – Chưa huấn luyện lâu ngày',
        'AND', 'days_since_last_training', 'DESC', 'fail_rate', 'DESC', TRUE, 'system'),

       (10, 3, 3, 'Tier 3 – Tỷ lệ trượt cao',
        'AND', 'fail_rate', 'DESC', 'days_since_last_training', 'DESC', TRUE, 'system'),

       (11, 3, 4, 'Tier 4 – Nhân viên thâm niên thấp (ưu tiên cuối)',
        'AND', 'years_of_service', 'ASC', NULL, NULL, TRUE, 'system');

-- ── Filters ───────────────────────────────────────────────────────────────

INSERT INTO priority_tier_filters
(tier_id, metric_name, operator, filter_value, filter_unit, filter_order, created_by)
VALUES
-- Tier 1: is_on_watchlist = true
(8, 'is_on_watchlist', 'EQ', 'true', 'True/False', 1, 'system'),

-- Tier 2: chưa huấn luyện > 60 ngày VÀ không trong watchlist
(9, 'days_since_last_training', 'GTE', '60', 'Ngày', 1, 'system'),
(9, 'is_on_watchlist', 'EQ', 'false', 'True/False', 2, 'system'),

-- Tier 3: tỷ lệ trượt >= 30%
(10, 'fail_rate', 'GTE', '30', '%', 1, 'system'),

-- Tier 4: không điều kiện đặc biệt, chỉ rank theo thâm niên tăng dần
(11, 'years_of_service', 'GTE', '0', 'Năm', 1, 'system');

-- ============================================================================
-- PART 11: APPROVAL ACTIONS (lịch sử phê duyệt)
-- ============================================================================

INSERT INTO approval_actions (entity_type, entity_id, entity_version, step_order,
                              required_role, action, performed_by_user_id, performed_by_username,
                              performed_by_full_name, performed_by_role, comment, performed_at, created_by)
VALUES
-- Defect Proposal 1: APPROVED (TL submit → SV approve → MG approve)
('DEFECT_REPORT', 1, 1, 0, 'ROLE_TEAM_LEADER', 'SUBMIT', 6, 'tl_tien01', 'Hoàng Văn Trưởng Tổ Tiện', 'ROLE_TEAM_LEADER',
 'Đề xuất báo cáo lỗi Line Tiện T1/2025', '2025-01-20 08:30:00', 'system'),
('DEFECT_REPORT', 1, 1, 1, 'ROLE_SUPERVISOR', 'APPROVE', 3, 'tran.giamsat1', 'Trần Thị Giám Sát', 'ROLE_SUPERVISOR',
 'Xác nhận lỗi đúng thực tế, đề xuất đào tạo ngay', '2025-01-21 10:00:00', 'system'),
('DEFECT_REPORT', 1, 2, 0, 'ROLE_TEAM_LEADER', 'SUBMIT', 6, 'tl_tien01', 'Hoàng Văn Trưởng Tổ Tiện', 'ROLE_TEAM_LEADER',
 'Nộp lại sau khi bổ sung thêm lỗi DF003', '2025-01-25 09:00:00', 'system'),
('DEFECT_REPORT', 1, 2, 1, 'ROLE_SUPERVISOR', 'APPROVE', 3, 'tran.giamsat1', 'Trần Thị Giám Sát', 'ROLE_SUPERVISOR',
 'Duyệt lần 2', '2025-01-26 11:00:00', 'system'),
('DEFECT_REPORT', 1, 2, 2, 'ROLE_MANAGER', 'APPROVE', 2, 'nguyen.quanly', 'Nguyễn Văn Quản Lý', 'ROLE_MANAGER',
 'Phê duyệt toàn bộ báo cáo lỗi Line Tiện', '2025-01-27 14:00:00', 'system'),
-- Defect Proposal 4: REJECTED
('DEFECT_REPORT', 4, 1, 0, 'ROLE_TEAM_LEADER', 'SUBMIT', 9, 'tl_dongco01', 'Bùi Thị Trưởng Tổ ĐC', 'ROLE_TEAM_LEADER',
 'Báo cáo lỗi ĐC tháng 1', '2025-01-28 08:00:00', 'system'),
('DEFECT_REPORT', 4, 1, 1, 'ROLE_SUPERVISOR', 'REJECT', 4, 'le.giamsat2', 'Lê Văn Giám Sát', 'ROLE_SUPERVISOR',
 'Thiếu ảnh minh chứng piston lắp sai. Cần bổ sung.', '2025-01-29 09:30:00', 'system'),
-- Training Plan 1: APPROVED (T1/2026 Tiện)
('TRAINING_PLAN', 1, 1, 0, 'ROLE_TEAM_LEADER', 'SUBMIT', 6, 'tl_tien01', 'Hoàng Văn Trưởng Tổ Tiện', 'ROLE_TEAM_LEADER',
 'Kế hoạch đào tạo T1/2026 Tổ Tiện', '2025-12-28 15:00:00', 'system'),
('TRAINING_PLAN', 1, 1, 1, 'ROLE_SUPERVISOR', 'APPROVE', 3, 'tran.giamsat1', 'Trần Thị Giám Sát', 'ROLE_SUPERVISOR',
 'OK, lịch phù hợp sản xuất', '2025-12-29 10:00:00', 'system'),
('TRAINING_PLAN', 1, 1, 2, 'ROLE_MANAGER', 'APPROVE', 2, 'nguyen.quanly', 'Nguyễn Văn Quản Lý', 'ROLE_MANAGER',
 'Phê duyệt. Thực hiện đúng lịch.', '2025-12-30 14:00:00', 'system'),
-- Training Plan 8: REJECTED
('TRAINING_PLAN', 8, 1, 0, 'ROLE_TEAM_LEADER', 'SUBMIT', 10, 'tl_hanlap01', 'Ngô Văn Trưởng Tổ Hàn', 'ROLE_TEAM_LEADER',
 'KH Tổ Hàn T3/2026', '2026-02-20 09:00:00', 'system'),
('TRAINING_PLAN', 8, 1, 1, 'ROLE_SUPERVISOR', 'REJECT', 4, 'le.giamsat2', 'Lê Văn Giám Sát', 'ROLE_SUPERVISOR',
 'Thiếu NV016 trong lịch. Bổ sung và nộp lại.', '2026-02-22 11:00:00', 'system'),
-- Training Plan 10: WAITING_MANAGER
('TRAINING_PLAN', 10, 1, 0, 'ROLE_TEAM_LEADER', 'SUBMIT', 8, 'tl_laprap01', 'Đặng Văn Trưởng Tổ Lắp',
 'ROLE_TEAM_LEADER', 'KH Bơm T3/2026', '2026-02-25 08:00:00', 'system'),
('TRAINING_PLAN', 10, 1, 1, 'ROLE_SUPERVISOR', 'APPROVE', 5, 'pham.giamsat3', 'Phạm Thị Giám Sát', 'ROLE_SUPERVISOR',
 'Đã kiểm, lịch phù hợp', '2026-02-26 10:00:00', 'system');

-- ============================================================================
-- PART 12: APPROVAL FLOW STEPS
-- ============================================================================

INSERT INTO approval_flow_steps (entity_type, step_order, approver_role, is_active, created_by)
VALUES ('DEFECT_REPORT', 1, 'ROLE_SUPERVISOR', TRUE, 'system'),
       ('DEFECT_REPORT', 2, 'ROLE_MANAGER', TRUE, 'system'),
       ('TRAINING_TOPIC_REPORT', 1, 'ROLE_SUPERVISOR', TRUE, 'system'),
       ('TRAINING_TOPIC_REPORT', 2, 'ROLE_MANAGER', TRUE, 'system'),
       ('TRAINING_PLAN', 1, 'ROLE_SUPERVISOR', TRUE, 'system'),
       ('TRAINING_PLAN', 2, 'ROLE_MANAGER', TRUE, 'system'),
       ('TRAINING_RESULT', 1, 'ROLE_SUPERVISOR', TRUE, 'system'),
       ('TRAINING_RESULT', 2, 'ROLE_MANAGER', TRUE, 'system');

-- ============================================================================
-- PART 13: IMPORT HISTORIES
-- ============================================================================

INSERT INTO import_histories (import_date, user_id, status, file_path, import_type,
                              import_error_description, created_by)
VALUES ('2025-12-01 08:30:00', 6, 'PASS', '/imports/2025/12/defects_line1_dec2025.xlsx', 'DEFECT',
        NULL, 'tl_tien01'),
       ('2025-12-15 09:00:00', 7, 'PASS', '/imports/2025/12/defects_line2_dec2025.xlsx', 'DEFECT',
        NULL, 'tl_phay01'),
       ('2026-01-10 10:15:00', 6, 'FAIL', '/imports/2026/01/training_samples_jan2026.xlsx', 'TRAINING_SAMPLE',
        JSON_OBJECT('total_rows', 50, 'error_rows', 3, 'errors', JSON_ARRAY(
                JSON_OBJECT('row', 12, 'message', 'process_code PH-99 không tồn tại'),
                JSON_OBJECT('row', 27, 'message', 'product_code BOM-XXXXX không hợp lệ'),
                JSON_OBJECT('row', 43, 'message', 'training_code TS9999 trùng với bản ghi hiện tại')
                                                                 )), 'tl_tien01'),
       ('2026-02-05 14:00:00', 8, 'PASS', '/imports/2026/02/manufacturing_lines_feb2026.xlsx', 'MANUFACTURING_LINE',
        NULL, 'tl_laprap01'),
       ('2026-02-20 11:30:00', 9, 'PASS', '/imports/2026/02/employees_eng_dept.xlsx', 'EMPLOYEE',
        NULL, 'tl_dongco01'),
       ('2026-03-01 08:00:00', 7, 'FAIL', '/imports/2026/03/defects_line2_mar2026.xlsx', 'DEFECT',
        JSON_OBJECT('total_rows', 20, 'error_rows', 1, 'errors', JSON_ARRAY(
                JSON_OBJECT('row', 5, 'message', 'Thiếu trường detected_date bắt buộc')
                                                                 )), 'tl_phay01'),
       ('2026-03-10 13:00:00', 1, 'PASS', '/imports/2026/03/training_samples_full_q1.xlsx', 'TRAINING_SAMPLE',
        NULL, 'admin');

-- ============================================================================
-- PART 14: FACTORY CALENDAR 2026
-- ============================================================================

INSERT INTO factory_calendars (calendar_year, calendar_name, source_system, source_endpoint,
                               source_version, synced_at, synced_by, start_date, end_date, is_active, created_by)
VALUES (2026, 'DMVN Working Calendar 2026', 'EXT_HRM_CALENDAR',
        '/api/v1/calendars/factory/2026', 'v1',
        '2026-01-02 07:00:00', 'system', '2026-01-01', '2026-12-31', TRUE, 'system');

SET @cal := LAST_INSERT_ID();

INSERT INTO factory_calendar_entries (calendar_id, work_date, day_type, holiday_name, note, created_by)
VALUES
-- January 2026
(@cal, '2026-01-01', 'HOLIDAY', 'Tết Dương Lịch', NULL, 'system'),
(@cal, '2026-01-02', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-05', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-06', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-07', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-08', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-09', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-10', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-01-11', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-01-12', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-13', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-14', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-15', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-16', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-19', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-20', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-21', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-22', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-23', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-24', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-01-25', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-01-26', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-27', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-28', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-01-29', 'SPECIAL_EVENT', NULL, 'Họp tổng kết năm 2025', 'system'),
(@cal, '2026-01-30', 'HOLIDAY', 'Trước Tết Nguyên Đán', NULL, 'system'),
(@cal, '2026-01-31', 'HOLIDAY', 'Tết Nguyên Đán (Giao Thừa)', NULL, 'system'),
-- February 2026
(@cal, '2026-02-01', 'HOLIDAY', 'Tết Nguyên Đán (Mùng 1)', NULL, 'system'),
(@cal, '2026-02-02', 'HOLIDAY', 'Tết Nguyên Đán (Mùng 2)', NULL, 'system'),
(@cal, '2026-02-03', 'HOLIDAY', 'Tết Nguyên Đán (Mùng 3)', NULL, 'system'),
(@cal, '2026-02-04', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-05', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-06', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-07', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-02-08', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-02-09', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-10', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-11', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-12', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-13', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-14', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-02-16', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-17', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-18', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-19', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-20', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-23', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-24', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-25', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-26', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-02-27', 'WORKING_DAY', NULL, NULL, 'system'),
-- March 2026
(@cal, '2026-03-02', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-03', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-04', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-05', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-06', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-07', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-08', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-09', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-10', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-11', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-12', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-13', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-14', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-15', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-16', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-17', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-18', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-19', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-20', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-21', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-22', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-23', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-24', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-25', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-26', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-27', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-28', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-29', 'WEEKEND', NULL, NULL, 'system'),
(@cal, '2026-03-30', 'WORKING_DAY', NULL, NULL, 'system'),
(@cal, '2026-03-31', 'SPECIAL_EVENT', NULL, 'Họp review Q1/2026', 'system');

-- ============================================================================
-- PART 15: TRAINING SAMPLE REVIEW CONFIG & POLICIES
-- ============================================================================

INSERT INTO training_sample_review_policies (policy_code, effective_date, expiration_date,
                                             status, description, created_by, updated_by)
VALUES ('TSRP-2026-001', '2026-01-01', '2026-12-31', 'ACTIVE',
        'Chính sách rà soát mẫu huấn luyện định kỳ hàng năm cho tất cả dây chuyền năm 2026', 'admin', 'admin'),
       ('TSRP-2025-001', '2025-01-01', '2025-12-31', 'DEACTIVE',
        'Chính sách rà soát mẫu huấn luyện định kỳ hàng năm năm 2025', 'admin', 'admin');

INSERT INTO training_sample_review_configs (product_line_id, trigger_month, trigger_day, due_days,
                                            review_policy_id, created_by)
VALUES (1, 3, 1, 30, 1, 'admin'),
       (2, 3, 1, 30, 1, 'admin'),
       (3, 6, 1, 30, 1, 'admin'),
       (4, 9, 1, 30, 1, 'admin'),
       (5, 12, 1, 30, 1, 'admin');

INSERT INTO notification_templates (code, subject_template, html_template_name, description, created_by)
VALUES ('APPROVAL_NUDGE',
        '[Nhắc ký] {entityTypeLabel}: {documentTitle}',
        'approval-nudge-request',
        'TL chủ động nhắc SV/Manager ký duyệt một phiếu đang chờ xử lý',
        'system'),

       ('APPROVAL_OVERDUE',
        '[Cần xử lý] Bạn có {pendingCount} phiếu chờ phê duyệt quá {overdueHours} giờ',
        'approval-overdue',
        'Tự động nhắc SV/Manager khi có phiếu chờ duyệt vượt quá thời hạn SLA',
        'system'),

       ('PLAN_APPROVAL_REQUEST',
        '[Cần phê duyệt] Kế hoạch huấn luyện: {planTitle}',
        'plan-approval-request',
        'Gửi cho SV/Manager ngay khi TL nộp kế hoạch huấn luyện mới',
        'system'),

       ('PLAN_REJECTED',
        '[Bị trả lại] Kế hoạch huấn luyện cần chỉnh sửa: {planTitle}',
        'plan-rejected',
        'Gửi cho TL khi kế hoạch huấn luyện bị SV hoặc Manager từ chối',
        'system'),

       ('TRAINING_REMINDER_TODAY',
        '[Nhắc lịch] Hôm nay bạn có {trainingCount} lịch kiểm tra huấn luyện',
        'training-reminder-today',
        'Gửi tự động mỗi sáng cho TL khi có lịch huấn luyện trong ngày hôm đó',
        'system'),

       ('TRAINING_OVERDUE_WARNING',
        '[Cảnh báo] Bạn có {overdueCount} lịch huấn luyện quá hạn chưa ghi kết quả',
        'training-overdue-warning',
        'Gửi tự động cho TL khi có lịch huấn luyện đã qua ngày mà chưa cập nhật kết quả',
        'system');

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- END OF V8 FAKE DATA
-- Tổng kết:
--   Users:           13 (admin, 1 mgr, 3 sv, 6 TL, 2 FI)
--   Roles:            5
--   Sections:         3  |  Groups: 6  |  Teams: 6
--   Employees:       30  (ACTIVE/MATERNITY_LEAVE/RESIGNED)
--   Product Lines:    5  |  Products: 20  |  Processes: 21
--   Defects:         40  (5 lines, đủ loại CLAIM/DEFECTIVE_GOODS/STARTLED_CLAIM)
--   Defect Proposals: 8  (đủ trạng thái)
--   Training Samples:30
--   Training Plans:  15  (đủ trạng thái)
--   Plan Details:    60
--   Training Results: 9  |  Result Details: 38
--   Priority Policies:2  (PROCESS entity, ACTIVE)
--   Priority Tiers:   7  (Policy 1: 4 tiers, Policy 2: 3 tiers)
--   Tier Filters:    14
--   Snapshots:        2  |  Snapshot Details: 8
--   Approval Actions:14
--   Import Histories: 7  (PASS/FAIL với error detail JSON)
--   Calendar Entries:80+ ngày cho Jan-Mar 2026
-- ============================================================================