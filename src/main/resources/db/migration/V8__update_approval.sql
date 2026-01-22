/* ==========================================================================
   1. CẬP NHẬT BẢNG ISSUE_REPORT
   - Xóa cột cũ (approval cũ)
   - Thêm cột mới (versioning, reject info)
   ========================================================================== */

-- Tắt check khóa ngoại để xóa cột FK dễ dàng
ALTER TABLE issue_report DROP FOREIGN KEY issue_report_ibfk_1;
ALTER TABLE issue_report DROP FOREIGN KEY issue_report_ibfk_2;
ALTER TABLE issue_report DROP FOREIGN KEY issue_report_ibfk_3;

ALTER TABLE issue_report
    DROP COLUMN verified_by_sv,
    DROP COLUMN verified_at_sv,
    DROP COLUMN approved_by_manager,
    DROP COLUMN approved_at_manager,
    DROP COLUMN rejected_by;
--



/* ==========================================================================
   2. TẠO BẢNG MỚI: ISSUE_REPORT_APPROVAL
   ========================================================================== */
DROP TABLE IF EXISTS issue_report_approval;
CREATE TABLE issue_report_approval (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Fields
                                       issue_report_id BIGINT NOT NULL,
                                       processed_by_user_id BIGINT NOT NULL,
                                       processed_role VARCHAR(255),
                                       action VARCHAR(50) NOT NULL,
                                       resulting_status VARCHAR(50),
                                       comment TEXT,
                                       plan_version INT,

                                       delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       created_by NVARCHAR(255),
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                       updated_by NVARCHAR(255),


    -- Constraints
                                       CONSTRAINT fk_ira_issue_report FOREIGN KEY (issue_report_id) REFERENCES issue_report(id),
                                       CONSTRAINT fk_ira_processed_by FOREIGN KEY (processed_by_user_id) REFERENCES users(id)
);

-- Ví dụ: Bảng issue_report, thêm status 'WAITING_APPROVAL'
-- LƯU Ý: Bạn phải liệt kê lại TẤT CẢ status cũ đang có, sau đó thêm status mới vào cuối.

ALTER TABLE training_result
    MODIFY COLUMN status ENUM('DRAFT', 'ON_GOING', 'DONE', 'WAITING_MANAGER', 'REJECTED_BY_MANAGER', 'APPROVED_BY_MANAGER') DEFAULT 'ON_GOING';
-- Ví dụ: Bảng issue_report, thêm status 'WAITING_APPROVAL'
-- LƯU Ý: Bạn phải liệt kê lại TẤT CẢ status cũ đang có, sau đó thêm status mới vào cuối.

UPDATE training_plan SET status = 'DRAFT';
ALTER TABLE training_plan
    MODIFY COLUMN status ENUM('DRAFT', 'WAITING_SV', 'REJECTED_BY_SV', 'WAITING_MANAGER', 'REJECTED_BY_MANAGER', 'APPROVED_BY_SV','APPROVED_BY_MANAGER','NEED_UPDATE') DEFAULT 'DRAFT';
/* ==========================================================================
   3. CẬP NHẬT BẢNG TRAINING_TOPICS
   - Xóa cột cũ
   - Thêm cột mới
   ========================================================================== */

ALTER TABLE training_topics DROP FOREIGN KEY training_topics_ibfk_1;
ALTER TABLE training_topics DROP FOREIGN KEY training_topics_ibfk_2;

ALTER TABLE training_topics
-- Xóa các cột cũ
    DROP COLUMN verified_by_sv,
    DROP COLUMN verified_at_sv,
    DROP COLUMN approved_by_manager,
    DROP COLUMN approved_at_manager,
    DROP COLUMN version;



/* ==========================================================================
   4. TẠO BẢNG MỚI: TRAINING_TOPIC_APPROVAL
   ========================================================================== */
DROP TABLE IF EXISTS training_topic_approval;

CREATE TABLE training_topic_approval (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Fields
                                         training_topic_id BIGINT NOT NULL,
                                         processed_by_user_id BIGINT NOT NULL,
                                         processed_role VARCHAR(255),
                                         action VARCHAR(50) NOT NULL,
                                         resulting_status VARCHAR(50),
                                         comment TEXT,
                                         plan_version INT,

                                         delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         created_by NVARCHAR(255),
                                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         updated_by NVARCHAR(255),


    -- Constraints
                                         CONSTRAINT fk_tta_training_topic FOREIGN KEY (training_topic_id) REFERENCES training_topics(id),
                                         CONSTRAINT fk_tta_processed_by FOREIGN KEY (processed_by_user_id) REFERENCES users(id)
);