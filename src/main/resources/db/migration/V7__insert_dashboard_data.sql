-- ============================================================================
-- V7__insert_dashboard_data.sql

-- Thêm dữ liệu giả lập cho màn hình Dashboard cực kỳ lớn (hơn 50 record/bảng)
-- Viết tường minh (explicit INSERT) theo yêu cầu!
-- ============================================================================

-- Bỏ qua foreign key check để insert dễ
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Xoá mọi dữ liệu mock sinh ra từ trước (Dựa trên system/note đã quy ước)
DELETE FROM training_plan_details WHERE batch_id LIKE 'dashboard-mock%';
DELETE FROM training_result_details WHERE note LIKE '%Dashboard mock%';
DELETE FROM employee_skills WHERE created_by = 'system_mock';
DELETE FROM defects WHERE note LIKE 'Dashboard mock defect%';

-- 2. Định nghĩa biến thời gian tương đối
SET @today = CURRENT_DATE;
SET @yesterday = DATE_SUB(@today, INTERVAL 1 DAY);

-- ============================================================================
-- 3. TRAINING TASKS (Hơn 50 lượt thực thi Huấn Luyện)
-- ============================================================================

-- A. 12 Huấn Luyện Hôm Nay (PENDING)
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date, status, note, created_by, delete_flag, created_at, updated_at)
VALUES 
(3, 1, 'dashboard-mock-1', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-2', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-3', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-4', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-5', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-6', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 1, 'dashboard-mock-7', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-8', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-9', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-10', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-11', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-12', DATE_FORMAT(@today, '%Y-%m-01'), @today, NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW());

-- B. 15 Bị lỡ hẹn (PENDING - Quá khứ gần: 1-5 ngày trước)
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date, status, note, created_by, delete_flag, created_at, updated_at)
VALUES 
(3, 1, 'dashboard-mock-miss-1', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 1 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-miss-2', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 1 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-miss-3', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-miss-4', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-miss-5', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-miss-6', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 3 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 1, 'dashboard-mock-miss-7', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 3 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-miss-8', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 4 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-miss-9', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 4 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-miss-10', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 4 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-miss-11', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-miss-12', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 1, 'dashboard-mock-miss-13', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-miss-14', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 6 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-miss-15', DATE_FORMAT(@today, '%Y-%m-01'), DATE_SUB(@today, INTERVAL 6 DAY), NULL, 'PENDING', 'Dashboard mock', 'system', 0, NOW(), NOW());

-- C. 30 Lịch sử đã huấn luyện (DONE - rải rác 30 ngày qua)
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date, status, note, created_by, delete_flag, created_at, updated_at)
VALUES 
(3, 1, 'dashboard-mock-hist-1', DATE_FORMAT(DATE_SUB(@today, INTERVAL 1 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-hist-2', DATE_FORMAT(DATE_SUB(@today, INTERVAL 1 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-hist-3', DATE_FORMAT(DATE_SUB(@today, INTERVAL 2 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-hist-4', DATE_FORMAT(DATE_SUB(@today, INTERVAL 2 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-hist-5', DATE_FORMAT(DATE_SUB(@today, INTERVAL 3 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 3 DAY), DATE_SUB(@today, INTERVAL 3 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-hist-6', DATE_FORMAT(DATE_SUB(@today, INTERVAL 4 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 4 DAY), DATE_SUB(@today, INTERVAL 4 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 1, 'dashboard-mock-hist-7', DATE_FORMAT(DATE_SUB(@today, INTERVAL 5 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-hist-8', DATE_FORMAT(DATE_SUB(@today, INTERVAL 5 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-hist-9', DATE_FORMAT(DATE_SUB(@today, INTERVAL 6 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 6 DAY), DATE_SUB(@today, INTERVAL 6 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-hist-10', DATE_FORMAT(DATE_SUB(@today, INTERVAL 7 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 7 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-hist-11', DATE_FORMAT(DATE_SUB(@today, INTERVAL 8 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 8 DAY), DATE_SUB(@today, INTERVAL 8 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-hist-12', DATE_FORMAT(DATE_SUB(@today, INTERVAL 9 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 9 DAY), DATE_SUB(@today, INTERVAL 9 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 1, 'dashboard-mock-hist-13', DATE_FORMAT(DATE_SUB(@today, INTERVAL 10 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 10 DAY), DATE_SUB(@today, INTERVAL 10 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-hist-14', DATE_FORMAT(DATE_SUB(@today, INTERVAL 11 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 11 DAY), DATE_SUB(@today, INTERVAL 11 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-hist-15', DATE_FORMAT(DATE_SUB(@today, INTERVAL 12 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 12 DAY), DATE_SUB(@today, INTERVAL 12 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-hist-16', DATE_FORMAT(DATE_SUB(@today, INTERVAL 13 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 13 DAY), DATE_SUB(@today, INTERVAL 13 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-hist-17', DATE_FORMAT(DATE_SUB(@today, INTERVAL 14 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 14 DAY), DATE_SUB(@today, INTERVAL 14 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-hist-18', DATE_FORMAT(DATE_SUB(@today, INTERVAL 15 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 15 DAY), DATE_SUB(@today, INTERVAL 15 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 1, 'dashboard-mock-hist-19', DATE_FORMAT(DATE_SUB(@today, INTERVAL 16 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 16 DAY), DATE_SUB(@today, INTERVAL 16 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-hist-20', DATE_FORMAT(DATE_SUB(@today, INTERVAL 17 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 17 DAY), DATE_SUB(@today, INTERVAL 17 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-hist-21', DATE_FORMAT(DATE_SUB(@today, INTERVAL 18 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 18 DAY), DATE_SUB(@today, INTERVAL 18 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-hist-22', DATE_FORMAT(DATE_SUB(@today, INTERVAL 19 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 19 DAY), DATE_SUB(@today, INTERVAL 19 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-hist-23', DATE_FORMAT(DATE_SUB(@today, INTERVAL 20 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 20 DAY), DATE_SUB(@today, INTERVAL 20 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-hist-24', DATE_FORMAT(DATE_SUB(@today, INTERVAL 21 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 21 DAY), DATE_SUB(@today, INTERVAL 21 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 1, 'dashboard-mock-hist-25', DATE_FORMAT(DATE_SUB(@today, INTERVAL 22 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 22 DAY), DATE_SUB(@today, INTERVAL 22 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 2, 'dashboard-mock-hist-26', DATE_FORMAT(DATE_SUB(@today, INTERVAL 24 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 24 DAY), DATE_SUB(@today, INTERVAL 24 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 3, 'dashboard-mock-hist-27', DATE_FORMAT(DATE_SUB(@today, INTERVAL 26 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 26 DAY), DATE_SUB(@today, INTERVAL 26 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 4, 'dashboard-mock-hist-28', DATE_FORMAT(DATE_SUB(@today, INTERVAL 27 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 27 DAY), DATE_SUB(@today, INTERVAL 27 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 5, 'dashboard-mock-hist-29', DATE_FORMAT(DATE_SUB(@today, INTERVAL 29 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 29 DAY), DATE_SUB(@today, INTERVAL 29 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW()),
(3, 6, 'dashboard-mock-hist-30', DATE_FORMAT(DATE_SUB(@today, INTERVAL 30 DAY), '%Y-%m-01'), DATE_SUB(@today, INTERVAL 30 DAY), DATE_SUB(@today, INTERVAL 30 DAY), 'DONE', 'Dashboard mock', 'system', 0, NOW(), NOW());


-- D. 30 Kết quả (Result Details) cho 30 task lịch sử ở C.
-- 6 Task trượt (Fail = FALSE) hôm qua / vài hôm trước, 24 task kia Đỗ (Pass = TRUE)
INSERT INTO training_result_details (training_result_id, training_plan_detail_id, employee_id, training_sample_id, planned_date, actual_date, status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES 
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-1'), 1, 1, DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'APPROVED', FALSE, 'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-2'), 2, 2, DATE_SUB(@today, INTERVAL 1 DAY), DATE_SUB(@today, INTERVAL 1 DAY), 'APPROVED', FALSE, 'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-3'), 3, 3, DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'APPROVED', FALSE, 'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-4'), 4, 4, DATE_SUB(@today, INTERVAL 2 DAY), DATE_SUB(@today, INTERVAL 2 DAY), 'APPROVED', FALSE, 'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-5'), 5, 5, DATE_SUB(@today, INTERVAL 3 DAY), DATE_SUB(@today, INTERVAL 3 DAY), 'APPROVED', FALSE, 'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-6'), 6, 6, DATE_SUB(@today, INTERVAL 4 DAY), DATE_SUB(@today, INTERVAL 4 DAY), 'APPROVED', FALSE, 'Dashboard mock fail result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-7'), 1, 1, DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-8'), 2, 2, DATE_SUB(@today, INTERVAL 5 DAY), DATE_SUB(@today, INTERVAL 5 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-9'), 3, 3, DATE_SUB(@today, INTERVAL 6 DAY), DATE_SUB(@today, INTERVAL 6 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-10'), 4, 4, DATE_SUB(@today, INTERVAL 7 DAY), DATE_SUB(@today, INTERVAL 7 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-11'), 5, 5, DATE_SUB(@today, INTERVAL 8 DAY), DATE_SUB(@today, INTERVAL 8 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-12'), 6, 6, DATE_SUB(@today, INTERVAL 9 DAY), DATE_SUB(@today, INTERVAL 9 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-13'), 1, 1, DATE_SUB(@today, INTERVAL 10 DAY), DATE_SUB(@today, INTERVAL 10 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-14'), 2, 2, DATE_SUB(@today, INTERVAL 11 DAY), DATE_SUB(@today, INTERVAL 11 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-15'), 3, 3, DATE_SUB(@today, INTERVAL 12 DAY), DATE_SUB(@today, INTERVAL 12 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-16'), 4, 4, DATE_SUB(@today, INTERVAL 13 DAY), DATE_SUB(@today, INTERVAL 13 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-17'), 5, 5, DATE_SUB(@today, INTERVAL 14 DAY), DATE_SUB(@today, INTERVAL 14 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-18'), 6, 6, DATE_SUB(@today, INTERVAL 15 DAY), DATE_SUB(@today, INTERVAL 15 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-19'), 1, 1, DATE_SUB(@today, INTERVAL 16 DAY), DATE_SUB(@today, INTERVAL 16 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-20'), 2, 2, DATE_SUB(@today, INTERVAL 17 DAY), DATE_SUB(@today, INTERVAL 17 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-21'), 3, 3, DATE_SUB(@today, INTERVAL 18 DAY), DATE_SUB(@today, INTERVAL 18 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-22'), 4, 4, DATE_SUB(@today, INTERVAL 19 DAY), DATE_SUB(@today, INTERVAL 19 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-23'), 5, 5, DATE_SUB(@today, INTERVAL 20 DAY), DATE_SUB(@today, INTERVAL 20 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-24'), 6, 6, DATE_SUB(@today, INTERVAL 21 DAY), DATE_SUB(@today, INTERVAL 21 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-25'), 1, 1, DATE_SUB(@today, INTERVAL 22 DAY), DATE_SUB(@today, INTERVAL 22 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-26'), 2, 2, DATE_SUB(@today, INTERVAL 24 DAY), DATE_SUB(@today, INTERVAL 24 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-27'), 3, 3, DATE_SUB(@today, INTERVAL 26 DAY), DATE_SUB(@today, INTERVAL 26 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-28'), 4, 4, DATE_SUB(@today, INTERVAL 27 DAY), DATE_SUB(@today, INTERVAL 27 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-29'), 5, 5, DATE_SUB(@today, INTERVAL 29 DAY), DATE_SUB(@today, INTERVAL 29 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW()),
(9, (SELECT id FROM training_plan_details WHERE batch_id = 'dashboard-mock-hist-30'), 6, 6, DATE_SUB(@today, INTERVAL 30 DAY), DATE_SUB(@today, INTERVAL 30 DAY), 'APPROVED', TRUE, 'Dashboard mock pass result', 'system', 0, NOW(), NOW());


-- ============================================================================
-- 4. EMPLOYEE SKILLS (Hơn 50 Chứng chỉ Kỹ năng trãi đều 5 process)
-- ============================================================================

INSERT INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by, delete_flag, created_at, updated_at)
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
INSERT INTO defects (defect_code, defect_description, process_id, detected_date, defect_type, origin_measures, outflow_measures, conclusion, note, created_by, delete_flag, created_at, updated_at)
VALUES 
('DF101', 'Lỗi giả lập MOCK 01', 1, DATE_SUB(@today, INTERVAL 1 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF102', 'Lỗi giả lập MOCK 02', 2, DATE_SUB(@today, INTERVAL 1 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF103', 'Lỗi giả lập MOCK 03', 3, DATE_SUB(@today, INTERVAL 1 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF104', 'Lỗi giả lập MOCK 04', 4, DATE_SUB(@today, INTERVAL 2 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF105', 'Lỗi giả lập MOCK 05', 1, DATE_SUB(@today, INTERVAL 2 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF106', 'Lỗi giả lập MOCK 06', 2, DATE_SUB(@today, INTERVAL 3 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF107', 'Lỗi giả lập MOCK 07', 3, DATE_SUB(@today, INTERVAL 3 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF108', 'Lỗi giả lập MOCK 08', 4, DATE_SUB(@today, INTERVAL 4 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF109', 'Lỗi giả lập MOCK 09', 1, DATE_SUB(@today, INTERVAL 4 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF110', 'Lỗi giả lập MOCK 10', 2, DATE_SUB(@today, INTERVAL 5 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

('DF111', 'Lỗi giả lập MOCK 11', 3, DATE_SUB(@today, INTERVAL 5 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF112', 'Lỗi giả lập MOCK 12', 4, DATE_SUB(@today, INTERVAL 6 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF113', 'Lỗi giả lập MOCK 13', 1, DATE_SUB(@today, INTERVAL 6 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF114', 'Lỗi giả lập MOCK 14', 2, DATE_SUB(@today, INTERVAL 7 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF115', 'Lỗi giả lập MOCK 15', 3, DATE_SUB(@today, INTERVAL 8 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF116', 'Lỗi giả lập MOCK 16', 4, DATE_SUB(@today, INTERVAL 8 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF117', 'Lỗi giả lập MOCK 17', 1, DATE_SUB(@today, INTERVAL 9 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF118', 'Lỗi giả lập MOCK 18', 2, DATE_SUB(@today, INTERVAL 10 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF119', 'Lỗi giả lập MOCK 19', 3, DATE_SUB(@today, INTERVAL 11 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF120', 'Lỗi giả lập MOCK 20', 4, DATE_SUB(@today, INTERVAL 12 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

('DF121', 'Lỗi giả lập MOCK 21', 1, DATE_SUB(@today, INTERVAL 13 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF122', 'Lỗi giả lập MOCK 22', 2, DATE_SUB(@today, INTERVAL 14 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF123', 'Lỗi giả lập MOCK 23', 3, DATE_SUB(@today, INTERVAL 15 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF124', 'Lỗi giả lập MOCK 24', 4, DATE_SUB(@today, INTERVAL 16 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF125', 'Lỗi giả lập MOCK 25', 1, DATE_SUB(@today, INTERVAL 17 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF126', 'Lỗi giả lập MOCK 26', 2, DATE_SUB(@today, INTERVAL 17 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF127', 'Lỗi giả lập MOCK 27', 3, DATE_SUB(@today, INTERVAL 18 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF128', 'Lỗi giả lập MOCK 28', 4, DATE_SUB(@today, INTERVAL 19 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF129', 'Lỗi giả lập MOCK 29', 1, DATE_SUB(@today, INTERVAL 20 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF130', 'Lỗi giả lập MOCK 30', 2, DATE_SUB(@today, INTERVAL 21 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

('DF131', 'Lỗi giả lập MOCK 31', 3, DATE_SUB(@today, INTERVAL 22 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF132', 'Lỗi giả lập MOCK 32', 4, DATE_SUB(@today, INTERVAL 23 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF133', 'Lỗi giả lập MOCK 33', 1, DATE_SUB(@today, INTERVAL 24 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF134', 'Lỗi giả lập MOCK 34', 2, DATE_SUB(@today, INTERVAL 25 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF135', 'Lỗi giả lập MOCK 35', 3, DATE_SUB(@today, INTERVAL 25 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF136', 'Lỗi giả lập MOCK 36', 4, DATE_SUB(@today, INTERVAL 26 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF137', 'Lỗi giả lập MOCK 37', 1, DATE_SUB(@today, INTERVAL 26 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF138', 'Lỗi giả lập MOCK 38', 2, DATE_SUB(@today, INTERVAL 27 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF139', 'Lỗi giả lập MOCK 39', 3, DATE_SUB(@today, INTERVAL 28 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF140', 'Lỗi giả lập MOCK 40', 4, DATE_SUB(@today, INTERVAL 28 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),

('DF141', 'Lỗi giả lập MOCK 41', 1, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF142', 'Lỗi giả lập MOCK 42', 2, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF143', 'Lỗi giả lập MOCK 43', 3, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF144', 'Lỗi giả lập MOCK 44', 4, DATE_SUB(@today, INTERVAL 29 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF145', 'Lỗi giả lập MOCK 45', 1, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF146', 'Lỗi giả lập MOCK 46', 2, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF147', 'Lỗi giả lập MOCK 47', 3, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF148', 'Lỗi giả lập MOCK 48', 4, DATE_SUB(@today, INTERVAL 30 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF149', 'Lỗi giả lập MOCK 49', 1, DATE_SUB(@today, INTERVAL 31 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW()),
('DF150', 'Lỗi giả lập MOCK 50', 2, DATE_SUB(@today, INTERVAL 31 DAY), 'DEFECTIVE_GOODS', 'Measure 1', 'Measure 2', 'N/A', 'Dashboard mock defect', 'system', 0, NOW(), NOW());


-- 1. Rejected Training Plan for tl_tien01
INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note, min_training_per_day, max_training_per_day, created_by)
VALUES (901, 'TP-REJ-TIEN-01', 'Kế hoạch đào tạo tháng 3 - Bị từ chối', '2026-03-01', '2026-03-31', 1, 1, 'REJECTED_BY_MANAGER', 1, 'Thiếu thông tin nhân viên G54.', 1, 3, 'tl_tien01');

-- 2. Rejected Training Result for tl_tien01
-- Note: status REJECTED_BY_MANAGER since REJECTED_BY_SV is not in the results enum
INSERT INTO training_results (id, training_plan_id, title, form_code, year, line_id, team_id, status, current_version, note, created_by)
VALUES (901, 1, 'Kết quả đào tạo T1/2026 - Bị từ chối', 'TR-REJ-TIEN-01', 2026, 1, 1, 'REJECTED_BY_MANAGER', 1, 'Hình ảnh minh chứng không rõ ràng.', 'tl_tien01');

-- 3. Rejected Defect Proposal for tl_tien01
INSERT INTO defect_proposals (id, product_line_id, status, current_version, form_code, delete_flag, created_by)
VALUES (901, 1, 'REJECTED_BY_MANAGER', 1, 'DEF-REJ-TIEN-01', FALSE, 'tl_tien01');

-- 4. Rejected Training Sample Proposal for tl_tien01
INSERT INTO training_sample_proposals (id, product_line_id, status, current_version, form_code, delete_flag, created_by)
VALUES (901, 1, 'REJECTED_BY_SV', 1, 'SAM-REJ-TIEN-01', FALSE, 'tl_tien01');


-- Bật lại foreign key check
SET FOREIGN_KEY_CHECKS = 1;
