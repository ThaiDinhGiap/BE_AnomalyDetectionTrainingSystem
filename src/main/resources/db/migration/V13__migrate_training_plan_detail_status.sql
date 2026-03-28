-- First alter the table to accept the new string values
ALTER TABLE training_plan_details MODIFY COLUMN status VARCHAR(30) DEFAULT 'PENDING_REVIEW';

-- Then migrate the existing records
UPDATE training_plan_details
SET status = 'PENDING_REVIEW'
WHERE status = 'PENDING';

UPDATE training_plan_details
SET status = 'COMPLETED'
WHERE status = 'DONE';

UPDATE training_plan_details
SET status = 'MISSED'
WHERE status = 'MISS';
