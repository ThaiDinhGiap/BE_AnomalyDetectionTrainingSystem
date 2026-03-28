ALTER TABLE employees
    ADD COLUMN join_date DATE DEFAULT NULL COMMENT 'Ngày vào công ty' AFTER status;

-- Update years_of_service metric to use SQL instead of PROPERTY (since join_date may be null)
UPDATE computed_metrics
SET compute_method     = 'SQL',
    compute_definition = 'SELECT TIMESTAMPDIFF(YEAR, e.join_date, CURDATE()) FROM employees e WHERE e.id = :entityId AND e.join_date IS NOT NULL AND e.delete_flag = FALSE',
    updated_by         = 'system'
WHERE metric_name = 'years_of_service'
  AND delete_flag = FALSE;
