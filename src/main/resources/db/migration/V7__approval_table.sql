/* ==========================================================================
   PHẦN 1: TẠO BẢNG MỚI ĐỂ LƯU TRỮ LỊCH SỬ DUYỆT (1-N)
   ========================================================================== */
DROP TABLE IF EXISTS training_plan_approval;

CREATE TABLE training_plan_approval (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        training_plan_id BIGINT NOT NULL,
                                        processed_by_user_id BIGINT NOT NULL,
                                        processed_role VARCHAR(50) COMMENT 'Lưu cứng role lúc duyệt (VD: SV, MANAGER)',
                                        action VARCHAR(20) NOT NULL COMMENT 'SUBMIT, APPROVE, REJECT, REVERT',
                                        resulting_status VARCHAR(20) COMMENT 'Trạng thái plan sau khi hành động',
                                        comment TEXT,
                                        plan_version INT,

                                        delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by NVARCHAR(255),
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        updated_by NVARCHAR(255),

    -- Tạo khóa ngoại
                                        FOREIGN KEY (training_plan_id) REFERENCES training_plan(id) ON DELETE CASCADE,
                                        FOREIGN KEY (processed_by_user_id) REFERENCES users(id) ON DELETE CASCADE
);

/* ==========================================================================
   PHẦN 2: MIGRATION DỮ LIỆU CŨ (QUAN TRỌNG)
   Trước khi xóa cột cũ, ta chuyển dữ liệu sang bảng mới để không mất lịch sử
   ========================================================================== */

-- 2.1: Chuyển dữ liệu Supervisor đã duyệt (dựa trên cột verified_by_sv)
INSERT INTO training_plan_approval
(training_plan_id, processed_by_user_id, processed_role, action, resulting_status, created_at)
SELECT
    id,
    verified_by_sv,
    'SUPERVISOR',
    'APPROVE',
    'WAITING_MANAGER',
    COALESCE(verified_at_sv, NOW())
FROM training_plan
WHERE verified_by_sv IS NOT NULL;

-- 2.2: Chuyển dữ liệu Manager đã duyệt (dựa trên cột approved_by_manager)
INSERT INTO training_plan_approval
(training_plan_id, processed_by_user_id, processed_role, action, resulting_status, created_at)
SELECT
    id,
    approved_by_manager,
    'MANAGER',
    'APPROVE',
    'APPROVED',
    COALESCE(approved_at_manager, NOW())
FROM training_plan
WHERE approved_by_manager IS NOT NULL;

-- 3.2: Xóa các khóa ngoại cũ
-- training_plan_ibfk_2 tương ứng với verified_by_sv
-- training_plan_ibfk_3 tương ứng với approved_by_manager
ALTER TABLE training_plan DROP FOREIGN KEY training_plan_ibfk_2;
ALTER TABLE training_plan DROP FOREIGN KEY training_plan_ibfk_3;

ALTER TABLE training_plan DROP INDEX verified_by_sv;
ALTER TABLE training_plan DROP INDEX approved_by_manager;

-- 3.4: Xóa các cột không còn dùng
ALTER TABLE training_plan DROP COLUMN verified_by_sv;
ALTER TABLE training_plan DROP COLUMN verified_at_sv;
ALTER TABLE training_plan DROP COLUMN approved_by_manager;
ALTER TABLE training_plan DROP COLUMN approved_at_manager;

CREATE TABLE training_result_approval (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        training_result_id BIGINT NOT NULL,
                                        processed_by_user_id BIGINT NOT NULL,
                                        processed_role VARCHAR(50) COMMENT 'Lưu cứng role lúc duyệt (VD: SV, MANAGER)',
                                        action VARCHAR(20) NOT NULL COMMENT 'SUBMIT, APPROVE, REJECT, REVERT',
                                        resulting_status VARCHAR(20) COMMENT 'Trạng thái result sau khi hành động',
                                        comment TEXT,
                                        plan_version INT,

                                        delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by NVARCHAR(255),
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        updated_by NVARCHAR(255),

    -- Tạo khóa ngoại
                                        FOREIGN KEY (training_result_id) REFERENCES training_result(id) ON DELETE CASCADE,
                                        FOREIGN KEY (processed_by_user_id) REFERENCES users(id) ON DELETE CASCADE

);