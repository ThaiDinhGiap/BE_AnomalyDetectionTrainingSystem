-- ============================================================================
-- V4: SEED RBAC DATA - Modules, Permissions, System Roles & Assignments
-- ============================================================================

-- ----------------------------------------------------------------------------
-- PART 1: MODULES
-- ----------------------------------------------------------------------------
INSERT INTO modules (id, module_code, display_name, description, sort_order, created_by)
VALUES (1,  'defect_report',                'Báo cáo lỗi sản phẩm',       'Quản lý báo cáo lỗi',                 1,  'system'),
       (2,  'training_sample',              'Chủ đề đào tạo',             'Quản lý chủ đề đào tạo',              2,  'system'),
       (3,  'training_plan',                'Kế hoạch đào tạo',           'Quản lý kế hoạch đào tạo',            3,  'system'),
       (4,  'training_result',              'Kết quả đào tạo',            'Quản lý kết quả đào tạo',             4,  'system'),
       (5,  'employee',                     'Nhân viên',                  'Quản lý thông tin nhân viên',         5,  'system'),
       (6,  'user',                         'Tài khoản người dùng',       'Quản lý tài khoản hệ thống',          6,  'system'),
       (7,  'role',                         'Vai trò & Phân quyền',       'Quản lý vai trò và phân quyền',       7,  'system'),
       (8,  'master_data',                  'Dữ liệu danh mục',           'Quản lý dữ liệu danh mục',            8,  'system'),
       (9,  'scoring',                      'Chấm điểm ưu tiên',          'Quản lý chính sách chấm điểm',        9,  'system'),
       (10, 'dashboard',                    'Dashboard & Báo cáo',        'Xem dashboard và báo cáo tổng hợp',   10, 'system'),
       (11, 'system',                       'Cài đặt hệ thống',           'Cài đặt và cấu hình hệ thống',        11, 'system'),
       (12, 'staff_organization',           'Tổ chức nhân sự',            'Quản lí danh sách nhân sự công ty',   12, 'system'),
       (13, 'manufacturing_line',           'Tổ chức sản xuất',           'Quản lí danh sách tổ chức dây chuyền',13, 'system');

-- ----------------------------------------------------------------------------
-- PART 2: PERMISSIONS
-- ----------------------------------------------------------------------------
INSERT INTO permissions (id, permission_code, display_name, module_id, action, sort_order, is_system, created_by)
VALUES
-- defect_report (module 1)
(1,  'defect_proposal.view',    'Xem báo cáo lỗi',          1, 'view',   1, TRUE, 'system'),
(2,  'defect_proposal.create',  'Tạo báo cáo lỗi',          1, 'create', 2, TRUE, 'system'),
(3,  'defect_proposal.edit',    'Sửa báo cáo lỗi',          1, 'edit',   3, TRUE, 'system'),
(4,  'defect_proposal.delete',  'Xoá báo cáo lỗi',          1, 'delete', 4, TRUE, 'system'),
(5,  'defect_proposal.approve', 'Phê duyệt báo cáo lỗi',    1, 'approve',5, TRUE, 'system'),
(111,'defect.view',             'Xem danh sách lỗi quá khứ',1, 'view',  6, TRUE, 'system'),

-- training_sample (module 2)
(6,  'training_sample_proposal.view',   'Xem chủ đề đào tạo',           2, 'view',   1, TRUE, 'system'),
(7,  'training_sample_proposal.create', 'Tạo chủ đề đào tạo',           2, 'create', 2, TRUE, 'system'),
(8,  'training_sample_proposal.edit',   'Sửa chủ đề đào tạo',           2, 'edit',   3, TRUE, 'system'),
(9,  'training_sample_proposal.delete', 'Xoá chủ đề đào tạo',           2, 'delete', 4, TRUE, 'system'),
(100,'training_sample.view',           'Xem danh sách mẫu huấn luyện',  2, 'view',   5, TRUE, 'system'),

-- training_plan (module 3)
(10, 'training_plan.view',    'Xem kế hoạch đào tạo',    3, 'view',   1, TRUE, 'system'),
(11, 'training_plan.create',  'Tạo kế hoạch đào tạo',    3, 'create', 2, TRUE, 'system'),
(12, 'training_plan.edit',    'Sửa kế hoạch đào tạo',    3, 'edit',   3, TRUE, 'system'),
(13, 'training_plan.delete',  'Xoá kế hoạch đào tạo',    3, 'delete', 4, TRUE, 'system'),
(14, 'training_plan.approve', 'Phê duyệt kế hoạch đào tạo', 3, 'approve', 5, TRUE, 'system'),

-- training_result (module 4)
(15, 'training_result.view',  'Xem kết quả đào tạo',     4, 'view',   1, TRUE, 'system'),
(16, 'training_result.edit',  'Cập nhật kết quả đào tạo',4, 'edit',   2, TRUE, 'system'),
(17, 'training_result.approve','Phê duyệt kết quả đào tạo',4,'approve',3, TRUE, 'system'),

-- employee (module 5)
(18, 'employee.view',         'Xem thông tin nhân viên',  5, 'view',   1, TRUE, 'system'),
(19, 'employee.create',       'Thêm nhân viên',           5, 'create', 2, TRUE, 'system'),
(20, 'employee.edit',         'Sửa thông tin nhân viên',  5, 'edit',   3, TRUE, 'system'),
(21, 'employee.delete',       'Xoá nhân viên',            5, 'delete', 4, TRUE, 'system'),

-- user (module 6)
(22, 'user.view',             'Xem tài khoản người dùng', 6, 'view',   1, TRUE, 'system'),
(23, 'user.create',           'Tạo tài khoản người dùng', 6, 'create', 2, TRUE, 'system'),
(24, 'user.edit',             'Sửa tài khoản người dùng', 6, 'edit',   3, TRUE, 'system'),
(25, 'user.delete',           'Xoá tài khoản người dùng', 6, 'delete', 4, TRUE, 'system'),
(26, 'user.assign_role',      'Gán vai trò cho người dùng',6,'assign_role',5,TRUE,'system'),

-- role (module 7)
(27, 'role.view',             'Xem vai trò',              7, 'view',   1, TRUE, 'system'),
(28, 'role.create',           'Tạo vai trò',              7, 'create', 2, TRUE, 'system'),
(29, 'role.edit',             'Sửa vai trò',              7, 'edit',   3, TRUE, 'system'),
(30, 'role.delete',           'Xoá vai trò',              7, 'delete', 4, TRUE, 'system'),
(31, 'role.assign_permission','Gán quyền cho vai trò',    7, 'assign_permission', 5, TRUE, 'system'),

-- master_data (module 8)
(32, 'master_data.view',      'Xem dữ liệu danh mục',    8, 'view',   1, TRUE, 'system'),
(33, 'master_data.create',    'Thêm dữ liệu danh mục',   8, 'create', 2, TRUE, 'system'),
(34, 'master_data.edit',      'Sửa dữ liệu danh mục',    8, 'edit',   3, TRUE, 'system'),
(35, 'master_data.delete',    'Xoá dữ liệu danh mục',    8, 'delete', 4, TRUE, 'system'),

-- scoring (module 9)
(36, 'scoring.view',          'Xem chính sách chấm điểm',9, 'view',   1, TRUE, 'system'),
(37, 'scoring.create',        'Tạo chính sách chấm điểm',9, 'create', 2, TRUE, 'system'),
(38, 'scoring.edit',          'Sửa chính sách chấm điểm',9, 'edit',   3, TRUE, 'system'),
(39, 'scoring.delete',        'Xoá chính sách chấm điểm',9, 'delete', 4, TRUE, 'system'),

-- dashboard (module 10)
(40, 'dashboard.view',        'Xem dashboard',           10, 'view',  1, TRUE, 'system'),
(41, 'dashboard.export',      'Xuất báo cáo',            10, 'export',2, TRUE, 'system'),

-- system (module 11)
(42, 'system.config',         'Cấu hình hệ thống',       11, 'config',1, TRUE, 'system'),

-- staff organization (module 12)
(43, 'staff_organization.view',          'Xem danh sách cấu trúc nhân sự',12, 'view',   1, TRUE, 'system'),
(44, 'staff_organization.create',        'Tạo danh sách cấu trúc nhân sự',12, 'create', 2, TRUE, 'system'),
(45, 'staff_organization.edit',          'Sửa danh sách cấu trúc nhân sự',12, 'edit',   3, TRUE, 'system'),
(46, 'staff_organization.delete',        'Xoá danh sách cấu trúc nhân sự',12, 'delete', 4, TRUE, 'system'),

-- manufacturing line (module 13)
(47, 'manufacturing_line.view',          'Xem danh sách cấu trúc dây chuyền',13, 'view',   1, TRUE, 'system'),
(48, 'manufacturing_line.create',        'Tạo danh sách cấu trúc dây chuyền',13, 'create', 2, TRUE, 'system'),
(49, 'manufacturing_line.edit',          'Sửa danh sách cấu trúc dây chuyền',13, 'edit',   3, TRUE, 'system'),
(50, 'manufacturing_line.delete',        'Xoá danh sách cấu trúc dây chuyền',13, 'delete', 4, TRUE, 'system');
-- ----------------------------------------------------------------------------
-- PART 3: ADD MISSING SYSTEM ROLE (FINAL_INSPECTION)
-- V2 already seeded roles 1-4; add role 5 here
-- ----------------------------------------------------------------------------
INSERT INTO roles (id, role_code, display_name, description, is_system, is_active, created_by)
VALUES (5, 'ROLE_FINAL_INSPECTION', 'Kiểm tra cuối chuyền', 'Nhập kết quả kiểm tra cuối', TRUE, TRUE, 'system');

-- ----------------------------------------------------------------------------
-- PART 4: ASSIGN PERMISSIONS TO ROLES
-- ADMIN gets all permissions
-- ----------------------------------------------------------------------------
INSERT INTO role_permissions (role_id, permission_id)
SELECT 1, id FROM permissions;

-- MANAGER: view/approve everything, manage employees
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(2, 1),(2, 2),(2, 3),(2, 5),    -- defect_proposal: view,create,edit,approve
(2, 6),(2, 7),(2, 8),           -- training_sample: view,create,edit
(2, 10),(2, 11),(2, 12),(2, 14),-- training_plan: view,create,edit,approve
(2, 15),(2, 16),(2, 17),        -- training_result: view,edit,approve
(2, 18),(2, 19),(2, 20),        -- employee: view,create,edit
(2, 36),(2, 37),(2, 38),        -- scoring: view,create,edit
(2, 40),(2, 41),                -- dashboard: view,export
(2, 43),
(2, 44),
(2, 45),
(2, 46),(2, 22),(2, 27);                -- user.view, role.view

-- SUPERVISOR: view/create/edit reports, view plans, manage training results
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(3, 1),(3, 2),(3, 3),(3, 5),    -- defect_proposal: view,create,edit,approve
(3, 6),(3, 7),(3, 8),           -- training_sample: view,create,edit
(3, 10),(3, 11),(3, 12),(3, 14),-- training_plan: view,create,edit,approve
(3, 15),(3, 16),                -- training_result: view,edit
(3, 18),(3, 19),(3, 20),        -- employee: view,create,edit
(3, 36),                        -- scoring: view
(3, 40);                        -- dashboard: view

-- TEAM_LEADER: create and view reports, input training results
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    (4, 1),   -- defect_proposal.view
    (4, 2),   -- defect_proposal.create
    (4, 3),   -- defect_proposal.edit
    (4, 4),   -- defect_proposal.delete
    (4, 5),   -- defect_proposal.approve
    (4, 111),-- defect.view (Xem danh sách lỗi quá khứ)
    (4, 6),   -- training_sample_proposal.view
    (4, 7),   -- training_sample_proposal.create
    (4, 8),   -- training_sample_proposal.edit
    (4, 9),   -- training_sample_proposal.delete
    (4, 10), -- training_sample.view (Xem danh sách mẫu huấn luyện)
    (4, 11),
    (4, 12),
    (4, 13),
    (4, 14),
    (4, 15),
    (4, 16),
    (4, 17);
-- FINAL_INSPECTION: view defects, input final check results
INSERT INTO role_permissions (role_id, permission_id)
VALUES
(5, 1),(5, 2),(5, 3),           -- defect_proposal: view,create,edit
(5, 15),(5, 16),                -- training_result: view,edit
(5, 18),                        -- employee: view
(5, 40);                        -- dashboard: view

-- ----------------------------------------------------------------------------
-- PART 5: ASSIGN FINAL_INSPECTION ROLE TO USERS 8 & 9
-- (V2 already assigned roles to users 1-7)
-- ----------------------------------------------------------------------------
INSERT INTO user_roles (user_id, role_id)
VALUES (8, 5),
       (9, 5);
