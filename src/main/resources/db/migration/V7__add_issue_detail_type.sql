ALTER TABLE issue_detail
    ADD COLUMN issue_detail_type ENUM('CREATE','UPDATE','DELETE'),
    ADD COLUMN target_defect_id BIGINT NULL,
    DROP COLUMN is_escaped;

-- Ensure all existing rows are CREATE (explicit backfill)
UPDATE issue_detail
SET issue_detail_type = 'CREATE'
WHERE issue_detail_type IS NULL OR issue_detail_type <> 'CREATE';

ALTER TABLE issue_detail_history
    ADD COLUMN issue_detail_type ENUM('CREATE','UPDATE','DELETE'),
    ADD COLUMN target_defect_id BIGINT NULL,
    DROP COLUMN is_escaped;

-- Ensure all existing rows are CREATE (explicit backfill)
UPDATE issue_detail
SET issue_detail_type = 'CREATE'
WHERE issue_detail_type IS NULL OR issue_detail_type <> 'CREATE';