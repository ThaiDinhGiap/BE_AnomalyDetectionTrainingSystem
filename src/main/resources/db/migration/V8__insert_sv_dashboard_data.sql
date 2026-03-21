-- ============================================================================
-- V8__insert_sv_dashboard_data.sql
-- Thêm dữ liệu phong phú cho SV Dashboard — tập trung GROUP 1 & 2
-- SV1 (user 3, tran.giamsat1) → group 1 (Tiện CNC), group 2 (Phay CNC)
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
SET @today = CURRENT_DATE;

-- ============================================================================
-- 1. PLANS WAITING_SV — cho SV thấy "Chờ phê duyệt" trên Todo
-- ============================================================================

INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note, min_training_per_day, max_training_per_day, created_by)
VALUES
-- Group 1 (Tiện CNC)
(200, 'TP-SV-W01', 'KH HL T3/2026 - Tổ Tiện Ca Ngày (Chờ SV)',       '2026-03-01', '2026-03-31', 1, 1, 'WAITING_SV', 1, 'Chờ SV phê duyệt.', 1, 3, 'tl_tien01'),
(210, 'TP-SV-W10', 'KH HL T4/2026 - Tổ Tiện Bơm Chìm (Chờ SV)',     '2026-04-01', '2026-04-30', 1, 6, 'WAITING_SV', 1, 'Chờ SV phê duyệt plan Bơm Chìm.', 1, 3, 'tl_tien01'),
(211, 'TP-SV-W11', 'KH HL T4/2026 - Tổ Tiện Van V-Series (Chờ SV)',  '2026-04-01', '2026-04-30', 1, 7, 'WAITING_SV', 1, 'Chờ SV phê duyệt plan Van.', 1, 3, 'tl_tien01'),
-- Group 2 (Phay CNC)
(201, 'TP-SV-W02', 'KH HL T3/2026 - Tổ Phay Ca Ngày (Chờ SV)',       '2026-03-01', '2026-03-31', 2, 2, 'WAITING_SV', 1, 'Chờ SV phê duyệt.', 1, 3, 'tl_phay01');

-- Plan details cho plans WAITING_SV
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date, status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- Plan 200 (Tổ Tiện, line 1)
(200, 1, 'sv-wait-200-nv001', '2026-03-01', '2026-03-10', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 'sv-wait-200-nv002', '2026-03-01', '2026-03-11', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 'sv-wait-200-nv003', '2026-03-01', '2026-03-12', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(), NOW()),
(200, 4, 'sv-wait-200-nv004', '2026-03-01', '2026-03-14', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 'sv-wait-200-nv006', '2026-03-01', '2026-03-15', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(), NOW()),
-- Plan 210 (Tổ Tiện, Bơm Chìm line 6)
(210, 1, 'sv-wait-210-nv001', '2026-04-01', '2026-04-07', NULL, 'PENDING', 'Chờ SV duyệt Bơm Chìm', 'tl_tien01', 0, NOW(), NOW()),
(210, 2, 'sv-wait-210-nv002', '2026-04-01', '2026-04-08', NULL, 'PENDING', 'Chờ SV duyệt Bơm Chìm', 'tl_tien01', 0, NOW(), NOW()),
(210, 3, 'sv-wait-210-nv003', '2026-04-01', '2026-04-09', NULL, 'PENDING', 'Chờ SV duyệt Bơm Chìm', 'tl_tien01', 0, NOW(), NOW()),
-- Plan 211 (Tổ Tiện, Van V-Series line 7)
(211, 4, 'sv-wait-211-nv004', '2026-04-01', '2026-04-10', NULL, 'PENDING', 'Chờ SV duyệt Van', 'tl_tien01', 0, NOW(), NOW()),
(211, 6, 'sv-wait-211-nv006', '2026-04-01', '2026-04-11', NULL, 'PENDING', 'Chờ SV duyệt Van', 'tl_tien01', 0, NOW(), NOW()),
-- Plan 201 (Tổ Phay, line 2)
(201, 7,  'sv-wait-201-nv007', '2026-03-01', '2026-03-15', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(), NOW()),
(201, 8,  'sv-wait-201-nv008', '2026-03-01', '2026-03-16', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(), NOW()),
(201, 9,  'sv-wait-201-nv009', '2026-03-01', '2026-03-17', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(), NOW()),
(201, 10, 'sv-wait-201-nv010', '2026-03-01', '2026-03-18', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 'sv-wait-201-nv012', '2026-03-01', '2026-03-19', NULL, 'PENDING', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(), NOW());


-- ============================================================================
-- 2. RESULTS — nhiều result cho SV nhìn thấy
--    training_results.status: ON_GOING|DONE|WAITING_MANAGER|REJECTED_BY_MANAGER|APPROVED
-- ============================================================================

INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version, note, created_by)
VALUES
-- Group 1 (Tiện CNC) — nhiều result đa dạng status
(200, 3,   'KQ HL T3/2026 - Tổ Tiện (Chờ MG)',          'TR-SV-W01', 2026, 1, 1, 'WAITING_MANAGER', 1, 'Chờ Manager phê duyệt.', 'tl_tien01'),
(210, 107, 'KQ HL T3/2026 - Bơm Chìm (Đang thực hiện)', 'TR-SV-W10', 2026, 1, 6, 'ON_GOING',        1, 'Đang nhập kết quả Bơm Chìm.', 'tl_tien01'),
(211, 108, 'KQ HL T4/2026 - Van (Đang thực hiện)',       'TR-SV-W11', 2026, 1, 7, 'ON_GOING',        1, 'Đang nhập kết quả Van.', 'tl_tien01'),
-- Group 2 (Phay CNC)
(201, 100, 'KQ HL T4/2026 - Tổ Phay (Chờ MG)',          'TR-SV-W02', 2026, 2, 2, 'WAITING_MANAGER', 1, 'Chờ Manager phê duyệt.', 'tl_phay01');


-- ============================================================================
-- 3. TRAINING RESULT DETAILS — rải 6 tháng (Oct 2025→Mar 2026)
--    Phục vụ: Pass Rate, Effectiveness Chart, Watchlist, Recent Activity, Top Samples
--    training_result_details.status: PENDING|DONE|NEED_SIGN|WAITING_SV|REJECTED_BY_SV|APPROVED
-- ============================================================================

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 200 (Tổ Tiện, team 1, line 1) — 24 result details           ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date, status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- October 2025 (3 đánh giá: 2 pass, 1 fail)
(200, 1, 1, '2025-10-05', '2025-10-05', 'APPROVED', TRUE,  'Oct - NV001 pass Process 1', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 2, '2025-10-08', '2025-10-08', 'APPROVED', TRUE,  'Oct - NV002 pass Process 2', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 3, '2025-10-12', '2025-10-12', 'APPROVED', FALSE, 'Oct - NV003 fail Process 3', 'tl_tien01', 0, NOW(), NOW()),
-- November 2025 (4 đánh giá: 3 pass, 1 fail)
(200, 1, 1, '2025-11-03', '2025-11-03', 'APPROVED', TRUE,  'Nov - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 4, 4, '2025-11-10', '2025-11-10', 'APPROVED', TRUE,  'Nov - NV004 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 1, '2025-11-15', '2025-11-15', 'APPROVED', FALSE, 'Nov - NV006 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 2, '2025-11-20', '2025-11-20', 'APPROVED', TRUE,  'Nov - NV002 pass', 'tl_tien01', 0, NOW(), NOW()),
-- December 2025 (4 đánh giá: 3 pass, 1 fail)
(200, 3, 3, '2025-12-02', '2025-12-02', 'APPROVED', TRUE,  'Dec - NV003 pass lần 2', 'tl_tien01', 0, NOW(), NOW()),
(200, 1, 4, '2025-12-09', '2025-12-09', 'APPROVED', TRUE,  'Dec - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 4, 1, '2025-12-15', '2025-12-15', 'APPROVED', FALSE, 'Dec - NV004 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 2, '2025-12-22', '2025-12-22', 'APPROVED', TRUE,  'Dec - NV006 pass', 'tl_tien01', 0, NOW(), NOW()),
-- January 2026 (4 đánh giá: 3 pass, 1 fail)
(200, 1, 1, '2026-01-06', '2026-01-06', 'APPROVED', TRUE,  'Jan - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 3, '2026-01-12', '2026-01-12', 'APPROVED', TRUE,  'Jan - NV002 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 4, '2026-01-18', '2026-01-18', 'APPROVED', FALSE, 'Jan - NV003 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 4, 2, '2026-01-25', '2026-01-25', 'APPROVED', TRUE,  'Jan - NV004 pass', 'tl_tien01', 0, NOW(), NOW()),
-- February 2026 (4 đánh giá: 3 pass, 1 fail)
(200, 1, 1, '2026-02-03', '2026-02-03', 'APPROVED', TRUE,  'Feb - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 3, '2026-02-10', '2026-02-10', 'APPROVED', TRUE,  'Feb - NV006 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 4, '2026-02-17', '2026-02-17', 'APPROVED', FALSE, 'Feb - NV002 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 1, '2026-02-24', '2026-02-24', 'APPROVED', TRUE,  'Feb - NV003 pass', 'tl_tien01', 0, NOW(), NOW()),
-- March 2026 (5 đánh giá: đa dạng status — APPROVED/WAITING_SV/PENDING)
(200, 4, 2, '2026-03-03', '2026-03-03', 'APPROVED', TRUE,  'Mar - NV004 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 1, 3, '2026-03-07', '2026-03-07', 'APPROVED', TRUE,  'Mar - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 4, '2026-03-12', '2026-03-12', 'WAITING_SV', TRUE,  'Mar - NV006 chờ SV ký', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 1, '2026-03-18', '2026-03-18', 'WAITING_SV', TRUE,  'Mar - NV002 chờ SV ký', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 2, '2026-03-25', NULL,         'PENDING',    NULL, 'Mar - NV003 chưa huấn luyện', 'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 210 (Bơm Chìm, team 1, line 6) — 10 result details          ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date, status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES
(210, 1, 31, '2026-01-15', '2026-01-15', 'APPROVED', TRUE,  'Jan - NV001 pass Bơm Chìm',   'tl_tien01', 0, NOW(), NOW()),
(210, 2, 32, '2026-01-20', '2026-01-20', 'APPROVED', TRUE,  'Jan - NV002 pass Bơm Chìm',   'tl_tien01', 0, NOW(), NOW()),
(210, 3, 33, '2026-02-05', '2026-02-05', 'APPROVED', FALSE, 'Feb - NV003 fail Bơm Chìm',   'tl_tien01', 0, NOW(), NOW()),
(210, 4, 31, '2026-02-12', '2026-02-12', 'APPROVED', TRUE,  'Feb - NV004 pass Bơm Chìm',   'tl_tien01', 0, NOW(), NOW()),
(210, 6, 32, '2026-02-20', '2026-02-20', 'APPROVED', TRUE,  'Feb - NV006 pass Bơm Chìm',   'tl_tien01', 0, NOW(), NOW()),
(210, 1, 33, '2026-03-03', '2026-03-03', 'APPROVED', TRUE,  'Mar - NV001 pass lần 2',       'tl_tien01', 0, NOW(), NOW()),
(210, 2, 31, '2026-03-10', '2026-03-10', 'APPROVED', TRUE,  'Mar - NV002 pass',             'tl_tien01', 0, NOW(), NOW()),
(210, 3, 32, '2026-03-15', '2026-03-15', 'REJECTED_BY_SV', FALSE, 'Mar - NV003 SV từ chối', 'tl_tien01', 0, NOW(), NOW()),
(210, 4, 33, '2026-03-20', '2026-03-20', 'WAITING_SV', TRUE,  'Mar - NV004 chờ SV ký',      'tl_tien01', 0, NOW(), NOW()),
(210, 6, 31, '2026-03-25', NULL,         'PENDING',    NULL, 'Mar - NV006 chưa huấn luyện',  'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 211 (Van V-Series, team 1, line 7) — 8 result details        ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date, status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES
(211, 1, 34, '2026-01-10', '2026-01-10', 'APPROVED', TRUE,  'Jan - NV001 pass Van',     'tl_tien01', 0, NOW(), NOW()),
(211, 2, 35, '2026-01-18', '2026-01-18', 'APPROVED', FALSE, 'Jan - NV002 fail Van',     'tl_tien01', 0, NOW(), NOW()),
(211, 4, 36, '2026-02-08', '2026-02-08', 'APPROVED', TRUE,  'Feb - NV004 pass Van',     'tl_tien01', 0, NOW(), NOW()),
(211, 6, 34, '2026-02-18', '2026-02-18', 'APPROVED', TRUE,  'Feb - NV006 pass Van',     'tl_tien01', 0, NOW(), NOW()),
(211, 1, 35, '2026-03-05', '2026-03-05', 'APPROVED', TRUE,  'Mar - NV001 pass Van',     'tl_tien01', 0, NOW(), NOW()),
(211, 2, 36, '2026-03-12', '2026-03-12', 'WAITING_SV', TRUE,  'Mar - NV002 chờ SV ký',  'tl_tien01', 0, NOW(), NOW()),
(211, 4, 34, '2026-03-18', NULL,         'PENDING',    NULL, 'Mar - NV004 chưa huấn luyện', 'tl_tien01', 0, NOW(), NOW()),
(211, 6, 35, '2026-03-22', NULL,         'PENDING',    NULL, 'Mar - NV006 chưa huấn luyện', 'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 201 (Tổ Phay, team 2, line 2) — 20 result details           ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date, status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- October 2025
(201, 7,  7,  '2025-10-10', '2025-10-10', 'APPROVED', TRUE,  'Oct - NV007 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 8,  8,  '2025-10-15', '2025-10-15', 'APPROVED', FALSE, 'Oct - NV008 fail', 'tl_phay01', 0, NOW(), NOW()),
(201, 9,  9,  '2025-10-22', '2025-10-22', 'APPROVED', TRUE,  'Oct - NV009 pass', 'tl_phay01', 0, NOW(), NOW()),
-- November 2025
(201, 10, 10, '2025-11-05', '2025-11-05', 'APPROVED', TRUE,  'Nov - NV010 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 7,  '2025-11-12', '2025-11-12', 'APPROVED', TRUE,  'Nov - NV012 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 7,  8,  '2025-11-20', '2025-11-20', 'APPROVED', FALSE, 'Nov - NV007 fail', 'tl_phay01', 0, NOW(), NOW()),
-- December 2025
(201, 8,  9,  '2025-12-03', '2025-12-03', 'APPROVED', TRUE,  'Dec - NV008 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 9,  10, '2025-12-10', '2025-12-10', 'APPROVED', TRUE,  'Dec - NV009 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 10, 7,  '2025-12-18', '2025-12-18', 'APPROVED', FALSE, 'Dec - NV010 fail', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 8,  '2025-12-22', '2025-12-22', 'APPROVED', TRUE,  'Dec - NV012 pass', 'tl_phay01', 0, NOW(), NOW()),
-- January 2026
(201, 7,  9,  '2026-01-06', '2026-01-06', 'APPROVED', TRUE,  'Jan - NV007 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 8,  10, '2026-01-15', '2026-01-15', 'APPROVED', TRUE,  'Jan - NV008 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 9,  7,  '2026-01-22', '2026-01-22', 'APPROVED', FALSE, 'Jan - NV009 fail', 'tl_phay01', 0, NOW(), NOW()),
-- February 2026
(201, 10, 8,  '2026-02-05', '2026-02-05', 'APPROVED', TRUE,  'Feb - NV010 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 9,  '2026-02-12', '2026-02-12', 'APPROVED', TRUE,  'Feb - NV012 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 7,  10, '2026-02-20', '2026-02-20', 'APPROVED', TRUE,  'Feb - NV007 pass', 'tl_phay01', 0, NOW(), NOW()),
-- March 2026
(201, 8,  7,  '2026-03-03', '2026-03-03', 'APPROVED', TRUE,  'Mar - NV008 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 9,  8,  '2026-03-10', '2026-03-10', 'WAITING_SV', TRUE,  'Mar - NV009 chờ SV ký', 'tl_phay01', 0, NOW(), NOW()),
(201, 10, 9,  '2026-03-15', '2026-03-15', 'REJECTED_BY_SV', FALSE, 'Mar - NV010 SV từ chối', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 10, '2026-03-20', NULL,         'PENDING',    NULL, 'Mar - NV012 chưa huấn luyện', 'tl_phay01', 0, NOW(), NOW());


-- ============================================================================
-- 4. DEFECTS rải 6 tháng cho GROUP 1 & 2 — Effectiveness Chart + Hotspot
-- ============================================================================

INSERT INTO defects (defect_code, defect_description, process_id, detected_date, defect_type, origin_measures, outflow_measures, conclusion, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- ── Group 1 (Tiện CNC, processes 1-4 line 1 + 22-30 lines 6-8) ──────
-- Line 1 — P-Series
('SVD01', 'Xước trục bơm sau tiện tinh - lô tháng 10',        2, '2025-10-05', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Thay dao', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD02', 'Đường kính ngoài sai IT8 - lô tháng 10',           1, '2025-10-18', 'CLAIM',           'M1', 'M2', 'Setup lại', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD03', 'Ba via ren M20 không đạt - tháng 11',              3, '2025-11-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Thay taro', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD04', 'Trục ngắn hơn bản vẽ 0.3mm - tháng 11',           1, '2025-11-22', 'CLAIM',           'M1', 'M2', 'Kiểm offset', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD05', 'Độ nhám Ra vượt 3.2 - tháng 12',                   2, '2025-12-05', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Mài dao', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD06', 'Lỗ tâm 60° lệch >0.05mm - tháng 12',              3, '2025-12-20', 'CLAIM',           'M1', 'M2', 'Kiểm đồ gá', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD07', 'Vát mép trục bơm không đều - tháng 1',             4, '2026-01-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'SOP mới', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD08', 'Ren M20 ba via sau cắt đứt - tháng 1',            3, '2026-01-25', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Đổi dao', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD09', 'Xước trục bơm do phoi quấn - tháng 2',            2, '2026-02-10', 'CLAIM',           'M1', 'M2', 'Thổi phoi', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD10', 'Đường kính lệch 0.05mm - tháng 2',                1, '2026-02-25', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Hiệu chuẩn', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD11', 'Trục bơm cong sau cắt - tháng 3',                 4, '2026-03-05', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Giảm feed', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD12', 'Nhám bề mặt vượt Ra 1.6 - tháng 3',              2, '2026-03-12', 'CLAIM',           'M1', 'M2', 'Thay insert', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD13', 'Ren ngoài lỗi profil - tháng 3',                  3, '2026-03-18', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Kiểm ren', 'SV mock', 'system', 0, NOW(), NOW()),
-- Line 6 — Bơm Chìm S-Series
('SVD14', 'Xước trục bơm chìm inox - tháng 12',              22, '2025-12-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao mòn', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD15', 'Vỏ bơm chìm lệch tâm - tháng 1',                 23, '2026-01-15', 'CLAIM',           'M1', 'M2', 'Đồ gá mòn', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD16', 'Cánh bơm mất cân bằng - tháng 2',                 24, '2026-02-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Đúc lỗi', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD17', 'Xước inox trục S200 - tháng 3',                    22, '2026-03-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Thay dao CBN', 'SV mock', 'system', 0, NOW(), NOW()),
-- Line 7 — Van V-Series
('SVD18', 'Mặt tựa van rỗ micro - tháng 11',                 25, '2025-11-15', 'CLAIM',           'M1', 'M2', 'Phôi bọt khí', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD19', 'Ren trục van Tr28 sờn - tháng 1',                  26, '2026-01-20', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao ren mòn', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD20', 'Đế van không phẳng - tháng 3',                     27, '2026-03-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Kẹp lực quá', 'SV mock', 'system', 0, NOW(), NOW()),
-- Line 8 — Trục Khuỷu C-Series
('SVD21', 'Oval cổ trục chính - tháng 10',                    28, '2025-10-12', 'CLAIM',           'M1', 'M2', 'Phôi lệch', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD22', 'Nhám cổ biên Ra >1.0 - tháng 12',                 29, '2025-12-18', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Bỏ qua mài', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD23', 'Vết cháy mài cổ trục - tháng 2',                  30, '2026-02-15', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Feed nhanh', 'SV mock', 'system', 0, NOW(), NOW()),

-- ── Group 2 (Phay CNC, processes 5-9, line 2) ───────────────────────
('SVD24', 'Độ phẳng mặt vỏ bơm vượt 0.1mm - tháng 10',       5, '2025-10-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao rung', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD25', 'Hốc buồng bơm rộng +0.08mm - tháng 10',           6, '2025-10-25', 'CLAIM',           'M1', 'M2', 'CAM sai', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD26', 'Lỗ doa H7 bị côn - tháng 11',                     7, '2025-11-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Doa mòn', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD27', 'Rãnh then lệch tâm >0.03mm - tháng 11',           8, '2025-11-28', 'CLAIM',           'M1', 'M2', 'Kẹp lỏng', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD28', 'Taro M8 bị vỡ kẹt trong lỗ - tháng 12',           9, '2025-12-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Phoi kẹt', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD29', 'Song song 2 mặt phay >0.05mm - tháng 12',         5, '2025-12-22', 'CLAIM',           'M1', 'M2', 'Trục lỏng', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD30', 'Mặt phẳng vỏ bơm có dao cứa - tháng 1',           5, '2026-01-12', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao mẻ', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD31', 'Khoan lỗ sâu lệch hướng - tháng 1',               7, '2026-01-28', 'CLAIM',           'M1', 'M2', 'Đồ gá sai', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD32', 'Rãnh dầu R2 không đều - tháng 2',                  8, '2026-02-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao mẻ góc', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD33', 'Ren M10 bị nghiêng - tháng 2',                     9, '2026-02-25', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Mũi cùn', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD34', 'Hốc buồng bơm sai dung sai - tháng 3',            6, '2026-03-05', 'CLAIM',           'M1', 'M2', 'CAM lỗi', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD35', 'Taro M6 bị gãy - tháng 3',                         9, '2026-03-15', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Không dầu cắt', 'SV mock', 'system', 0, NOW(), NOW()),
('SVD36', 'Phẳng vỏ bơm lệch sau clamp - tháng 3',           5, '2026-03-20', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Lực kẹp lớn', 'SV mock', 'system', 0, NOW(), NOW());


-- ============================================================================
-- 5. EMPLOYEE SKILLS bổ sung cho team 2 (Phay) — Watchlist + Coverage
--    Team 1 (Tiện) đã có đầy đủ từ V7
-- ============================================================================

INSERT IGNORE INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by, delete_flag, created_at, updated_at)
VALUES
-- Tổ Phay (team 2, employees 7-12) — processes 5-9
(7,  5, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(7,  6, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 10 DAY), 'system_mock', 0, NOW(), NOW()),
(7,  7, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(8,  5, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(8,  6, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(8,  7, 'REVOKED',        '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(8,  8, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 5 DAY), 'system_mock', 0, NOW(), NOW()),
(9,  5, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 15 DAY), 'system_mock', 0, NOW(), NOW()),
(9,  6, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(9,  8, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(10, 5, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(10, 7, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(10, 9, 'REVOKED',        '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(12, 5, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(12, 6, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 8 DAY), 'system_mock', 0, NOW(), NOW()),
(12, 8, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(12, 9, 'VALID',          '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW());


SET FOREIGN_KEY_CHECKS = 1;
