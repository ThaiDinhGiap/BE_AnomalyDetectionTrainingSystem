INSERT INTO permissions (permission_code, display_name, module_id, action, sort_order, is_system, created_by)
VALUES ('training_plan.view.own_group',
        'Xem kế hoạch đào tạo theo group phụ trách',
        3, 'view', 21, TRUE, 'system'),

       ('training_plan.view.section',
        'Xem kế hoạch đào tạo toàn section',
        3, 'view', 22, TRUE, 'system'),

       ('training_plan.view.cross_group',
        'Xem kế hoạch đào tạo nhiều group (Final Inspection)',
        3, 'view', 23, TRUE, 'system');


INSERT INTO role_permissions (role_id, permission_id)
SELECT 2, id
FROM permissions
WHERE permission_code = 'training_plan.view.section';

INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id
FROM permissions
WHERE permission_code = 'training_plan.view.own_group';

INSERT INTO role_permissions (role_id, permission_id)
SELECT 5, id
FROM permissions
WHERE permission_code = 'training_plan.view.cross_group';