-- ── 4 permission mới ────────────────────────────────────────────────────────
INSERT INTO permissions (id, permission_code, display_name, module_id, action, sort_order, is_system, created_by)
VALUES (45, 'employee.view_all', 'Xem toàn bộ nhân viên hệ thống', 5, 'view', 3, TRUE, 'system'),
       (46, 'line_structure.configure', 'Cấu hình cấu trúc dây chuyền', 12, 'configure', 3, TRUE, 'system'),
       (47, 'product.catalog', 'Quản lý danh mục sản phẩm theo dây chuyền', 13, 'catalog', 3, TRUE, 'system'),
       (48, 'scoring.policy_view', 'Xem cấu hình chính sách ưu tiên', 9, 'view', 3, TRUE, 'system'),
       (49, 'review_approve.confirm', 'Xác nhận kết quả', 15, 'confirm', 2, TRUE, 'system');

-- ── Gán cho ADMIN (role_id=1) ───────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
VALUES (1, 45),
       (1, 46),
       (1, 47),
       (1, 48);

-- ── FINAL_INSPECTION (role_id=5) ─────────────────────────────────────────────
INSERT INTO role_permissions (role_id, permission_id)
VALUES
    -- Training Result
    (5, 49);
