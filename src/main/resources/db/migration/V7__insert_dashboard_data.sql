-- ============================================================================
-- V7__insert_dashboard_data.sql

-- Thêm dữ liệu giả lập cho màn hình Dashboard cực kỳ lớn (hơn 50 record/bảng)
-- Viết tường minh (explicit INSERT) theo yêu cầu!
-- ============================================================================

-- Bỏ qua foreign key check để insert dễ
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Xoá mọi dữ liệu mock sinh ra từ trước (Dựa trên system/note đã quy ước)
DELETE
FROM training_plan_details
WHERE batch_id LIKE 'dashboard-mock%';
DELETE
FROM training_result_details
WHERE note LIKE '%Dashboard mock%';
DELETE
FROM employee_skills
WHERE created_by = 'system_mock';
DELETE
FROM defects
WHERE note LIKE 'Dashboard mock defect%';

-- 2. Định nghĩa biến thời gian tương đối
SET @today = CURRENT_DATE;
SET @yesterday = DATE_SUB(@today, INTERVAL 1 DAY);

-- ============================================================================
-- 3. TRAINING TASKS (Hơn 50 lượt thực thi Huấn Luyện)
-- ============================================================================

-- A. 12 Huấn Luyện Hôm Nay (PENDING)
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES (3, 1, 'dashboard-mock-1', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 2, 'dashboard-mock-2', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 3, 'dashboard-mock-3', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 4, 'dashboard-mock-4', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 5, 'dashboard-mock-5', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 6, 'dashboard-mock-6', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 1, 'dashboard-mock-7', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 2, 'dashboard-mock-8', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 3, 'dashboard-mock-9', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 4, 'dashboard-mock-10', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 5, 'dashboard-mock-11', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW()),
       (3, 6, 'dashboard-mock-12', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING_REVIEW', 'Dashboard mock',
        'system',
        0, NOW(), NOW());

-- B. 15 Bị lỡ hẹn (PENDING - Quá khứ gần: 1-5 ngày trước)
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES (3, 1, 'dashboard-mock-miss-1', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 1 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 2, 'dashboard-mock-miss-2', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 1 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 3, 'dashboard-mock-miss-3', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 4, 'dashboard-mock-miss-4', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 5, 'dashboard-mock-miss-5', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 6, 'dashboard-mock-miss-6', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 3 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 1, 'dashboard-mock-miss-7', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 3 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 2, 'dashboard-mock-miss-8', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 4 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 3, 'dashboard-mock-miss-9', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 4 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 4, 'dashboard-mock-miss-10', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 4 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 5, 'dashboard-mock-miss-11', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 6, 'dashboard-mock-miss-12', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 1, 'dashboard-mock-miss-13', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 2, 'dashboard-mock-miss-14', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 6 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW()),
       (3, 3, 'dashboard-mock-miss-15', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 6 DAY), NULL,
        'PENDING_REVIEW', 'Dashboard mock', 'system', 0, NOW(), NOW());

-- C. 30 Lịch sử đã huấn luyện (COMPLETED - rải rác 30 ngày qua)
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES (3, 1, 'dashboard-mock-hist-1', DATE_FORMAT(DATE_SUB(@today, INTERVAL 1 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 2, 'dashboard-mock-hist-2', DATE_FORMAT(DATE_SUB(@today, INTERVAL 1 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 3, 'dashboard-mock-hist-3', DATE_FORMAT(DATE_SUB(@today, INTERVAL 2 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 4, 'dashboard-mock-hist-4', DATE_FORMAT(DATE_SUB(@today, INTERVAL 2 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 5, 'dashboard-mock-hist-5', DATE_FORMAT(DATE_SUB(@today, INTERVAL 3 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 3 DAY), DATE_SUB(@today, INTERVAL 3 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 6, 'dashboard-mock-hist-6', DATE_FORMAT(DATE_SUB(@today, INTERVAL 4 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 4 DAY), DATE_SUB(@today, INTERVAL 4 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 1, 'dashboard-mock-hist-7', DATE_FORMAT(DATE_SUB(@today, INTERVAL 5 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 2, 'dashboard-mock-hist-8', DATE_FORMAT(DATE_SUB(@today, INTERVAL 5 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 3, 'dashboard-mock-hist-9', DATE_FORMAT(DATE_SUB(@today, INTERVAL 6 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 6 DAY), DATE_SUB(@today, INTERVAL 6 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 4, 'dashboard-mock-hist-10', DATE_FORMAT(DATE_SUB(@today, INTERVAL 7 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 7 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 5, 'dashboard-mock-hist-11', DATE_FORMAT(DATE_SUB(@today, INTERVAL 8 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 8 DAY), DATE_SUB(@today, INTERVAL 8 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 6, 'dashboard-mock-hist-12', DATE_FORMAT(DATE_SUB(@today, INTERVAL 9 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 9 DAY), DATE_SUB(@today, INTERVAL 9 DAY), 'COMPLETED', 'Dashboard mock', 'system', 0,
        NOW(), NOW()),
       (3, 1, 'dashboard-mock-hist-13', DATE_FORMAT(DATE_SUB(@today, INTERVAL 10 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 10 DAY), DATE_SUB(@today, INTERVAL 10 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 2, 'dashboard-mock-hist-14', DATE_FORMAT(DATE_SUB(@today, INTERVAL 11 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 11 DAY), DATE_SUB(@today, INTERVAL 11 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 3, 'dashboard-mock-hist-15', DATE_FORMAT(DATE_SUB(@today, INTERVAL 12 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 12 DAY), DATE_SUB(@today, INTERVAL 12 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 4, 'dashboard-mock-hist-16', DATE_FORMAT(DATE_SUB(@today, INTERVAL 13 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 13 DAY), DATE_SUB(@today, INTERVAL 13 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 5, 'dashboard-mock-hist-17', DATE_FORMAT(DATE_SUB(@today, INTERVAL 14 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 14 DAY), DATE_SUB(@today, INTERVAL 14 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 6, 'dashboard-mock-hist-18', DATE_FORMAT(DATE_SUB(@today, INTERVAL 15 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 15 DAY), DATE_SUB(@today, INTERVAL 15 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 1, 'dashboard-mock-hist-19', DATE_FORMAT(DATE_SUB(@today, INTERVAL 16 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 16 DAY), DATE_SUB(@today, INTERVAL 16 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 2, 'dashboard-mock-hist-20', DATE_FORMAT(DATE_SUB(@today, INTERVAL 17 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 17 DAY), DATE_SUB(@today, INTERVAL 17 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 3, 'dashboard-mock-hist-21', DATE_FORMAT(DATE_SUB(@today, INTERVAL 18 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 18 DAY), DATE_SUB(@today, INTERVAL 18 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 4, 'dashboard-mock-hist-22', DATE_FORMAT(DATE_SUB(@today, INTERVAL 19 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 19 DAY), DATE_SUB(@today, INTERVAL 19 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 5, 'dashboard-mock-hist-23', DATE_FORMAT(DATE_SUB(@today, INTERVAL 20 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 20 DAY), DATE_SUB(@today, INTERVAL 20 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 6, 'dashboard-mock-hist-24', DATE_FORMAT(DATE_SUB(@today, INTERVAL 21 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 21 DAY), DATE_SUB(@today, INTERVAL 21 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 1, 'dashboard-mock-hist-25', DATE_FORMAT(DATE_SUB(@today, INTERVAL 22 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 22 DAY), DATE_SUB(@today, INTERVAL 22 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 2, 'dashboard-mock-hist-26', DATE_FORMAT(DATE_SUB(@today, INTERVAL 24 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 24 DAY), DATE_SUB(@today, INTERVAL 24 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 3, 'dashboard-mock-hist-27', DATE_FORMAT(DATE_SUB(@today, INTERVAL 26 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 26 DAY), DATE_SUB(@today, INTERVAL 26 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 4, 'dashboard-mock-hist-28', DATE_FORMAT(DATE_SUB(@today, INTERVAL 27 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 27 DAY), DATE_SUB(@today, INTERVAL 27 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 5, 'dashboard-mock-hist-29', DATE_FORMAT(DATE_SUB(@today, INTERVAL 29 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 29 DAY), DATE_SUB(@today, INTERVAL 29 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW()),
       (3, 6, 'dashboard-mock-hist-30', DATE_FORMAT(DATE_SUB(@today, INTERVAL 30 DAY), '%Y-%m-01'),
        DATE_SUB(@today, INTERVAL 30 DAY), DATE_SUB(@today, INTERVAL 30 DAY), 'COMPLETED', 'Dashboard mock', 'system',
        0,
        NOW(), NOW());


-- D. 30 Kết quả (Result Details) cho 30 task lịch sử ở C.
-- 6 Task trượt (Fail = FALSE) hôm qua / vài hôm trước, 24 task kia Đỗ (Pass = TRUE)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-1'), 1, 1,
        DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'COMPLETED', FALSE,
        'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-2'), 2, 2,
        DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'COMPLETED', FALSE,
        'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-3'), 3, 3,
        DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'COMPLETED', FALSE,
        'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-4'), 4, 4,
        DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'COMPLETED', FALSE,
        'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-5'), 5, 5,
        DATE_SUB(@today, INTERVAL 3 DAY), DATE_SUB(@today, INTERVAL 3 DAY), 'COMPLETED', FALSE,
        'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-6'), 6, 6,
        DATE_SUB(@today, INTERVAL 4 DAY), DATE_SUB(@today, INTERVAL 4 DAY), 'COMPLETED', FALSE,
        'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-7'), 1, 1,
        DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-8'), 2, 2,
        DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-9'), 3, 3,
        DATE_SUB(@today, INTERVAL 6 DAY), DATE_SUB(@today, INTERVAL 6 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-10'), 4, 4,
        DATE_SUB(@today, INTERVAL 7 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-11'), 5, 5,
        DATE_SUB(@today, INTERVAL 8 DAY), DATE_SUB(@today, INTERVAL 8 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-12'), 6, 6,
        DATE_SUB(@today, INTERVAL 9 DAY), DATE_SUB(@today, INTERVAL 9 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-13'), 1, 1,
        DATE_SUB(@today, INTERVAL 10 DAY), DATE_SUB(@today, INTERVAL 10 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-14'), 2, 2,
        DATE_SUB(@today, INTERVAL 11 DAY), DATE_SUB(@today, INTERVAL 11 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-15'), 3, 3,
        DATE_SUB(@today, INTERVAL 12 DAY), DATE_SUB(@today, INTERVAL 12 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-16'), 4, 4,
        DATE_SUB(@today, INTERVAL 13 DAY), DATE_SUB(@today, INTERVAL 13 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-17'), 5, 5,
        DATE_SUB(@today, INTERVAL 14 DAY), DATE_SUB(@today, INTERVAL 14 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-18'), 6, 6,
        DATE_SUB(@today, INTERVAL 15 DAY), DATE_SUB(@today, INTERVAL 15 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-19'), 1, 1,
        DATE_SUB(@today, INTERVAL 16 DAY), DATE_SUB(@today, INTERVAL 16 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-20'), 2, 2,
        DATE_SUB(@today, INTERVAL 17 DAY), DATE_SUB(@today, INTERVAL 17 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-21'), 3, 3,
        DATE_SUB(@today, INTERVAL 18 DAY), DATE_SUB(@today, INTERVAL 18 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-22'), 4, 4,
        DATE_SUB(@today, INTERVAL 19 DAY), DATE_SUB(@today, INTERVAL 19 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-23'), 5, 5,
        DATE_SUB(@today, INTERVAL 20 DAY), DATE_SUB(@today, INTERVAL 20 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-24'), 6, 6,
        DATE_SUB(@today, INTERVAL 21 DAY), DATE_SUB(@today, INTERVAL 21 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-25'), 1, 1,
        DATE_SUB(@today, INTERVAL 22 DAY), DATE_SUB(@today, INTERVAL 22 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-26'), 2, 2,
        DATE_SUB(@today, INTERVAL 24 DAY), DATE_SUB(@today, INTERVAL 24 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-27'), 3, 3,
        DATE_SUB(@today, INTERVAL 26 DAY), DATE_SUB(@today, INTERVAL 26 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-28'), 4, 4,
        DATE_SUB(@today, INTERVAL 27 DAY), DATE_SUB(@today, INTERVAL 27 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-29'), 5, 5,
        DATE_SUB(@today, INTERVAL 29 DAY), DATE_SUB(@today, INTERVAL 29 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-30'), 6, 6,
        DATE_SUB(@today, INTERVAL 30 DAY), DATE_SUB(@today, INTERVAL 30 DAY), 'COMPLETED', TRUE,
        'Dashboard mock pass result', 'system', 0, NOW(), NOW());

-- E. Training Result Details ở trạng thái PENDING, liên kết đầy đủ về Training Plan
-- Liên kết: training_result_details (PENDING) → training_results (id=9) → training_plans (id=3, COMPLETED)
--           training_result_details → training_plan_details (PENDING) → training_plans (id=3)
-- 12 dòng cho 12 plan details PENDING hôm nay (dashboard-mock-1 đến dashboard-mock-12)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-1'), 1, 1, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-2'), 2, 2, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-3'), 3, 3, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-4'), 4, 4, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-5'), 5, 5, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-6'), 6, 6, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-7'), 1, 1, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-8'), 2, 2, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-9'), 3, 3, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-10'), 4, 4, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-11'), 5, 5, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW()),
       (9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-12'), 6, 6, @today, NULL,
        'PENDING_REVIEW',
        NULL, 'Dashboard mock pending result - chờ huấn luyện', 'system', 0, NOW(), NOW());

-- F. 2 dòng PENDING result cho các plan details có sẵn từ V6 (IDs 13, 14 - NV003 & NV004 thuộc Plan 3, T3)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (9, 13, 3, 2, '2026-03-11', NULL, 'PENDING_REVIEW', NULL, 'NV003 - chờ huấn luyện T3', 'tl_tien01', 0, NOW(),
        NOW()),
       (9, 14, 4, 1, '2026-03-12', NULL, 'PENDING_REVIEW', NULL, 'NV004 - chờ huấn luyện T3', 'tl_tien01', 0, NOW(),
        NOW());


-- ============================================================================
-- 4. EMPLOYEE SKILLS (Hơn 50 Chứng chỉ Kỹ năng trãi đều 5 process)
-- ============================================================================

INSERT INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by, delete_flag,
                             created_at, updated_at)
VALUES
-- Process 1 (10 staff)
(1, 1, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(2, 1, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(3, 1, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(4, 1, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(5, 1, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 5 DAY), 'system_mock', 0, NOW(), NOW()),
(6, 1, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 12 DAY), 'system_mock', 0, NOW(), NOW()),
(7, 1, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(8, 1, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(9, 1, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(10, 1, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
-- Process 2 (10 staff)
(1, 2, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(2, 2, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 8 DAY), 'system_mock', 0, NOW(), NOW()),
(3, 2, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(4, 2, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(5, 2, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(6, 2, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(7, 2, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 15 DAY), 'system_mock', 0, NOW(), NOW()),
(8, 2, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(9, 2, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(10, 2, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
-- Process 3 (10 staff)
(1, 3, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 2 DAY), 'system_mock', 0, NOW(), NOW()),
(2, 3, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(3, 3, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(4, 3, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(5, 3, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 18 DAY), 'system_mock', 0, NOW(), NOW()),
(6, 3, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(7, 3, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(8, 3, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(9, 3, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(10, 3, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
-- Process 4 (10 staff)
(1, 4, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(2, 4, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(3, 4, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 25 DAY), 'system_mock', 0, NOW(), NOW()),
(4, 4, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(5, 4, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(6, 4, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 20 DAY), 'system_mock', 0, NOW(), NOW()),
(7, 4, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(8, 4, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(9, 4, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(10, 4, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
-- Process 5 (10 staff)
(1, 5, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(2, 5, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(3, 5, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(4, 5, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 10 DAY), 'system_mock', 0, NOW(), NOW()),
(5, 5, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(6, 5, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(7, 5, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(8, 5, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 4 DAY), 'system_mock', 0, NOW(), NOW()),
(9, 5, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW()),
(10, 5, 'VALID', '2024-01-01', '2027-01-01', 'system_mock', 0, NOW(), NOW());

-- ============================================================================
-- 5. DEFECTS (60 Lỗi lịch sử rải đều quá khứ phục vụ biểu đồ Trend)
-- ============================================================================
INSERT INTO defects (defect_code, defect_description, process_id, detected_date, defect_type, origin_measures,
                     outflow_measures, conclusion, note, created_by, delete_flag, created_at, updated_at)
VALUES ('DF101', 'Lỗi giả lập MOCK 01', 1, DATE_SUB(@today, INTERVAL 1 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF102', 'Lỗi giả lập MOCK 02', 2, DATE_SUB(@today, INTERVAL 1 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF103', 'Lỗi giả lập MOCK 03', 3, DATE_SUB(@today, INTERVAL 1 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF104', 'Lỗi giả lập MOCK 04', 4, DATE_SUB(@today, INTERVAL 2 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF105', 'Lỗi giả lập MOCK 05', 1, DATE_SUB(@today, INTERVAL 2 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF106', 'Lỗi giả lập MOCK 06', 2, DATE_SUB(@today, INTERVAL 3 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF107', 'Lỗi giả lập MOCK 07', 3, DATE_SUB(@today, INTERVAL 3 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF108', 'Lỗi giả lập MOCK 08', 4, DATE_SUB(@today, INTERVAL 4 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF109', 'Lỗi giả lập MOCK 09', 1, DATE_SUB(@today, INTERVAL 4 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF110', 'Lỗi giả lập MOCK 10', 2, DATE_SUB(@today, INTERVAL 5 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

       ('DF111', 'Lỗi giả lập MOCK 11', 3, DATE_SUB(@today, INTERVAL 5 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF112', 'Lỗi giả lập MOCK 12', 4, DATE_SUB(@today, INTERVAL 6 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF113', 'Lỗi giả lập MOCK 13', 1, DATE_SUB(@today, INTERVAL 6 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF114', 'Lỗi giả lập MOCK 14', 2, DATE_SUB(@today, INTERVAL 7 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF115', 'Lỗi giả lập MOCK 15', 3, DATE_SUB(@today, INTERVAL 8 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF116', 'Lỗi giả lập MOCK 16', 4, DATE_SUB(@today, INTERVAL 8 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF117', 'Lỗi giả lập MOCK 17', 1, DATE_SUB(@today, INTERVAL 9 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF118', 'Lỗi giả lập MOCK 18', 2, DATE_SUB(@today, INTERVAL 10 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF119', 'Lỗi giả lập MOCK 19', 3, DATE_SUB(@today, INTERVAL 11 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF120', 'Lỗi giả lập MOCK 20', 4, DATE_SUB(@today, INTERVAL 12 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

       ('DF121', 'Lỗi giả lập MOCK 21', 1, DATE_SUB(@today, INTERVAL 13 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF122', 'Lỗi giả lập MOCK 22', 2, DATE_SUB(@today, INTERVAL 14 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF123', 'Lỗi giả lập MOCK 23', 3, DATE_SUB(@today, INTERVAL 15 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF124', 'Lỗi giả lập MOCK 24', 4, DATE_SUB(@today, INTERVAL 16 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF125', 'Lỗi giả lập MOCK 25', 1, DATE_SUB(@today, INTERVAL 17 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF126', 'Lỗi giả lập MOCK 26', 2, DATE_SUB(@today, INTERVAL 17 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF127', 'Lỗi giả lập MOCK 27', 3, DATE_SUB(@today, INTERVAL 18 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF128', 'Lỗi giả lập MOCK 28', 4, DATE_SUB(@today, INTERVAL 19 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF129', 'Lỗi giả lập MOCK 29', 1, DATE_SUB(@today, INTERVAL 20 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF130', 'Lỗi giả lập MOCK 30', 2, DATE_SUB(@today, INTERVAL 21 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

       ('DF131', 'Lỗi giả lập MOCK 31', 3, DATE_SUB(@today, INTERVAL 22 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF132', 'Lỗi giả lập MOCK 32', 4, DATE_SUB(@today, INTERVAL 23 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF133', 'Lỗi giả lập MOCK 33', 1, DATE_SUB(@today, INTERVAL 24 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF134', 'Lỗi giả lập MOCK 34', 2, DATE_SUB(@today, INTERVAL 25 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF135', 'Lỗi giả lập MOCK 35', 3, DATE_SUB(@today, INTERVAL 25 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF136', 'Lỗi giả lập MOCK 36', 4, DATE_SUB(@today, INTERVAL 26 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF137', 'Lỗi giả lập MOCK 37', 1, DATE_SUB(@today, INTERVAL 26 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF138', 'Lỗi giả lập MOCK 38', 2, DATE_SUB(@today, INTERVAL 27 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF139', 'Lỗi giả lập MOCK 39', 3, DATE_SUB(@today, INTERVAL 28 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF140', 'Lỗi giả lập MOCK 40', 4, DATE_SUB(@today, INTERVAL 28 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

       ('DF141', 'Lỗi giả lập MOCK 41', 1, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF142', 'Lỗi giả lập MOCK 42', 2, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF143', 'Lỗi giả lập MOCK 43', 3, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF144', 'Lỗi giả lập MOCK 44', 4, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF145', 'Lỗi giả lập MOCK 45', 1, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF146', 'Lỗi giả lập MOCK 46', 2, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF147', 'Lỗi giả lập MOCK 47', 3, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF148', 'Lỗi giả lập MOCK 48', 4, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF149', 'Lỗi giả lập MOCK 49', 1, DATE_SUB(@today, INTERVAL 31 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
       ('DF150', 'Lỗi giả lập MOCK 50', 2, DATE_SUB(@today, INTERVAL 31 DAY), 'DEFECTIVE_GOODS', 'Measure 1',
        'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW());


-- 1. Rejected Training Plan for tl_tien01
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES (901, 'TP-REJ-TIEN-01', 'Kế hoạch đào tạo tháng 3 - Bị từ chối', '2026-03-01', '2026-03-31', 1, 1, 'REJECTED', 1,
        'Thiếu thông tin nhân viên G54.', 1, 3, 'tl_tien01');

-- 2. Rejected Training Result for tl_tien01
-- Note: status REJECTED_BY_MANAGER since REJECTED_BY_SV is not in the results enum
INSERT INTO training_results (id, training_plan_id, title, form_code, year, line_id, team_id, status, current_version,
                              note, created_by)
VALUES (901, 1, 'Kết quả đào tạo T1/2026 - Bị từ chối', 'TR-REJ-TIEN-01', 2026, 1, 1, 'REJECTED', 1,
        'Hình ảnh minh chứng không rõ ràng.', 'tl_tien01');

-- 3. Rejected Defect Proposal for tl_tien01
INSERT INTO defect_proposals (id, product_line_id, status, current_version, form_code, delete_flag, created_by)
VALUES (901, 1, 'REJECTED', 1, 'DEF-REJ-TIEN-01', FALSE, 'tl_tien01');

-- 4. Rejected Training Sample Proposal for tl_tien01
INSERT INTO training_sample_proposals (id, product_line_id, status, current_version, form_code, delete_flag, created_by)
VALUES (901, 1, 'REJECTED', 1, 'SAM-REJ-TIEN-01', FALSE, 'tl_tien01');

-- ============================================================================
-- 6. THÊM DỮ LIỆU TRAINING PLAN + RESULT LIÊN KẾT ĐẦY ĐỦ (T4/2026)
--    Quy tắc: 1 employee có 1 batch_id duy nhất trong 1 kế hoạch.
--    Nếu ngày kế hoạch MISS → thêm dòng mới cùng batch_id, planned_date mới.
--    1 batch_id tương ứng 1 result_detail (liên kết tới dòng PENDING/COMPLETED mới nhất).
-- ============================================================================

-- ── 6A. TRAINING PLANS (6 headers cho T4/2026, nhiều team) ──────────────────
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES (100, 'TP-PH-2026-004', 'Kế hoạch HLV T4/2026 - Tổ Phay Ca Ngày', '2026-04-01', '2026-04-30', 2, 2, 'COMPLETED',
        1, 'Tháng 4 đã duyệt, đang thực hiện.', 1, 3, 'tl_phay01'),
       (101, 'TP-HA-2026-002', 'Kế hoạch HLV T2/2026 - Tổ Hàn & Nhiệt', '2026-02-03', '2026-02-28', 3, 3, 'COMPLETED',
        1, 'Tổ Hàn T2 đã duyệt.', 1, 3, 'tl_hanlap01'),
       (102, 'TP-HA-2026-004', 'Kế hoạch HLV T4/2026 - Tổ Hàn & Nhiệt', '2026-04-01', '2026-04-30', 3, 3, 'COMPLETED',
        1, 'Tổ Hàn T4 đã duyệt, toàn bộ pending.', 1, 3, 'tl_hanlap01'),
       (103, 'TP-B-2026-004', 'Kế hoạch HLV T4/2026 - Tổ Lắp Ráp Bơm', '2026-04-01', '2026-04-30', 4, 5, 'COMPLETED', 1,
        'T4 đã duyệt, đang thực hiện.', 1, 3, 'tl_laprap01'),
       (104, 'TP-DC-2026-004', 'Kế hoạch HLV T4/2026 - Tổ Động Cơ', '2026-04-01', '2026-04-30', 5, 4, 'COMPLETED', 1,
        'T4 đã duyệt, chờ MG duyệt kết quả.', 1, 2, 'tl_dongco01'),
       (105, 'TP-KCS-2026-004', 'Kế hoạch HLV T4/2026 - Tổ KCS', '2026-04-01', '2026-04-30', 6, 5, 'COMPLETED', 1,
        'T4 KCS đã duyệt.', 1, 2, 'tl_kcs01');

-- ── 6B. TRAINING PLAN DETAILS ───────────────────────────────────────────────
-- Quy tắc: 1 employee = 1 batch_id. Nhiều dòng cùng batch_id = lên lịch lại.
-- Dòng cũ: MISS (ngày KH cũ không thực hiện). Dòng mới: PENDING (ngày KH mới).
-- Nếu đã hoàn thành: 1 dòng COMPLETED.

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 100: Tổ Phay T4 │ 5 employees │ 7 rows (2 đã lên lịch lại)   ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- NV007: đã hoàn thành → 1 dòng COMPLETED
(100, 7, 'pend-mock-ph4-nv007', '2026-04-01', '2026-04-03', '2026-04-03', 'COMPLETED', 'NV007 đã huấn luyện đạt',
 'tl_phay01', 0, NOW(), NOW()),
-- NV008: đã hoàn thành → 1 dòng COMPLETED
(100, 8, 'pend-mock-ph4-nv008', '2026-04-01', '2026-04-04', '2026-04-04', 'COMPLETED', 'NV008 đã huấn luyện đạt',
 'tl_phay01', 0, NOW(), NOW()),
-- NV009: ngày 04-07 MISS → lên lịch lại 04-14 PENDING → 2 dòng cùng batch_id
(100, 9, 'pend-mock-ph4-nv009', '2026-04-01', '2026-04-07', NULL, 'MISSED', 'NV009 vắng ngày 07/04', 'tl_phay01', 0,
 NOW(), NOW()),
(100, 9, 'pend-mock-ph4-nv009', '2026-04-01', '2026-04-14', NULL, 'PENDING_REVIEW', 'NV009 lên lịch lại 14/04',
 'tl_phay01', 0,
 NOW(), NOW()),
-- NV010: chưa đến ngày → 1 dòng PENDING
(100, 10, 'pend-mock-ph4-nv010', '2026-04-01', '2026-04-08', NULL, 'PENDING_REVIEW', 'NV010 chờ huấn luyện',
 'tl_phay01', 0,
 NOW(), NOW()),
-- NV012: ngày 04-03 MISS → lên lịch lại 04-09 PENDING → 2 dòng cùng batch_id
(100, 12, 'pend-mock-ph4-nv012', '2026-04-01', '2026-04-03', NULL, 'MISSED', 'NV012 vắng ngày 03/04', 'tl_phay01', 0,
 NOW(), NOW()),
(100, 12, 'pend-mock-ph4-nv012', '2026-04-01', '2026-04-09', NULL, 'PENDING_REVIEW', 'NV012 lên lịch lại 09/04',
 'tl_phay01',
 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 101: Tổ Hàn T2 │ 4 employees │ 5 rows (1 đã lên lịch lại)    ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES (101, 13, 'pend-mock-ha2-nv013', '2026-02-01', '2026-02-04', '2026-02-04', 'COMPLETED',
        'NV013 đã huấn luyện đạt',
        'tl_hanlap01', 0, NOW(), NOW()),
       (101, 14, 'pend-mock-ha2-nv014', '2026-02-01', '2026-02-05', '2026-02-05', 'COMPLETED',
        'NV014 đã huấn luyện đạt',
        'tl_hanlap01', 0, NOW(), NOW()),
-- NV015: ngày 02-10 MISS → lên lịch lại 02-17 PENDING
       (101, 15, 'pend-mock-ha2-nv015', '2026-02-01', '2026-02-10', NULL, 'MISSED', 'NV015 vắng ngày 10/02',
        'tl_hanlap01', 0, NOW(), NOW()),
       (101, 15, 'pend-mock-ha2-nv015', '2026-02-01', '2026-02-17', NULL, 'PENDING_REVIEW', 'NV015 lên lịch lại 17/02',
        'tl_hanlap01', 0, NOW(), NOW()),
       (101, 16, 'pend-mock-ha2-nv016', '2026-02-01', '2026-02-11', NULL, 'PENDING_REVIEW', 'NV016 chờ huấn luyện',
        'tl_hanlap01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 102: Tổ Hàn T4 │ 4 employees │ 6 rows (2 đã lên lịch lại)    ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- NV013: ngày 04-03 MISS → lên lịch lại 04-10 PENDING
(102, 13, 'pend-mock-ha4-nv013', '2026-04-01', '2026-04-03', NULL, 'MISSED', 'NV013 vắng ngày 03/04', 'tl_hanlap01', 0,
 NOW(), NOW()),
(102, 13, 'pend-mock-ha4-nv013', '2026-04-01', '2026-04-10', NULL, 'PENDING_REVIEW', 'NV013 lên lịch lại 10/04',
 'tl_hanlap01',
 0, NOW(), NOW()),
(102, 14, 'pend-mock-ha4-nv014', '2026-04-01', '2026-04-04', NULL, 'PENDING_REVIEW', 'NV014 chờ huấn luyện',
 'tl_hanlap01', 0,
 NOW(), NOW()),
-- NV015: ngày 04-07 MISS → lên lịch lại 04-14 PENDING
(102, 15, 'pend-mock-ha4-nv015', '2026-04-01', '2026-04-07', NULL, 'MISSED', 'NV015 vắng ngày 07/04', 'tl_hanlap01', 0,
 NOW(), NOW()),
(102, 15, 'pend-mock-ha4-nv015', '2026-04-01', '2026-04-14', NULL, 'PENDING_REVIEW', 'NV015 lên lịch lại 14/04',
 'tl_hanlap01',
 0, NOW(), NOW()),
(102, 16, 'pend-mock-ha4-nv016', '2026-04-01', '2026-04-08', NULL, 'PENDING_REVIEW', 'NV016 chờ huấn luyện',
 'tl_hanlap01', 0,
 NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 103: Tổ Lắp Bơm T4 │ 4 employees │ 7 rows (2 lên lịch lại)  ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES (103, 17, 'pend-mock-lb4-nv017', '2026-04-01', '2026-04-03', '2026-04-03', 'COMPLETED',
        'NV017 đã huấn luyện đạt',
        'tl_laprap01', 0, NOW(), NOW()),
-- NV018: ngày 04-04 MISS → lên lịch lại 04-11 PENDING
       (103, 18, 'pend-mock-lb4-nv018', '2026-04-01', '2026-04-04', NULL, 'MISSED', 'NV018 vắng ngày 04/04',
        'tl_laprap01', 0, NOW(), NOW()),
       (103, 18, 'pend-mock-lb4-nv018', '2026-04-01', '2026-04-11', NULL, 'PENDING_REVIEW', 'NV018 lên lịch lại 11/04',
        'tl_laprap01', 0, NOW(), NOW()),
       (103, 19, 'pend-mock-lb4-nv019', '2026-04-01', '2026-04-07', NULL, 'PENDING_REVIEW', 'NV019 chờ huấn luyện',
        'tl_laprap01', 0, NOW(), NOW()),
-- NV020: ngày 04-08 MISS → 04-15 MISS → lên lịch lại 04-22 PENDING (3 dòng cùng batch)
       (103, 20, 'pend-mock-lb4-nv020', '2026-04-01', '2026-04-08', NULL, 'MISSED', 'NV020 vắng lần 1 ngày 08/04',
        'tl_laprap01', 0, NOW(), NOW()),
       (103, 20, 'pend-mock-lb4-nv020', '2026-04-01', '2026-04-15', NULL, 'MISSED', 'NV020 vắng lần 2 ngày 15/04',
        'tl_laprap01', 0, NOW(), NOW()),
       (103, 20, 'pend-mock-lb4-nv020', '2026-04-01', '2026-04-22', NULL, 'PENDING_REVIEW',
        'NV020 lên lịch lần 3 ngày 22/04',
        'tl_laprap01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 104: Tổ Động Cơ T4 │ 5 employees │ 8 rows (2 lên lịch lại)  ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES (104, 22, 'pend-mock-dc4-nv022', '2026-04-01', '2026-04-03', '2026-04-03', 'COMPLETED',
        'NV022 đã huấn luyện đạt',
        'tl_dongco01', 0, NOW(), NOW()),
       (104, 23, 'pend-mock-dc4-nv023', '2026-04-01', '2026-04-04', '2026-04-04', 'COMPLETED',
        'NV023 đã huấn luyện đạt',
        'tl_dongco01', 0, NOW(), NOW()),
-- NV024: ngày 04-07 MISS → lên lịch lại 04-14 PENDING
       (104, 24, 'pend-mock-dc4-nv024', '2026-04-01', '2026-04-07', NULL, 'MISSED', 'NV024 vắng ngày 07/04',
        'tl_dongco01', 0, NOW(), NOW()),
       (104, 24, 'pend-mock-dc4-nv024', '2026-04-01', '2026-04-14', NULL, 'PENDING_REVIEW', 'NV024 lên lịch lại 14/04',
        'tl_dongco01', 0, NOW(), NOW()),
       (104, 25, 'pend-mock-dc4-nv025', '2026-04-01', '2026-04-08', NULL, 'PENDING_REVIEW', 'NV025 chờ huấn luyện',
        'tl_dongco01', 0, NOW(), NOW()),
-- NV026: ngày 04-03 MISS → 04-09 MISS → lên lịch lại 04-16 PENDING
       (104, 26, 'pend-mock-dc4-nv026', '2026-04-01', '2026-04-03', NULL, 'MISSED', 'NV026 vắng lần 1 ngày 03/04',
        'tl_dongco01', 0, NOW(), NOW()),
       (104, 26, 'pend-mock-dc4-nv026', '2026-04-01', '2026-04-09', NULL, 'MISSED', 'NV026 vắng lần 2 ngày 09/04',
        'tl_dongco01', 0, NOW(), NOW()),
       (104, 26, 'pend-mock-dc4-nv026', '2026-04-01', '2026-04-16', NULL, 'PENDING_REVIEW',
        'NV026 lên lịch lần 3 ngày 16/04',
        'tl_dongco01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 105: Tổ KCS T4 │ 4 employees │ 6 rows (1 lên lịch lại)      ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES (105, 27, 'pend-mock-kcs4-nv027', '2026-04-01', '2026-04-03', '2026-04-03', 'COMPLETED',
        'NV027 đã huấn luyện đạt',
        'tl_kcs01', 0, NOW(), NOW()),
-- NV028: ngày 04-04 MISS → lên lịch lại 04-11 PENDING
       (105, 28, 'pend-mock-kcs4-nv028', '2026-04-01', '2026-04-04', NULL, 'MISSED', 'NV028 vắng ngày 04/04',
        'tl_kcs01',
        0, NOW(), NOW()),
       (105, 28, 'pend-mock-kcs4-nv028', '2026-04-01', '2026-04-11', NULL, 'PENDING_REVIEW', 'NV028 lên lịch lại 11/04',
        'tl_kcs01', 0, NOW(), NOW()),
       (105, 29, 'pend-mock-kcs4-nv029', '2026-04-01', '2026-04-07', NULL, 'PENDING_REVIEW', 'NV029 chờ huấn luyện',
        'tl_kcs01', 0, NOW(), NOW()),
       (105, 30, 'pend-mock-kcs4-nv030', '2026-04-01', '2026-04-08', NULL, 'PENDING_REVIEW', 'NV030 chờ huấn luyện',
        'tl_kcs01', 0, NOW(), NOW());

-- ── 6C. TRAINING RESULTS (6 headers liên kết về 6 plans trên) ───────────────
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES (100, 100, 'Kết quả HLV T4/2026 – Tổ Phay', 'TR-RES-PH-004', 2026, 2, 2, 'ONGOING', 1, 'Đang nhập kết quả T4.',
        'tl_phay01'),
       (101, 101, 'Kết quả HLV T2/2026 – Tổ Hàn', 'TR-RES-HA-001', 2026, 3, 3, 'PENDING_APPROVAL', 1,
        'Chờ MG duyệt kết quả T2 Hàn.', 'tl_hanlap01'),
       (102, 102, 'Kết quả HLV T4/2026 – Tổ Hàn', 'TR-RES-HA-004', 2026, 3, 3, 'ONGOING', 1, 'T4 Hàn đang nhập liệu.',
        'tl_hanlap01'),
       (103, 103, 'Kết quả HLV T4/2026 – Tổ Lắp Bơm', 'TR-RES-LB-004', 2026, 4, 5, 'ONGOING', 1,
        'Đang nhập kết quả T4 lắp bơm.', 'tl_laprap01'),
       (104, 104, 'Kết quả HLV T4/2026 – Tổ Động Cơ', 'TR-RES-DC-004', 2026, 5, 4, 'PENDING_APPROVAL', 1,
        'Chờ MG duyệt kết quả T4 ĐC.', 'tl_dongco01'),
       (105, 105, 'Kết quả HLV T4/2026 – Tổ KCS', 'TR-RES-KCS-004', 2026, 6, 5, 'ONGOING', 1,
        'Đang nhập kết quả T4 KCS.', 'tl_kcs01');

-- ── 6D. TRAINING RESULT DETAILS ─────────────────────────────────────────────
-- Quy tắc: 1 batch_id = 1 result_detail.
-- Liên kết tới dòng PENDING mới nhất (nếu lên lịch lại) hoặc dòng COMPLETED.

-- Result 100 (Phay T4): 5 batch_ids → 5 result details (2 COMPLETED + 3 PENDING)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES
-- NV007 batch → liên kết dòng COMPLETED
(100, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ph4-nv007' AND status = 'COMPLETED'), 7, 7,
 '2026-04-03', '2026-04-03', 'COMPLETED', TRUE, 'NV007 đạt T4', 'tl_phay01', 0, NOW(), NOW()),
-- NV008 batch → liên kết dòng COMPLETED
(100, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ph4-nv008' AND status = 'COMPLETED'), 8, 8,
 '2026-04-04', '2026-04-04', 'COMPLETED', TRUE, 'NV008 đạt T4', 'tl_phay01', 0, NOW(), NOW()),
-- NV009 batch → liên kết dòng PENDING (ngày lên lịch lại)
(100, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ph4-nv009' AND status = 'PENDING_REVIEW'), 9, 9,
 '2026-04-14', NULL, 'PENDING_REVIEW', NULL, 'NV009 chờ HLV ngày mới', 'tl_phay01', 0, NOW(), NOW()),
-- NV010 batch → liên kết dòng PENDING
(100, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ph4-nv010' AND status = 'PENDING_REVIEW'), 10,
 10,
 '2026-04-08', NULL, 'PENDING_REVIEW', NULL, 'NV010 chờ HLV', 'tl_phay01', 0, NOW(), NOW()),
-- NV012 batch → liên kết dòng PENDING (ngày lên lịch lại)
(100, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ph4-nv012' AND status = 'PENDING_REVIEW'), 12,
 8,
 '2026-04-09', NULL, 'PENDING_REVIEW', NULL, 'NV012 chờ HLV ngày mới', 'tl_phay01', 0, NOW(), NOW());

-- Result 101 (Hàn T2): 4 batch_ids → 4 result details (2 COMPLETED + 2 PENDING)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (101, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha2-nv013' AND status = 'COMPLETED'), 13,
        10,
        '2026-02-04', '2026-02-04', 'COMPLETED', TRUE, 'NV013 đạt T2 Hàn', 'tl_hanlap01', 0, NOW(), NOW()),
       (101, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha2-nv014' AND status = 'COMPLETED'), 14,
        11,
        '2026-02-05', '2026-02-05', 'COMPLETED', FALSE, 'NV014 trượt T2 Hàn', 'tl_hanlap01', 0, NOW(), NOW()),
       (101,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha2-nv015' AND status = 'PENDING_REVIEW'), 15,
        12, '2026-02-17', NULL, 'PENDING_REVIEW', NULL, 'NV015 chờ HLV ngày mới', 'tl_hanlap01', 0, NOW(), NOW()),
       (101,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha2-nv016' AND status = 'PENDING_REVIEW'), 16,
        10, '2026-02-11', NULL, 'PENDING_REVIEW', NULL, 'NV016 chờ HLV', 'tl_hanlap01', 0, NOW(), NOW());

-- Result 102 (Hàn T4): 4 batch_ids → 4 result details (tất cả PENDING)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (102,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha4-nv013' AND status = 'PENDING_REVIEW'), 13,
        10, '2026-04-10', NULL, 'PENDING_REVIEW', NULL, 'NV013 chờ HLV ngày mới', 'tl_hanlap01', 0, NOW(), NOW()),
       (102,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha4-nv014' AND status = 'PENDING_REVIEW'), 14,
        11, '2026-04-04', NULL, 'PENDING_REVIEW', NULL, 'NV014 chờ HLV', 'tl_hanlap01', 0, NOW(), NOW()),
       (102,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha4-nv015' AND status = 'PENDING_REVIEW'), 15,
        12, '2026-04-14', NULL, 'PENDING_REVIEW', NULL, 'NV015 chờ HLV ngày mới', 'tl_hanlap01', 0, NOW(), NOW()),
       (102,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ha4-nv016' AND status = 'PENDING_REVIEW'), 16,
        10, '2026-04-08', NULL, 'PENDING_REVIEW', NULL, 'NV016 chờ HLV', 'tl_hanlap01', 0, NOW(), NOW());

-- Result 103 (Lắp Bơm T4): 4 batch_ids → 4 result details (1 COMPLETED + 3 PENDING)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (103, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-lb4-nv017' AND status = 'COMPLETED'), 17,
        23,
        '2026-04-03', '2026-04-03', 'COMPLETED', TRUE, 'NV017 đạt T4', 'tl_laprap01', 0, NOW(), NOW()),
       (103,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-lb4-nv018' AND status = 'PENDING_REVIEW'), 18,
        24, '2026-04-11', NULL, 'PENDING_REVIEW', NULL, 'NV018 chờ HLV ngày mới', 'tl_laprap01', 0, NOW(), NOW()),
       (103,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-lb4-nv019' AND status = 'PENDING_REVIEW'), 19,
        25, '2026-04-07', NULL, 'PENDING_REVIEW', NULL, 'NV019 chờ HLV', 'tl_laprap01', 0, NOW(), NOW()),
-- NV020 batch: đã MISS 2 lần → liên kết dòng PENDING lần 3
       (103,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-lb4-nv020' AND status = 'PENDING_REVIEW'), 20,
        23, '2026-04-22', NULL, 'PENDING_REVIEW', NULL, 'NV020 chờ HLV lần 3', 'tl_laprap01', 0, NOW(), NOW());

-- Result 104 (Động Cơ T4): 5 batch_ids → 5 result details (2 COMPLETED + 3 PENDING)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (104, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-dc4-nv022' AND status = 'COMPLETED'), 22,
        18,
        '2026-04-03', '2026-04-03', 'COMPLETED', TRUE, 'NV022 đạt T4', 'tl_dongco01', 0, NOW(), NOW()),
       (104, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-dc4-nv023' AND status = 'COMPLETED'), 23,
        19,
        '2026-04-04', '2026-04-04', 'COMPLETED', TRUE, 'NV023 đạt T4', 'tl_dongco01', 0, NOW(), NOW()),
       (104,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-dc4-nv024' AND status = 'PENDING_REVIEW'), 24,
        20, '2026-04-14', NULL, 'PENDING_REVIEW', NULL, 'NV024 chờ HLV ngày mới', 'tl_dongco01', 0, NOW(), NOW()),
       (104,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-dc4-nv025' AND status = 'PENDING_REVIEW'), 25,
        21, '2026-04-08', NULL, 'PENDING_REVIEW', NULL, 'NV025 chờ HLV', 'tl_dongco01', 0, NOW(), NOW()),
-- NV026 batch: đã MISS 2 lần → liên kết dòng PENDING lần 3
       (104,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-dc4-nv026' AND status = 'PENDING_REVIEW'), 26,
        22, '2026-04-16', NULL, 'PENDING_REVIEW', NULL, 'NV026 chờ HLV lần 3', 'tl_dongco01', 0, NOW(), NOW());

-- Result 105 (KCS T4): 4 batch_ids → 4 result details (1 COMPLETED + 3 PENDING)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES (105, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-kcs4-nv027' AND status = 'COMPLETED'),
        27, 23,
        '2026-04-03', '2026-04-03', 'COMPLETED', TRUE, 'NV027 đạt T4', 'tl_kcs01', 0, NOW(), NOW()),
       (105,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-kcs4-nv028' AND status = 'PENDING_REVIEW'),
        28,
        24, '2026-04-11', NULL, 'PENDING_REVIEW', NULL, 'NV028 chờ HLV ngày mới', 'tl_kcs01', 0, NOW(), NOW()),
       (105,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-kcs4-nv029' AND status = 'PENDING_REVIEW'),
        29,
        25, '2026-04-07', NULL, 'PENDING_REVIEW', NULL, 'NV029 chờ HLV', 'tl_kcs01', 0, NOW(), NOW()),
       (105,
        (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-kcs4-nv030' AND status = 'PENDING_REVIEW'),
        30,
        23, '2026-04-08', NULL, 'PENDING_REVIEW', NULL, 'NV030 chờ HLV', 'tl_kcs01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 106: Tổ Tiện T4 │ 5 employees │ 7 rows (1 lên lịch lại)     ║
-- ║ team_id=1, line_id=1 — dây chuyền của chính tl_tien01             ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES (106, 'TP-TI-2026-004', 'Kế hoạch HLV T4/2026 - Tổ Tiện Ca Ngày', '2026-04-01', '2026-04-30', 1, 1, 'COMPLETED',
        1, 'Tổ Tiện T4 đã duyệt, đang thực hiện.', 1, 3, 'tl_tien01');

INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- NV001: đã hoàn thành → 1 dòng COMPLETED
(106, 1, 'pend-mock-ti4-nv001', '2026-04-01', '2026-04-02', '2026-04-02', 'COMPLETED', 'NV001 đã huấn luyện đạt',
 'tl_tien01', 0, NOW(), NOW()),
-- NV002: đã hoàn thành → 1 dòng COMPLETED
(106, 2, 'pend-mock-ti4-nv002', '2026-04-01', '2026-04-03', '2026-04-03', 'COMPLETED', 'NV002 đã huấn luyện đạt',
 'tl_tien01', 0, NOW(), NOW()),
-- NV003: ngày 04-07 MISS → lên lịch lại 04-14 PENDING
(106, 3, 'pend-mock-ti4-nv003', '2026-04-01', '2026-04-07', NULL, 'MISSED', 'NV003 vắng ngày 07/04', 'tl_tien01', 0,
 NOW(), NOW()),
(106, 3, 'pend-mock-ti4-nv003', '2026-04-01', '2026-04-14', NULL, 'PENDING_REVIEW', 'NV003 lên lịch lại 14/04',
 'tl_tien01', 0,
 NOW(), NOW()),
-- NV004: chưa đến ngày → 1 dòng PENDING
(106, 4, 'pend-mock-ti4-nv004', '2026-04-01', '2026-04-09', NULL, 'PENDING_REVIEW', 'NV004 chờ huấn luyện', 'tl_tien01',
 0,
 NOW(), NOW()),
-- NV006: chưa đến ngày → 1 dòng PENDING
(106, 6, 'pend-mock-ti4-nv006', '2026-04-01', '2026-04-10', NULL, 'PENDING_REVIEW', 'NV006 chờ huấn luyện', 'tl_tien01',
 0,
 NOW(), NOW());

-- Result 106 (Tiện T4)
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES (106, 106, 'Kết quả HLV T4/2026 – Tổ Tiện', 'TR-RES-TI-004', 2026, 1, 1, 'ONGOING', 1,
        'Đang nhập kết quả T4 Tiện.', 'tl_tien01');

-- Result 106 details: 5 batch_ids → mix status: COMPLETED / WAITING_SV / PENDING / NEED_SIGN / REJECTED_BY_SV
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES
-- NV001: đã test xong, đã duyệt
(106, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ti4-nv001' AND status = 'COMPLETED'), 1, 1,
 '2026-04-02', '2026-04-02', 'COMPLETED', TRUE, 'NV001 đạt T4 Tiện', 'tl_tien01', 0, NOW(), NOW()),
-- NV002: đã test xong, chờ SV ký
(106, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ti4-nv002' AND status = 'COMPLETED'), 2, 2,
 '2026-04-03', '2026-04-03', 'PENDING_REVIEW', TRUE, 'NV002 đã test, chờ SV ký duyệt', 'tl_tien01', 0, NOW(), NOW()),
-- NV003: chờ huấn luyện
(106, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ti4-nv003' AND status = 'PENDING_REVIEW'), 3, 3,
 '2026-04-14', NULL, 'PENDING_REVIEW', NULL, 'NV003 chờ HLV ngày mới', 'tl_tien01', 0, NOW(), NOW()),
-- NV004: đã test, cần TL ký (NEED_SIGN)
(106, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ti4-nv004' AND status = 'PENDING_REVIEW'), 4, 4,
 '2026-04-09', '2026-04-09', 'REVISING', TRUE, 'NV004 đã test, cần TL ký xác nhận', 'tl_tien01', 0, NOW(), NOW()),
-- NV006: SV từ chối, cần test lại
(106, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-ti4-nv006' AND status = 'PENDING_REVIEW'), 6, 5,
 '2026-04-10', '2026-04-10', 'REJECTED', FALSE, 'NV006 bị SV từ chối – kết quả không đạt', 'tl_tien01', 0, NOW(),
 NOW());


-- ============================================================================
-- 7. THÊM DÂY CHUYỀN MỚI CHO GROUP 1 (tl_tien01) + PLANS/RESULTS
-- ============================================================================

-- ── 7A. Thêm 3 product lines cho group 1 ────────────────────────────────────
INSERT INTO product_lines (id, code, name, group_id, created_by)
VALUES (6, 'PL-TIEN-S1', 'Dòng Máy Bơm Chìm S-Series (Tiện)', 1, 'admin'),
       (7, 'PL-TIEN-V1', 'Dòng Van Công Nghiệp V-Series (Tiện)', 1, 'admin'),
       (8, 'PL-TIEN-C1', 'Dòng Trục Khuỷu C-Series (Tiện)', 1, 'admin');

-- ── 7B. Processes cho 3 lines mới ───────────────────────────────────────────
INSERT INTO processes (id, code, name, description, classification, standard_time_jt, product_line_id, created_by)
VALUES
-- Line 6: Bơm Chìm S-Series (3 processes)
(22, 'TI-S01', 'Tiện Trục Bơm Chìm', 'Tiện thô + tinh trục bơm chìm inox 304', 1, 55.00, 6, 'admin'),
(23, 'TI-S02', 'Tiện Vỏ Bơm Chìm', 'Tiện vỏ bơm chìm, dung sai ±0.02mm', 1, 70.00, 6, 'admin'),
(24, 'TI-S03', 'Tiện Cánh Bơm Chìm', 'Tiện cánh bơm đúc, cân bằng ≤2g·mm', 2, 40.00, 6, 'admin'),
-- Line 7: Van V-Series (3 processes)
(25, 'TI-V01', 'Tiện Thân Van', 'Tiện thân van cổng DN50-DN200, IT7', 1, 65.00, 7, 'admin'),
(26, 'TI-V02', 'Tiện Đế Van & Mặt Tựa', 'Tiện mặt tựa van, Ra ≤ 0.8', 1, 80.00, 7, 'admin'),
(27, 'TI-V03', 'Tiện Trục Van & Ren', 'Tiện trục van + ren thang Tr28x5', 2, 45.00, 7, 'admin'),
-- Line 8: Trục Khuỷu C-Series (3 processes)
(28, 'TI-C01', 'Tiện Thô Trục Khuỷu', 'Tiện thô cổ trục chính + cổ biên', 1, 90.00, 8, 'admin'),
(29, 'TI-C02', 'Tiện Tinh Trục Khuỷu', 'Tiện tinh cổ trục, IT6, Ra ≤ 0.8', 1, 120.00, 8, 'admin'),
(30, 'TI-C03', 'Mài & Đánh Bóng Trục Khuỷu', 'Mài cổ trục, superfinish Ra ≤ 0.2', 2, 60.00, 8, 'admin');

-- ── 7C. Products cho 3 lines mới ────────────────────────────────────────────
INSERT INTO products (id, code, name, description, created_by)
VALUES (21, 'BOM-S100', 'Bơm Chìm S100', 'Bơm chìm 1HP, ngập nước 100%, inox 304', 'admin'),
       (22, 'BOM-S200', 'Bơm Chìm S200', 'Bơm chìm 2HP, cột áp 25m', 'admin'),
       (23, 'VAN-V50', 'Van Cổng V50', 'Van cổng DN50, PN16, thân gang', 'admin'),
       (24, 'VAN-V100', 'Van Cổng V100', 'Van cổng DN100, PN25, thân thép đúc', 'admin'),
       (25, 'CRK-C4', 'Trục Khuỷu 4 Xilanh C4', 'Trục khuỷu forged steel, 4 cổ biên', 'admin'),
       (26, 'CRK-C6', 'Trục Khuỷu 6 Xilanh C6', 'Trục khuỷu forged steel, 6 cổ biên', 'admin');

-- ── 7D. Product ↔ Process mapping ───────────────────────────────────────────
INSERT INTO product_process (product_id, process_id, standard_time_jt, created_by)
VALUES
-- Bơm Chìm S-Series
(21, 22, 55.0, 'admin'),
(21, 23, 70.0, 'admin'),
(21, 24, 40.0, 'admin'),
(22, 22, 58.0, 'admin'),
(22, 23, 75.0, 'admin'),
(22, 24, 42.0, 'admin'),
-- Van V-Series
(23, 25, 65.0, 'admin'),
(23, 26, 80.0, 'admin'),
(23, 27, 45.0, 'admin'),
(24, 25, 70.0, 'admin'),
(24, 26, 85.0, 'admin'),
(24, 27, 48.0, 'admin'),
-- Trục Khuỷu C-Series
(25, 28, 90.0, 'admin'),
(25, 29, 120.0, 'admin'),
(25, 30, 60.0, 'admin'),
(26, 28, 95.0, 'admin'),
(26, 29, 130.0, 'admin'),
(26, 30, 65.0, 'admin');

-- ── 7E. Defects cho 3 lines mới ─────────────────────────────────────────────
INSERT INTO defects (defect_code, defect_description, process_id, detected_date,
                     defect_type, origin_measures, outflow_measures, origin_cause,
                     outflow_cause, cause_point, customer, quantity, conclusion, note, created_by)
VALUES
-- Bơm Chìm
('DF041', 'Xước bề mặt trục bơm chìm inox', 22, '2025-05-10', 'DEFECTIVE_GOODS', 'Thay dao CBN mới', 'Soi đèn 100%',
 'Dao mòn cắt inox', 'Bỏ qua soi đèn', 'Đài tiện', 'Nội bộ', 4, 'Lịch thay dao inox riêng', NULL, 'system'),
('DF042', 'Vỏ bơm chìm lệch tâm >0.03mm', 23, '2025-06-05', 'CLAIM', 'Kiểm đồ gá kẹp', 'Đo CMM trước lắp', 'Đồ gá mòn',
 'Không đo CMM', 'Máy tiện CNC', 'KH Grundfos', 2, 'Cân chỉnh đồ gá hàng tuần', NULL, 'system'),
('DF043', 'Cánh bơm mất cân bằng >5g·mm', 24, '2025-07-12', 'DEFECTIVE_GOODS', 'Cân bằng lại trên máy',
 'Kiểm cân bằng 100%', 'Đúc không đều', 'Bỏ qua kiểm', 'Trạm cân bằng', 'Nội bộ', 6, 'Kiểm cân bằng mỗi chi tiết', NULL,
 'system'),
-- Van V-Series
('DF044', 'Mặt tựa van bị rỗ micro', 25, '2025-04-18', 'CLAIM', 'Tiện lại mặt tựa', 'Kiểm rò rỉ 100%',
 'Phôi đúc có bọt khí', 'Không kiểm phôi', 'Mặt tựa van', 'KH Kitz', 3, 'Kiểm phôi trước gia công', NULL, 'system'),
('DF045', 'Ren trục van Tr28x5 bị sờn', 26, '2025-05-22', 'DEFECTIVE_GOODS', 'Thay dao tiện ren', 'Kiểm caliper ren',
 'Dao ren mòn', 'Bỏ qua kiểm ren', 'Trạm tiện ren', 'Nội bộ', 5, 'Thay dao ren mỗi 100 chi tiết', NULL, 'system'),
('DF046', 'Đế van không phẳng >0.05mm', 26, '2025-06-10', 'DEFECTIVE_GOODS', 'Kiểm phẳng đồng hồ so',
 'Đo 100% trước lắp', 'Kẹp lực quá mạnh', 'Không đo giữa ca', 'Đồ gá kẹp', 'Nội bộ', 8,
 'SOP kiểm phẳng mỗi 10 chi tiết', NULL, 'system'),
-- Trục Khuỷu
('DF047', 'Cổ trục chính oval >0.01mm', 28, '2025-03-15', 'CLAIM', 'Tiện lại cổ trục', 'Đo 3 điểm panme',
 'Kẹp phôi bị lệch tâm', 'Đo 1 điểm thay vì 3', 'Mâm cặp', 'KH Toyota', 1, 'Đo oval 3 điểm bắt buộc', NULL, 'system'),
('DF048', 'Độ nhám cổ biên Ra >1.0 sau tiện', 29, '2025-04-20', 'DEFECTIVE_GOODS', 'Mài superfinish lại',
 'Đo nhám 100%', 'Bỏ qua superfinish', 'Đo nhám cuối ca', 'Trạm mài', 'Nội bộ', 3, 'Superfinish bắt buộc mỗi chi tiết',
 NULL, 'system'),
('DF049', 'Vết cháy mài trên cổ trục', 30, '2025-05-08', 'DEFECTIVE_GOODS', 'Giảm feed mài', 'Kiểm Barkhausen 100%',
 'Feed quá nhanh gây cháy', 'Không kiểm Barkhausen', 'Máy mài CNC', 'Nội bộ', 2, 'Thêm kiểm Barkhausen vào SOP', NULL,
 'system');

-- ── 7F. Training Samples cho 3 lines mới ────────────────────────────────────
INSERT INTO training_samples (id, process_id, product_line_id, defect_id, category_name,
                              training_description, product_id, training_sample_code, training_code,
                              process_order, category_order, content_order, note, created_by)
VALUES
-- Bơm Chìm (line 6, processes 22-24)
(31, 22, 6, (SELECT id FROM defects WHERE defect_code = 'DF041'), 'Lỗi Ngoại Quan - Xước Inox',
 'Soi đèn UV + lúp 10x phát hiện xước trục bơm chìm inox 304. Vết >0.05mm loại bỏ.',
 21, 'M-SC-01', 'TS0031', 1, 1, 1, 'Inox khó phát hiện xước', 'system'),
(32, 23, 6, (SELECT id FROM defects WHERE defect_code = 'DF042'), 'Lỗi Kích Thước - Đồng Tâm',
 'Đo đồng tâm vỏ bơm bằng đồng hồ so. Sai lệch cho phép ≤0.03mm.',
 21, 'M-SC-02', 'TS0032', 1, 2, 1, 'Đo trước khi chuyển lắp', 'system'),
(33, 24, 6, (SELECT id FROM defects WHERE defect_code = 'DF043'), 'Lỗi Cân Bằng - Cánh Bơm',
 'Đặt cánh bơm vào máy cân bằng động. Yêu cầu ≤2g·mm.',
 22, 'M-SC-03', 'TS0033', 2, 1, 1, 'Ghi nhận giá trị vào log', 'system'),
-- Van V-Series (line 7, processes 25-27)
(34, 25, 7, (SELECT id FROM defects WHERE defect_code = 'DF044'), 'Lỗi Bề Mặt - Rỗ Micro',
 'Kiểm mặt tựa van bằng kính lúp 20x. Không cho phép rỗ ≥0.1mm.',
 23, 'M-VN-01', 'TS0034', 1, 1, 1, 'Rỗ micro gây rò rỉ', 'system'),
(35, 26, 7, (SELECT id FROM defects WHERE defect_code = 'DF045'), 'Lỗi Ren - Ren Thang Tr28',
 'Dùng dưỡng ren GO/NO-GO kiểm ren Tr28x5. Kiểm profile bằng projector.',
 23, 'M-VN-02', 'TS0035', 1, 2, 1, 'Ren thang cần projector', 'system'),
(36, 27, 7, (SELECT id FROM defects WHERE defect_code = 'DF046'), 'Lỗi Phẳng - Đế Van',
 'Kiểm độ phẳng đế van trên bàn đá granite. Cho phép ≤0.05mm.',
 24, 'M-VN-03', 'TS0036', 2, 1, 1, 'Đo 5 điểm (4 góc + tâm)', 'system'),
-- Trục Khuỷu C-Series (line 8, processes 28-30)
(37, 28, 8, (SELECT id FROM defects WHERE defect_code = 'DF047'), 'Lỗi Kích Thước - Oval Cổ Trục',
 'Đo 3 điểm (0°/120°/240°) bằng panme điện tử. Oval cho phép ≤0.01mm.',
 25, 'M-CK-01', 'TS0037', 1, 1, 1, 'Đo tại 3 vị trí dọc trục', 'system'),
(38, 29, 8, (SELECT id FROM defects WHERE defect_code = 'DF048'), 'Lỗi Nhám - Cổ Biên',
 'Đo nhám Mitutoyo SJ-210 tại 3 vị trí. Ra ≤ 0.4 sau mài.',
 25, 'M-CK-02', 'TS0038', 1, 2, 1, 'Đo sau superfinish', 'system'),
(39, 30, 8, (SELECT id FROM defects WHERE defect_code = 'DF049'), 'Lỗi Nhiệt - Cháy Mài',
 'Kiểm Barkhausen noise analyzer. Vùng cháy hiển thị giá trị >100.',
 26, 'M-CK-03', 'TS0039', 2, 1, 1, 'Cháy mài gây nứt mỏi', 'system');

-- ── 7G. Employee Skills cho team 1 (NV001-004, NV006) trên processes mới ────
INSERT INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by, delete_flag,
                             created_at, updated_at)
VALUES
-- Process 22 (Tiện Trục Bơm Chìm)
(1, 22, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(2, 22, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(3, 22, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 10 DAY), 'system_mock', 0, NOW(), NOW()),
(4, 22, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(6, 22, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Process 23 (Tiện Vỏ Bơm Chìm)
(1, 23, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(2, 23, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 7 DAY), 'system_mock', 0, NOW(), NOW()),
(3, 23, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(4, 23, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(6, 23, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Process 24 (Tiện Cánh Bơm Chìm)
(1, 24, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(2, 24, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(3, 24, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(4, 24, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 15 DAY), 'system_mock', 0, NOW(), NOW()),
(6, 24, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
-- Process 25 (Tiện Thân Van)
(1, 25, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(2, 25, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(3, 25, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(4, 25, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(6, 25, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 12 DAY), 'system_mock', 0, NOW(), NOW()),
-- Process 26 (Tiện Đế Van)
(1, 26, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 5 DAY), 'system_mock', 0, NOW(), NOW()),
(2, 26, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(3, 26, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(4, 26, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(6, 26, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Process 27 (Tiện Trục Van)
(1, 27, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(2, 27, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(3, 27, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(4, 27, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 20 DAY), 'system_mock', 0, NOW(), NOW()),
(6, 27, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Process 28 (Tiện Thô Trục Khuỷu)
(1, 28, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(2, 28, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(3, 28, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 8 DAY), 'system_mock', 0, NOW(), NOW()),
(4, 28, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(6, 28, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
-- Process 29 (Tiện Tinh Trục Khuỷu)
(1, 29, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(2, 29, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 18 DAY), 'system_mock', 0, NOW(), NOW()),
(3, 29, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(4, 29, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(6, 29, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Process 30 (Mài Trục Khuỷu)
(1, 30, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(2, 30, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(3, 30, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(4, 30, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(6, 30, 'PENDING_REVIEW', '2025-04-01', DATE_ADD(@today, INTERVAL 3 DAY), 'system_mock', 0, NOW(), NOW());


-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 107: Tổ Tiện – Dòng Bơm Chìm T3 │ team=1, line=6            ║
-- ║ 5 employees │ 8 rows (2 lên lịch lại)                             ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES (107, 'TP-TI-2026-S01', 'Kế hoạch HLV T3/2026 - Dòng Bơm Chìm S-Series', '2026-03-01', '2026-03-31', 1, 6,
        'COMPLETED', 1, 'T3 Bơm Chìm đã duyệt.', 1, 3, 'tl_tien01');

INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- NV001: COMPLETED
(107, 1, 'pend-mock-s3-nv001', '2026-03-01', '2026-03-05', '2026-03-05', 'COMPLETED', 'NV001 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW()),
-- NV002: MISS → PENDING
(107, 2, 'pend-mock-s3-nv002', '2026-03-01', '2026-03-06', NULL, 'MISSED', 'NV002 vắng ngày 06/03', 'tl_tien01', 0,
 NOW(),
 NOW()),
(107, 2, 'pend-mock-s3-nv002', '2026-03-01', '2026-03-13', NULL, 'PENDING_REVIEW', 'NV002 lên lịch lại 13/03',
 'tl_tien01', 0,
 NOW(), NOW()),
-- NV003: COMPLETED
(107, 3, 'pend-mock-s3-nv003', '2026-03-01', '2026-03-07', '2026-03-07', 'COMPLETED', 'NV003 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW()),
-- NV004: MISS → MISS → PENDING (3 dòng)
(107, 4, 'pend-mock-s3-nv004', '2026-03-01', '2026-03-10', NULL, 'MISSED', 'NV004 vắng lần 1', 'tl_tien01', 0, NOW(),
 NOW()),
(107, 4, 'pend-mock-s3-nv004', '2026-03-01', '2026-03-17', NULL, 'MISSED', 'NV004 vắng lần 2', 'tl_tien01', 0, NOW(),
 NOW()),
(107, 4, 'pend-mock-s3-nv004', '2026-03-01', '2026-03-24', NULL, 'PENDING_REVIEW', 'NV004 lên lịch lần 3', 'tl_tien01',
 0,
 NOW(), NOW()),
-- NV006: PENDING
(107, 6, 'pend-mock-s3-nv006', '2026-03-01', '2026-03-12', NULL, 'PENDING_REVIEW', 'NV006 chờ huấn luyện', 'tl_tien01',
 0,
 NOW(), NOW());

-- Result 107 (Bơm Chìm T3)
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES (107, 107, 'Kết quả HLV T3/2026 – Dòng Bơm Chìm', 'TR-RES-S-003', 2026, 1, 6, 'PENDING_APPROVAL', 1,
        'Chờ MG duyệt T3 Bơm Chìm.', 'tl_tien01');

INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES
-- NV001: đã duyệt
(107, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-s3-nv001' AND status = 'COMPLETED'), 1, 31,
 '2026-03-05', '2026-03-05', 'COMPLETED', TRUE, 'NV001 đạt Bơm Chìm', 'tl_tien01', 0, NOW(), NOW()),
-- NV002: chờ huấn luyện
(107, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-s3-nv002' AND status = 'PENDING_REVIEW'), 2, 32,
 '2026-03-13', NULL, 'PENDING_REVIEW', NULL, 'NV002 chờ HLV ngày mới', 'tl_tien01', 0, NOW(), NOW()),
-- NV003: đã test, chờ SV ký
(107, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-s3-nv003' AND status = 'COMPLETED'), 3, 33,
 '2026-03-07', '2026-03-07', 'PENDING_REVIEW', TRUE, 'NV003 chờ SV ký duyệt', 'tl_tien01', 0, NOW(), NOW()),
-- NV004: đã test xong (COMPLETED) chưa ký
(107, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-s3-nv004' AND status = 'PENDING_REVIEW'), 4, 31,
 '2026-03-24', '2026-03-24', 'COMPLETED', TRUE, 'NV004 đã test xong, chưa ký', 'tl_tien01', 0, NOW(), NOW()),
-- NV006: SV từ chối
(107, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-s3-nv006' AND status = 'PENDING_REVIEW'), 6, 32,
 '2026-03-12', '2026-03-12', 'REJECTED', FALSE, 'NV006 bị SV từ chối – cần test lại', 'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 108: Tổ Tiện – Dòng Van V-Series T4 │ team=1, line=7         ║
-- ║ 5 employees │ 7 rows (1 lên lịch lại)                             ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES (108, 'TP-TI-2026-V01', 'Kế hoạch HLV T4/2026 - Dòng Van V-Series', '2026-04-01', '2026-04-30', 1, 7,
        'COMPLETED', 1, 'T4 Van đã duyệt, đang thực hiện.', 1, 3, 'tl_tien01');

INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- NV001: PENDING
(108, 1, 'pend-mock-v4-nv001', '2026-04-01', '2026-04-05', NULL, 'PENDING_REVIEW', 'NV001 chờ huấn luyện Van',
 'tl_tien01', 0,
 NOW(), NOW()),
-- NV002: COMPLETED
(108, 2, 'pend-mock-v4-nv002', '2026-04-01', '2026-04-03', '2026-04-03', 'COMPLETED', 'NV002 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW()),
-- NV003: COMPLETED
(108, 3, 'pend-mock-v4-nv003', '2026-04-01', '2026-04-04', '2026-04-04', 'COMPLETED', 'NV003 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW()),
-- NV004: MISS → PENDING
(108, 4, 'pend-mock-v4-nv004', '2026-04-01', '2026-04-07', NULL, 'MISSED', 'NV004 vắng ngày 07/04', 'tl_tien01', 0,
 NOW(),
 NOW()),
(108, 4, 'pend-mock-v4-nv004', '2026-04-01', '2026-04-14', NULL, 'PENDING_REVIEW', 'NV004 lên lịch lại 14/04',
 'tl_tien01', 0,
 NOW(), NOW()),
-- NV006: COMPLETED
(108, 6, 'pend-mock-v4-nv006', '2026-04-01', '2026-04-06', '2026-04-06', 'COMPLETED', 'NV006 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW());

-- Result 108 (Van V-Series T4)
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES (108, 108, 'Kết quả HLV T4/2026 – Dòng Van V-Series', 'TR-RES-V-004', 2026, 1, 7, 'ONGOING', 1,
        'Đang nhập kết quả T4 Van.', 'tl_tien01');

INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES
-- NV001: chờ huấn luyện
(108, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-v4-nv001' AND status = 'PENDING_REVIEW'), 1, 34,
 '2026-04-05', NULL, 'PENDING_REVIEW', NULL, 'NV001 chờ HLV Van', 'tl_tien01', 0, NOW(), NOW()),
-- NV002: đã test, cần TL ký
(108, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-v4-nv002' AND status = 'COMPLETED'), 2, 35,
 '2026-04-03', '2026-04-03', 'REVISING', TRUE, 'NV002 đã test Van, cần TL ký', 'tl_tien01', 0, NOW(), NOW()),
-- NV003: SV từ chối
(108, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-v4-nv003' AND status = 'COMPLETED'), 3, 36,
 '2026-04-04', '2026-04-04', 'REJECTED', FALSE, 'NV003 bị SV từ chối Van T4', 'tl_tien01', 0, NOW(), NOW()),
-- NV004: chờ huấn luyện
(108, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-v4-nv004' AND status = 'PENDING_REVIEW'), 4, 34,
 '2026-04-14', NULL, 'PENDING_REVIEW', NULL, 'NV004 chờ HLV ngày mới', 'tl_tien01', 0, NOW(), NOW()),
-- NV006: chờ SV ký
(108, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-v4-nv006' AND status = 'COMPLETED'), 6, 35,
 '2026-04-06', '2026-04-06', 'PENDING_REVIEW', TRUE, 'NV006 chờ SV ký Van T4', 'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Plan 109: Tổ Tiện – Dòng Trục Khuỷu T2 │ team=1, line=8          ║
-- ║ 5 employees │ 6 rows (1 lên lịch lại)                             ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES (109, 'TP-TI-2026-C01', 'Kế hoạch HLV T2/2026 - Dòng Trục Khuỷu C-Series', '2026-02-01', '2026-02-28', 1, 8,
        'COMPLETED', 1, 'T2 Trục Khuỷu đã duyệt.', 1, 3, 'tl_tien01');

INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- NV001: COMPLETED
(109, 1, 'pend-mock-c2-nv001', '2026-02-01', '2026-02-04', '2026-02-04', 'COMPLETED', 'NV001 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW()),
-- NV002: COMPLETED
(109, 2, 'pend-mock-c2-nv002', '2026-02-01', '2026-02-05', '2026-02-05', 'COMPLETED', 'NV002 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW()),
-- NV003: COMPLETED
(109, 3, 'pend-mock-c2-nv003', '2026-02-01', '2026-02-06', '2026-02-06', 'COMPLETED', 'NV003 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW()),
-- NV004: MISS → PENDING
(109, 4, 'pend-mock-c2-nv004', '2026-02-01', '2026-02-10', NULL, 'MISSED', 'NV004 vắng ngày 10/02', 'tl_tien01', 0,
 NOW(),
 NOW()),
(109, 4, 'pend-mock-c2-nv004', '2026-02-01', '2026-02-17', '2026-02-17', 'COMPLETED', 'NV004 đã hoàn thành lần 2',
 'tl_tien01', 0, NOW(), NOW()),
-- NV006: COMPLETED
(109, 6, 'pend-mock-c2-nv006', '2026-02-01', '2026-02-07', '2026-02-07', 'COMPLETED', 'NV006 đã huấn luyện đạt',
 'tl_tien01',
 0, NOW(), NOW());

-- Result 109 (Trục Khuỷu T2) — Đã hoàn tất
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES (109, 109, 'Kết quả HLV T2/2026 – Dòng Trục Khuỷu', 'TR-RES-C-002', 2026, 1, 8, 'COMPLETED', 1,
        'T2 Trục Khuỷu đã duyệt hoàn tất.', 'tl_tien01');

INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id,
                                     planned_date, actual_date, status, is_pass, note, created_by, delete_flag,
                                     created_at, updated_at)
VALUES
-- NV001: đã duyệt
(109, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-c2-nv001' AND status = 'COMPLETED'), 1, 37,
 '2026-02-04', '2026-02-04', 'COMPLETED', TRUE, 'NV001 đạt Trục Khuỷu', 'tl_tien01', 0, NOW(), NOW()),
-- NV002: đã duyệt
(109, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-c2-nv002' AND status = 'COMPLETED'), 2, 38,
 '2026-02-05', '2026-02-05', 'COMPLETED', TRUE, 'NV002 đạt Trục Khuỷu', 'tl_tien01', 0, NOW(), NOW()),
-- NV003: đã duyệt
(109, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-c2-nv003' AND status = 'COMPLETED'), 3, 39,
 '2026-02-06', '2026-02-06', 'COMPLETED', TRUE, 'NV003 đạt Trục Khuỷu', 'tl_tien01', 0, NOW(), NOW()),
-- NV004: trượt
(109, (SELECT id
       FROM training_plan_details
       WHERE batch_id = 'pend-mock-c2-nv004'
         AND training_plan_id = 109
         AND status = 'COMPLETED'), 4, 37, '2026-02-17',
 '2026-02-17', 'COMPLETED', FALSE, 'NV004 trượt Trục Khuỷu', 'tl_tien01', 0, NOW(), NOW()),
-- NV006: đã duyệt
(109, (SELECT id FROM training_plan_details WHERE batch_id = 'pend-mock-c2-nv006' AND status = 'COMPLETED'), 6, 38,
 '2026-02-07', '2026-02-07', 'COMPLETED', TRUE, 'NV006 đạt Trục Khuỷu', 'tl_tien01', 0, NOW(), NOW());


-- Bật lại foreign key check
SET FOREIGN_KEY_CHECKS = 1;
