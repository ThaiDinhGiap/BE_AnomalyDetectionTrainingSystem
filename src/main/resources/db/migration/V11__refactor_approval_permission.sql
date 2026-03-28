-- ============================================================================
-- V11: Add Approval Permissions
-- ============================================================================
-- Note: approval_flow_steps and approval_actions schema already uses
-- permission-based columns (required_permission, pending_status) from V1.
-- This migration only adds the new permission records and role assignments.

-- ============================================================================
-- 1. Add module for review/approve
-- ============================================================================
INSERT INTO modules (id, module_code, display_name, description, sort_order, created_by)
VALUES (14, 'review_approve', 'Kiểm tra và Phê duyệt', 'Kiểm tra và phê duyệt các đề xuất', 1, 'system');

-- ============================================================================
-- 2. Insert new permissions
-- ============================================================================
INSERT INTO permissions (id, permission_code, display_name, module_id, action, sort_order, is_system, created_by)
VALUES (39, 'APPROVAL_REVIEW', 'Kiểm duyệt báo cáo', 14, 'review', 1, TRUE, 'system'),
       (40, 'APPROVAL_APPROVE', 'Phê duyệt báo cáo', 14, 'approve', 2, TRUE, 'system');

-- ============================================================================
-- 3. Assign permissions to existing roles
-- ============================================================================
-- ROLE_SUPERVISOR gets APPROVAL_REVIEW
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r,
     permissions p
WHERE r.role_code = 'ROLE_SUPERVISOR'
  AND p.permission_code = 'APPROVAL_REVIEW'
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ROLE_MANAGER gets APPROVAL_APPROVE
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r,
     permissions p
WHERE r.role_code = 'ROLE_MANAGER'
  AND p.permission_code = 'APPROVAL_APPROVE'
ON DUPLICATE KEY UPDATE role_id = role_id;
