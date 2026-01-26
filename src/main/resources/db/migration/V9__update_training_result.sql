
ALTER TABLE training_plan_detail RENAME COLUMN result_status TO status;

ALTER TABLE training_plan_detail
    MODIFY COLUMN status ENUM('PENDING', 'DONE', 'MISSED') DEFAULT 'PENDING';

ALTER TABLE training_plan
DROP COLUMN title;

ALTER TABLE training_plan
    ADD COLUMN form_code VARCHAR(255) DEFAULT 'TR_PLAN';

ALTER TABLE training_plan
    ADD COLUMN note VARCHAR(255) ;

ALTER TABLE training_result
DROP COLUMN title;

ALTER TABLE training_result
    ADD COLUMN form_code VARCHAR(255) DEFAULT 'TR_RESULT';

ALTER TABLE training_result
    ADD COLUMN note VARCHAR(255) ;

ALTER TABLE training_result
    MODIFY COLUMN status ENUM('ON_GOING', 'DONE', 'WAITING_MANAGER', 'REJECTED_BY_MANAGER', 'APPROVED_BY_MANAGER') DEFAULT 'ON_GOING';

ALTER TABLE training_result_detail
    ADD COLUMN planned_date DATE;

ALTER TABLE training_result_detail
    MODIFY COLUMN actual_date DATE NULL;

ALTER TABLE training_result_detail
    MODIFY COLUMN time_in TIME  NULL;

ALTER TABLE training_result_detail
    MODIFY COLUMN time_out TIME  NULL;

ALTER TABLE training_result_detail
    ADD COLUMN training_sample VARCHAR(255);

ALTER TABLE training_result_detail DROP FOREIGN KEY training_result_detail_ibfk_4;


-- 1. Thêm cột mới
ALTER TABLE training_result_detail
    ADD COLUMN defect_training_content_id BIGINT;

-- 2. Thêm ràng buộc khóa ngoại
ALTER TABLE training_result_detail
    ADD CONSTRAINT fkk_training_result_detail
        FOREIGN KEY (defect_training_content_id) REFERENCES defect_training_content(id);