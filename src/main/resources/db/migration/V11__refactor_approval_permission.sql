-- ============================================================================
-- V11: Refactor Approval System from Role-based to Permission-based
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. Add new columns to approval_flow_steps
-- ============================================================================
ALTER TABLE approval_flow_steps
    ADD COLUMN required_permission VARCHAR(100) NULL AFTER step_order,
    ADD COLUMN step_label          VARCHAR(100) NULL AFTER required_permission,
    ADD COLUMN pending_status      VARCHAR(50)  NULL AFTER step_label;

-- Migrate data: map old approver_role → new required_permission, step_label, pending_status
UPDATE approval_flow_steps
SET required_permission = 'APPROVAL_REVIEW',
    step_label          = 'NGƯỜI KIỂM TRA',
    pending_status      = 'PENDING_REVIEW'
WHERE approver_role = 'ROLE_SUPERVISOR';
UPDATE approval_flow_steps
SET required_permission = 'APPROVAL_APPROVE',
    step_label          = 'NGƯỜI PHÊ DUYỆT',
    pending_status      = 'PENDING_APPROVAL'
WHERE approver_role = 'ROLE_MANAGER';

-- Make new columns NOT NULL
ALTER TABLE approval_flow_steps
    MODIFY COLUMN required_permission VARCHAR(100) NOT NULL,
    MODIFY COLUMN pending_status VARCHAR(50) NOT NULL;

-- Drop old column
ALTER TABLE approval_flow_steps
    DROP COLUMN approver_role;

-- ============================================================================
-- 2. Alter approval_actions table
-- ============================================================================
-- Change required_role from ENUM → VARCHAR
ALTER TABLE approval_actions
    ADD COLUMN required_permission VARCHAR(100) NULL AFTER step_order;

UPDATE approval_actions
SET required_permission = 'APPROVAL_REVIEW'
WHERE required_role IN ('ROLE_SUPERVISOR');
UPDATE approval_actions
SET required_permission = 'APPROVAL_APPROVE'
WHERE required_role IN ('ROLE_MANAGER');
UPDATE approval_actions
SET required_permission = required_role
WHERE required_permission IS NULL;

ALTER TABLE approval_actions
    MODIFY COLUMN required_permission VARCHAR(100) NOT NULL;
ALTER TABLE approval_actions
    DROP COLUMN required_role;

-- Change performed_by_role from ENUM → VARCHAR
ALTER TABLE approval_actions
    MODIFY COLUMN performed_by_role VARCHAR(100) NOT NULL;

-- ============================================================================
-- 3. Migrate ReportStatus values in entity tables
-- ============================================================================
-- training_plans
ALTER TABLE training_plans
    MODIFY COLUMN status VARCHAR(30) DEFAULT 'DRAFT';
UPDATE training_plans
SET status = 'PENDING_REVIEW'
WHERE status = 'WAITING_SV';
UPDATE training_plans
SET status = 'PENDING_APPROVAL'
WHERE status = 'WAITING_MANAGER';
UPDATE training_plans
SET status = 'REJECTED'
WHERE status IN ('REJECTED_BY_SV', 'REJECTED_BY_MANAGER');

-- training_results
ALTER TABLE training_results
    MODIFY COLUMN status VARCHAR(30) DEFAULT 'ON_GOING';
UPDATE training_results
SET status = 'PENDING_REVIEW'
WHERE status = 'WAITING_SV';
UPDATE training_results
SET status = 'PENDING_APPROVAL'
WHERE status = 'WAITING_MANAGER';
UPDATE training_results
SET status = 'REJECTED'
WHERE status IN ('REJECTED_BY_SV', 'REJECTED_BY_MANAGER');

-- training_result_details
ALTER TABLE training_result_details
    MODIFY COLUMN status VARCHAR(30) DEFAULT 'PENDING';
UPDATE training_result_details
SET status = 'PENDING_REVIEW'
WHERE status = 'WAITING_SV';
UPDATE training_result_details
SET status = 'REJECTED'
WHERE status IN ('REJECTED_BY_SV', 'REJECTED_BY_MANAGER');

-- training_sample_reviews
ALTER TABLE training_sample_reviews
    MODIFY COLUMN status VARCHAR(30) DEFAULT 'NEED_ASSIGNED';
UPDATE training_sample_reviews
SET status = 'PENDING_REVIEW'
WHERE status = 'WAITING_SV';
UPDATE training_sample_reviews
SET status = 'REJECTED'
WHERE status IN ('REJECTED_BY_SV', 'REJECTED_BY_MANAGER');

-- defect_proposals (nếu có cột status dạng ENUM)
-- (Các entity dạng Approvable khác cũng cần migrate status tương tự)

INSERT INTO modules (id, module_code, display_name, description, sort_order, created_by)
VALUES (14, 'review_approve', 'Kiểm tra và Phê duyệt', 'Kiểm tra và phê duyệt các đề xuất', 1, 'system');

-- ============================================================================
-- 4. Insert new permissions
-- ============================================================================
INSERT INTO permissions (id, permission_code, display_name, module_id, action, sort_order, is_system, created_by)
VALUES (36, 'approval.review', 'Kiểm duyệt báo cáo', 14, 'review', 1, TRUE, 'system'),
       (37, 'approval.approve', 'Phê duyệt báo cáo', 14, 'approve', 2, TRUE, 'system');

-- ============================================================================
-- 5. Assign permissions to existing roles
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

-- ============================================================================
-- 6. Update notification_templates codes (if they exist)
-- ============================================================================
-- Note: If notification_templates use the old NotificationType enum values as codes,
-- they need to be updated. This is safe because we use ON DUPLICATE KEY.
-- Skip this if you manage templates outside of DB migration.

SET FOREIGN_KEY_CHECKS = 1;
