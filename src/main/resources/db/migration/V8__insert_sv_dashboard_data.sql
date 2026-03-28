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

INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES
-- Group 1 (Tiện CNC)
(200, 'TP-SV-W01', 'KH HL T3/2026 - Tổ Tiện Ca Ngày (Chờ SV)', '2026-03-01', '2026-03-31', 1, 1, 'PENDING_REVIEW', 1,
 'Chờ SV phê duyệt.', 1, 3, 'tl_tien01'),
(210, 'TP-SV-W10', 'KH HL T4/2026 - Tổ Tiện Bơm Chìm (Chờ SV)', '2026-04-01', '2026-04-30', 1, 6, 'PENDING_REVIEW', 1,
 'Chờ SV phê duyệt plan Bơm Chìm.', 1, 3, 'tl_tien01'),
(211, 'TP-SV-W11', 'KH HL T4/2026 - Tổ Tiện Van V-Series (Chờ SV)', '2026-04-01', '2026-04-30', 1, 7, 'PENDING_REVIEW',
 1, 'Chờ SV phê duyệt plan Van.', 1, 3, 'tl_tien01'),
-- Group 2 (Phay CNC)
(201, 'TP-SV-W02', 'KH HL T3/2026 - Tổ Phay Ca Ngày (Chờ SV)', '2026-03-01', '2026-03-31', 2, 2, 'PENDING_REVIEW', 1,
 'Chờ SV phê duyệt.', 1, 3, 'tl_phay01');

-- Plan details cho plans WAITING_SV
INSERT INTO training_plan_details (training_plan_id, employee_id, batch_id, target_month, planned_date, actual_date,
                                   status, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- Plan 200 (Tổ Tiện, line 1)
(200, 1, 'sv-wait-200-nv001', '2026-03-01', '2026-03-10', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(),
 NOW()),
(200, 2, 'sv-wait-200-nv002', '2026-03-01', '2026-03-11', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(),
 NOW()),
(200, 3, 'sv-wait-200-nv003', '2026-03-01', '2026-03-12', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(),
 NOW()),
(200, 4, 'sv-wait-200-nv004', '2026-03-01', '2026-03-14', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(),
 NOW()),
(200, 6, 'sv-wait-200-nv006', '2026-03-01', '2026-03-15', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_tien01', 0, NOW(),
 NOW()),
-- Plan 210 (Tổ Tiện, Bơm Chìm line 6)
(210, 1, 'sv-wait-210-nv001', '2026-04-01', '2026-04-07', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt Bơm Chìm', 'tl_tien01', 0,
 NOW(), NOW()),
(210, 2, 'sv-wait-210-nv002', '2026-04-01', '2026-04-08', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt Bơm Chìm', 'tl_tien01', 0,
 NOW(), NOW()),
(210, 3, 'sv-wait-210-nv003', '2026-04-01', '2026-04-09', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt Bơm Chìm', 'tl_tien01', 0,
 NOW(), NOW()),
-- Plan 211 (Tổ Tiện, Van V-Series line 7)
(211, 4, 'sv-wait-211-nv004', '2026-04-01', '2026-04-10', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt Van', 'tl_tien01', 0, NOW(),
 NOW()),
(211, 6, 'sv-wait-211-nv006', '2026-04-01', '2026-04-11', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt Van', 'tl_tien01', 0, NOW(),
 NOW()),
-- Plan 201 (Tổ Phay, line 2)
(201, 7, 'sv-wait-201-nv007', '2026-03-01', '2026-03-15', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(),
 NOW()),
(201, 8, 'sv-wait-201-nv008', '2026-03-01', '2026-03-16', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(),
 NOW()),
(201, 9, 'sv-wait-201-nv009', '2026-03-01', '2026-03-17', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(),
 NOW()),
(201, 10, 'sv-wait-201-nv010', '2026-03-01', '2026-03-18', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(),
 NOW()),
(201, 12, 'sv-wait-201-nv012', '2026-03-01', '2026-03-19', NULL, 'PENDING_REVIEW', 'Chờ SV duyệt', 'tl_phay01', 0, NOW(),
 NOW());


-- ============================================================================
-- 2. RESULTS — nhiều result cho SV nhìn thấy
--    training_results.status: ON_GOING|DONE|WAITING_MANAGER|REJECTED_BY_MANAGER|COMPLETED
-- ============================================================================

INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES
-- Group 1 (Tiện CNC) — nhiều result đa dạng status
(200, 3, 'KQ HL T3/2026 - Tổ Tiện (Chờ MG)', 'TR-SV-W01', 2026, 1, 1, 'PENDING_APPROVAL', 1, 'Chờ Manager phê duyệt.',
 'tl_tien01'),
(210, 107, 'KQ HL T3/2026 - Bơm Chìm (Đang thực hiện)', 'TR-SV-W10', 2026, 1, 6, 'ONGOING', 1,
 'Đang nhập kết quả Bơm Chìm.', 'tl_tien01'),
(211, 108, 'KQ HL T4/2026 - Van (Đang thực hiện)', 'TR-SV-W11', 2026, 1, 7, 'ONGOING', 1, 'Đang nhập kết quả Van.',
 'tl_tien01'),
-- Group 2 (Phay CNC)
(201, 100, 'KQ HL T4/2026 - Tổ Phay (Chờ MG)', 'TR-SV-W02', 2026, 2, 2, 'PENDING_APPROVAL', 1, 'Chờ Manager phê duyệt.',
 'tl_phay01');


-- ============================================================================
-- 3. TRAINING RESULT DETAILS — rải 6 tháng (Oct 2025→Mar 2026)
--    Phục vụ: Pass Rate, Effectiveness Chart, Watchlist, Recent Activity, Top Samples
--    training_result_details.status: PENDING|DONE|NEED_SIGN|WAITING_SV|REJECTED_BY_SV|COMPLETED
-- ============================================================================

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 200 (Tổ Tiện, team 1, line 1) — 24 result details           ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date,
                                     status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- October 2025 (3 đánh giá: 2 pass, 1 fail)
(200, 1, 1, '2025-10-05', '2025-10-05', 'COMPLETED', TRUE, 'Oct - NV001 pass Process 1', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 2, '2025-10-08', '2025-10-08', 'COMPLETED', TRUE, 'Oct - NV002 pass Process 2', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 3, '2025-10-12', '2025-10-12', 'COMPLETED', FALSE, 'Oct - NV003 fail Process 3', 'tl_tien01', 0, NOW(), NOW()),
-- November 2025 (4 đánh giá: 3 pass, 1 fail)
(200, 1, 1, '2025-11-03', '2025-11-03', 'COMPLETED', TRUE, 'Nov - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 4, 4, '2025-11-10', '2025-11-10', 'COMPLETED', TRUE, 'Nov - NV004 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 1, '2025-11-15', '2025-11-15', 'COMPLETED', FALSE, 'Nov - NV006 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 2, '2025-11-20', '2025-11-20', 'COMPLETED', TRUE, 'Nov - NV002 pass', 'tl_tien01', 0, NOW(), NOW()),
-- December 2025 (4 đánh giá: 3 pass, 1 fail)
(200, 3, 3, '2025-12-02', '2025-12-02', 'COMPLETED', TRUE, 'Dec - NV003 pass lần 2', 'tl_tien01', 0, NOW(), NOW()),
(200, 1, 4, '2025-12-09', '2025-12-09', 'COMPLETED', TRUE, 'Dec - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 4, 1, '2025-12-15', '2025-12-15', 'COMPLETED', FALSE, 'Dec - NV004 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 2, '2025-12-22', '2025-12-22', 'COMPLETED', TRUE, 'Dec - NV006 pass', 'tl_tien01', 0, NOW(), NOW()),
-- January 2026 (4 đánh giá: 3 pass, 1 fail)
(200, 1, 1, '2026-01-06', '2026-01-06', 'COMPLETED', TRUE, 'Jan - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 3, '2026-01-12', '2026-01-12', 'COMPLETED', TRUE, 'Jan - NV002 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 4, '2026-01-18', '2026-01-18', 'COMPLETED', FALSE, 'Jan - NV003 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 4, 2, '2026-01-25', '2026-01-25', 'COMPLETED', TRUE, 'Jan - NV004 pass', 'tl_tien01', 0, NOW(), NOW()),
-- February 2026 (4 đánh giá: 3 pass, 1 fail)
(200, 1, 1, '2026-02-03', '2026-02-03', 'COMPLETED', TRUE, 'Feb - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 3, '2026-02-10', '2026-02-10', 'COMPLETED', TRUE, 'Feb - NV006 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 4, '2026-02-17', '2026-02-17', 'COMPLETED', FALSE, 'Feb - NV002 fail', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 1, '2026-02-24', '2026-02-24', 'COMPLETED', TRUE, 'Feb - NV003 pass', 'tl_tien01', 0, NOW(), NOW()),
-- March 2026 (5 đánh giá: đa dạng status — COMPLETED/WAITING_SV/PENDING)
(200, 4, 2, '2026-03-03', '2026-03-03', 'COMPLETED', TRUE, 'Mar - NV004 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 1, 3, '2026-03-07', '2026-03-07', 'COMPLETED', TRUE, 'Mar - NV001 pass', 'tl_tien01', 0, NOW(), NOW()),
(200, 6, 4, '2026-03-12', '2026-03-12', 'PENDING_REVIEW', TRUE, 'Mar - NV006 chờ SV ký', 'tl_tien01', 0, NOW(), NOW()),
(200, 2, 1, '2026-03-18', '2026-03-18', 'PENDING_REVIEW', TRUE, 'Mar - NV002 chờ SV ký', 'tl_tien01', 0, NOW(), NOW()),
(200, 3, 2, '2026-03-25', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV003 chưa huấn luyện', 'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 210 (Bơm Chìm, team 1, line 6) — 10 result details          ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date,
                                     status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES (210, 1, 31, '2026-01-15', '2026-01-15', 'COMPLETED', TRUE, 'Jan - NV001 pass Bơm Chìm', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 2, 32, '2026-01-20', '2026-01-20', 'COMPLETED', TRUE, 'Jan - NV002 pass Bơm Chìm', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 3, 33, '2026-02-05', '2026-02-05', 'COMPLETED', FALSE, 'Feb - NV003 fail Bơm Chìm', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 4, 31, '2026-02-12', '2026-02-12', 'COMPLETED', TRUE, 'Feb - NV004 pass Bơm Chìm', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 6, 32, '2026-02-20', '2026-02-20', 'COMPLETED', TRUE, 'Feb - NV006 pass Bơm Chìm', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 1, 33, '2026-03-03', '2026-03-03', 'COMPLETED', TRUE, 'Mar - NV001 pass lần 2', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 2, 31, '2026-03-10', '2026-03-10', 'COMPLETED', TRUE, 'Mar - NV002 pass', 'tl_tien01', 0, NOW(), NOW()),
       (210, 3, 32, '2026-03-15', '2026-03-15', 'REJECTED', FALSE, 'Mar - NV003 SV từ chối', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 4, 33, '2026-03-20', '2026-03-20', 'PENDING_REVIEW', TRUE, 'Mar - NV004 chờ SV ký', 'tl_tien01', 0, NOW(),
        NOW()),
       (210, 6, 31, '2026-03-25', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV006 chưa huấn luyện', 'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 211 (Van V-Series, team 1, line 7) — 8 result details        ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date,
                                     status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES (211, 1, 34, '2026-01-10', '2026-01-10', 'COMPLETED', TRUE, 'Jan - NV001 pass Van', 'tl_tien01', 0, NOW(),
        NOW()),
       (211, 2, 35, '2026-01-18', '2026-01-18', 'COMPLETED', FALSE, 'Jan - NV002 fail Van', 'tl_tien01', 0, NOW(),
        NOW()),
       (211, 4, 36, '2026-02-08', '2026-02-08', 'COMPLETED', TRUE, 'Feb - NV004 pass Van', 'tl_tien01', 0, NOW(),
        NOW()),
       (211, 6, 34, '2026-02-18', '2026-02-18', 'COMPLETED', TRUE, 'Feb - NV006 pass Van', 'tl_tien01', 0, NOW(),
        NOW()),
       (211, 1, 35, '2026-03-05', '2026-03-05', 'COMPLETED', TRUE, 'Mar - NV001 pass Van', 'tl_tien01', 0, NOW(),
        NOW()),
       (211, 2, 36, '2026-03-12', '2026-03-12', 'PENDING_REVIEW', TRUE, 'Mar - NV002 chờ SV ký', 'tl_tien01', 0, NOW(),
        NOW()),
       (211, 4, 34, '2026-03-18', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV004 chưa huấn luyện', 'tl_tien01', 0, NOW(), NOW()),
       (211, 6, 35, '2026-03-22', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV006 chưa huấn luyện', 'tl_tien01', 0, NOW(), NOW());

-- ╔═══════════════════════════════════════════════════════════════════════╗
-- ║ Result 201 (Tổ Phay, team 2, line 2) — 20 result details           ║
-- ╚═══════════════════════════════════════════════════════════════════════╝
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date,
                                     status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- October 2025
(201, 7, 7, '2025-10-10', '2025-10-10', 'COMPLETED', TRUE, 'Oct - NV007 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 8, 8, '2025-10-15', '2025-10-15', 'COMPLETED', FALSE, 'Oct - NV008 fail', 'tl_phay01', 0, NOW(), NOW()),
(201, 9, 9, '2025-10-22', '2025-10-22', 'COMPLETED', TRUE, 'Oct - NV009 pass', 'tl_phay01', 0, NOW(), NOW()),
-- November 2025
(201, 10, 10, '2025-11-05', '2025-11-05', 'COMPLETED', TRUE, 'Nov - NV010 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 7, '2025-11-12', '2025-11-12', 'COMPLETED', TRUE, 'Nov - NV012 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 7, 8, '2025-11-20', '2025-11-20', 'COMPLETED', FALSE, 'Nov - NV007 fail', 'tl_phay01', 0, NOW(), NOW()),
-- December 2025
(201, 8, 9, '2025-12-03', '2025-12-03', 'COMPLETED', TRUE, 'Dec - NV008 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 9, 10, '2025-12-10', '2025-12-10', 'COMPLETED', TRUE, 'Dec - NV009 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 10, 7, '2025-12-18', '2025-12-18', 'COMPLETED', FALSE, 'Dec - NV010 fail', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 8, '2025-12-22', '2025-12-22', 'COMPLETED', TRUE, 'Dec - NV012 pass', 'tl_phay01', 0, NOW(), NOW()),
-- January 2026
(201, 7, 9, '2026-01-06', '2026-01-06', 'COMPLETED', TRUE, 'Jan - NV007 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 8, 10, '2026-01-15', '2026-01-15', 'COMPLETED', TRUE, 'Jan - NV008 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 9, 7, '2026-01-22', '2026-01-22', 'COMPLETED', FALSE, 'Jan - NV009 fail', 'tl_phay01', 0, NOW(), NOW()),
-- February 2026
(201, 10, 8, '2026-02-05', '2026-02-05', 'COMPLETED', TRUE, 'Feb - NV010 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 9, '2026-02-12', '2026-02-12', 'COMPLETED', TRUE, 'Feb - NV012 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 7, 10, '2026-02-20', '2026-02-20', 'COMPLETED', TRUE, 'Feb - NV007 pass', 'tl_phay01', 0, NOW(), NOW()),
-- March 2026
(201, 8, 7, '2026-03-03', '2026-03-03', 'COMPLETED', TRUE, 'Mar - NV008 pass', 'tl_phay01', 0, NOW(), NOW()),
(201, 9, 8, '2026-03-10', '2026-03-10', 'PENDING_REVIEW', TRUE, 'Mar - NV009 chờ SV ký', 'tl_phay01', 0, NOW(), NOW()),
(201, 10, 9, '2026-03-15', '2026-03-15', 'REJECTED', FALSE, 'Mar - NV010 SV từ chối', 'tl_phay01', 0, NOW(), NOW()),
(201, 12, 10, '2026-03-20', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV012 chưa huấn luyện', 'tl_phay01', 0, NOW(), NOW());


-- ============================================================================
-- 4. DEFECTS rải 6 tháng cho GROUP 1 & 2 — Effectiveness Chart + Hotspot
-- ============================================================================

INSERT INTO defects (defect_code, defect_description, process_id, detected_date, defect_type, origin_measures,
                     outflow_measures, conclusion, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- ── Group 1 (Tiện CNC, processes 1-4 line 1 + 22-30 lines 6-8) ──────
-- Line 1 — P-Series
('SVD01', 'Xước trục bơm sau tiện tinh - lô tháng 10', 2, '2025-10-05', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Thay dao',
 'SV mock', 'system', 0, NOW(), NOW()),
('SVD02', 'Đường kính ngoài sai IT8 - lô tháng 10', 1, '2025-10-18', 'CLAIM', 'M1', 'M2', 'Setup lại', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD03', 'Ba via ren M20 không đạt - tháng 11', 3, '2025-11-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Thay taro', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD04', 'Trục ngắn hơn bản vẽ 0.3mm - tháng 11', 1, '2025-11-22', 'CLAIM', 'M1', 'M2', 'Kiểm offset', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD05', 'Độ nhám Ra vượt 3.2 - tháng 12', 2, '2025-12-05', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Mài dao', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD06', 'Lỗ tâm 60° lệch >0.05mm - tháng 12', 3, '2025-12-20', 'CLAIM', 'M1', 'M2', 'Kiểm đồ gá', 'SV mock', 'system',
 0, NOW(), NOW()),
('SVD07', 'Vát mép trục bơm không đều - tháng 1', 4, '2026-01-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'SOP mới', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD08', 'Ren M20 ba via sau cắt đứt - tháng 1', 3, '2026-01-25', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Đổi dao', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD09', 'Xước trục bơm do phoi quấn - tháng 2', 2, '2026-02-10', 'CLAIM', 'M1', 'M2', 'Thổi phoi', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD10', 'Đường kính lệch 0.05mm - tháng 2', 1, '2026-02-25', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Hiệu chuẩn', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD11', 'Trục bơm cong sau cắt - tháng 3', 4, '2026-03-05', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Giảm feed', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD12', 'Nhám bề mặt vượt Ra 1.6 - tháng 3', 2, '2026-03-12', 'CLAIM', 'M1', 'M2', 'Thay insert', 'SV mock', 'system',
 0, NOW(), NOW()),
('SVD13', 'Ren ngoài lỗi profil - tháng 3', 3, '2026-03-18', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Kiểm ren', 'SV mock',
 'system', 0, NOW(), NOW()),
-- Line 6 — Bơm Chìm S-Series
('SVD14', 'Xước trục bơm chìm inox - tháng 12', 22, '2025-12-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao mòn', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD15', 'Vỏ bơm chìm lệch tâm - tháng 1', 23, '2026-01-15', 'CLAIM', 'M1', 'M2', 'Đồ gá mòn', 'SV mock', 'system', 0,
 NOW(), NOW()),
('SVD16', 'Cánh bơm mất cân bằng - tháng 2', 24, '2026-02-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Đúc lỗi', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD17', 'Xước inox trục S200 - tháng 3', 22, '2026-03-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Thay dao CBN', 'SV mock',
 'system', 0, NOW(), NOW()),
-- Line 7 — Van V-Series
('SVD18', 'Mặt tựa van rỗ micro - tháng 11', 25, '2025-11-15', 'CLAIM', 'M1', 'M2', 'Phôi bọt khí', 'SV mock', 'system',
 0, NOW(), NOW()),
('SVD19', 'Ren trục van Tr28 sờn - tháng 1', 26, '2026-01-20', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao ren mòn', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD20', 'Đế van không phẳng - tháng 3', 27, '2026-03-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Kẹp lực quá', 'SV mock',
 'system', 0, NOW(), NOW()),
-- Line 8 — Trục Khuỷu C-Series
('SVD21', 'Oval cổ trục chính - tháng 10', 28, '2025-10-12', 'CLAIM', 'M1', 'M2', 'Phôi lệch', 'SV mock', 'system', 0,
 NOW(), NOW()),
('SVD22', 'Nhám cổ biên Ra >1.0 - tháng 12', 29, '2025-12-18', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Bỏ qua mài', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD23', 'Vết cháy mài cổ trục - tháng 2', 30, '2026-02-15', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Feed nhanh', 'SV mock',
 'system', 0, NOW(), NOW()),

-- ── Group 2 (Phay CNC, processes 5-9, line 2) ───────────────────────
('SVD24', 'Độ phẳng mặt vỏ bơm vượt 0.1mm - tháng 10', 5, '2025-10-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao rung',
 'SV mock', 'system', 0, NOW(), NOW()),
('SVD25', 'Hốc buồng bơm rộng +0.08mm - tháng 10', 6, '2025-10-25', 'CLAIM', 'M1', 'M2', 'CAM sai', 'SV mock', 'system',
 0, NOW(), NOW()),
('SVD26', 'Lỗ doa H7 bị côn - tháng 11', 7, '2025-11-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Doa mòn', 'SV mock', 'system',
 0, NOW(), NOW()),
('SVD27', 'Rãnh then lệch tâm >0.03mm - tháng 11', 8, '2025-11-28', 'CLAIM', 'M1', 'M2', 'Kẹp lỏng', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD28', 'Taro M8 bị vỡ kẹt trong lỗ - tháng 12', 9, '2025-12-08', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Phoi kẹt',
 'SV mock', 'system', 0, NOW(), NOW()),
('SVD29', 'Song song 2 mặt phay >0.05mm - tháng 12', 5, '2025-12-22', 'CLAIM', 'M1', 'M2', 'Trục lỏng', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD30', 'Mặt phẳng vỏ bơm có dao cứa - tháng 1', 5, '2026-01-12', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao mẻ', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD31', 'Khoan lỗ sâu lệch hướng - tháng 1', 7, '2026-01-28', 'CLAIM', 'M1', 'M2', 'Đồ gá sai', 'SV mock', 'system',
 0, NOW(), NOW()),
('SVD32', 'Rãnh dầu R2 không đều - tháng 2', 8, '2026-02-10', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Dao mẻ góc', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD33', 'Ren M10 bị nghiêng - tháng 2', 9, '2026-02-25', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Mũi cùn', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD34', 'Hốc buồng bơm sai dung sai - tháng 3', 6, '2026-03-05', 'CLAIM', 'M1', 'M2', 'CAM lỗi', 'SV mock', 'system',
 0, NOW(), NOW()),
('SVD35', 'Taro M6 bị gãy - tháng 3', 9, '2026-03-15', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Không dầu cắt', 'SV mock',
 'system', 0, NOW(), NOW()),
('SVD36', 'Phẳng vỏ bơm lệch sau clamp - tháng 3', 5, '2026-03-20', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Lực kẹp lớn',
 'SV mock', 'system', 0, NOW(), NOW());


-- ============================================================================
-- 5. EMPLOYEE SKILLS bổ sung cho team 2 (Phay) — Watchlist + Coverage
--    Team 1 (Tiện) đã có đầy đủ từ V7
-- ============================================================================

INSERT IGNORE INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by,
                                    delete_flag, created_at, updated_at)
VALUES
-- Tổ Phay (team 2, employees 7-12) — processes 5-9
(7, 5, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(7, 6, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 10 DAY), 'system_mock', 0, NOW(), NOW()),
(7, 7, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(8, 5, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(8, 6, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(8, 7, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(8, 8, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 5 DAY), 'system_mock', 0, NOW(), NOW()),
(9, 5, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 15 DAY), 'system_mock', 0, NOW(), NOW()),
(9, 6, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(9, 8, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(10, 5, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(10, 7, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(10, 9, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(12, 5, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(12, 6, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 8 DAY), 'system_mock', 0, NOW(), NOW()),
(12, 8, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(12, 9, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW());

-- ============================================================================
-- 1. BỔ SUNG PRODUCT LINES cho các group chưa có line (group 3,4,5,6)
--    Hiện tại: G1→line1, G2→line2, G3→line3, G4→line5, G5→line4, G6→chưa có
-- ============================================================================

INSERT INTO product_lines (id, code, name, group_id, created_by)
VALUES (6, 'PL-TIEN-S1', 'Dòng Bơm Chìm S-Series (Tiện)', 1, 'admin'),
       (7, 'PL-TIEN-V1', 'Dòng Van Công Nghiệp V-Series', 1, 'admin'),
       (8, 'PL-TIEN-C1', 'Dòng Trục Khuỷu C-Series', 1, 'admin'),
       (9, 'PL-HAN-W2', 'Dòng Bơm Ly Tâm W200-Series (Hàn)', 3, 'admin'),
       (10, 'PL-LA-B2', 'Dây Chuyền Lắp Ráp Bơm Bùn B2', 4, 'admin'),
       (11, 'PL-QC-01', 'Kiểm Định Chất Lượng Tổng Hợp', 6, 'admin')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- ============================================================================
-- 2. PROCESSES cho các lines mới (nếu chưa có)
-- ============================================================================

INSERT IGNORE INTO processes (id, code, name, description, classification, standard_time_jt, product_line_id,
                              created_by)
VALUES
-- Line 6 — Bơm Chìm S-Series
(22, 'BC-01', 'Tiện Trục Bơm Chìm Inox', 'Gia công trục bơm chìm inox 304', 1, 50.00, 6, 'admin'),
(23, 'BC-02', 'Tiện Vỏ Bơm Chìm', 'Gia công vỏ bơm chìm đúc', 1, 65.00, 6, 'admin'),
(24, 'BC-03', 'Tiện Cánh Bơm Chìm', 'Gia công cánh bơm đúc ly tâm', 2, 40.00, 6, 'admin'),
-- Line 7 — Van V-Series
(25, 'VN-01', 'Tiện Mặt Tựa Van', 'Gia công mặt tựa van chính xác', 1, 70.00, 7, 'admin'),
(26, 'VN-02', 'Tiện Trục Van', 'Gia công trục van điều khiển', 1, 55.00, 7, 'admin'),
(27, 'VN-03', 'Tiện Đế Van', 'Gia công đế van lắp bích', 2, 45.00, 7, 'admin'),
-- Line 8 — Trục Khuỷu C-Series
(28, 'TK-01', 'Tiện Cổ Trục Chính', 'Gia công cổ trục chính ±0.01', 1, 120.00, 8, 'admin'),
(29, 'TK-02', 'Tiện Cổ Biên', 'Gia công cổ biên offset', 1, 110.00, 8, 'admin'),
(30, 'TK-03', 'Mài Cổ Trục', 'Mài cổ trục Ra 0.4', 1, 90.00, 8, 'admin'),
-- Line 9 — Hàn W200
(31, 'HA-W2-01', 'Hàn MIG Thân Bơm W200', 'Hàn MIG vòng bích W200', 1, 130.00, 9, 'admin'),
(32, 'HA-W2-02', 'Hàn TIG Ống W200', 'Hàn TIG ống nội W200', 1, 160.00, 9, 'admin'),
-- Line 10 — Lắp Ráp Bơm Bùn B2
(33, 'LA-B2-01', 'Lắp Cánh Bùn B2', 'Lắp cánh bơm bùn chịu mài mòn', 1, 85.00, 10, 'admin'),
(34, 'LA-B2-02', 'Test Bơm Bùn B2', 'Test áp bùn 2x danh định', 1, 70.00, 10, 'admin'),
-- Line 11 — KCS
(35, 'QC-01', 'Kiểm Tra Ngoại Quan', 'Kiểm tra bề mặt, kích thước tổng quan', 1, 30.00, 11, 'admin'),
(36, 'QC-02', 'Kiểm Tra Chức Năng', 'Test chạy thử và đo thông số', 1, 45.00, 11, 'admin');


-- ============================================================================
-- 3. TRAINING SAMPLES cho các lines mới
-- ============================================================================

INSERT IGNORE INTO training_samples (id, training_code, category_name, process_id, product_line_id, created_by,
                                     delete_flag, created_at, updated_at)
VALUES
-- Line 6
(31, 'TS-BC-01', 'Mẫu Tiện Trục Bơm Chìm', 22, 6, 'system', 0, NOW(), NOW()),
(32, 'TS-BC-02', 'Mẫu Tiện Vỏ Bơm Chìm', 23, 6, 'system', 0, NOW(), NOW()),
(33, 'TS-BC-03', 'Mẫu Tiện Cánh Bơm Chìm', 24, 6, 'system', 0, NOW(), NOW()),
-- Line 7
(34, 'TS-VN-01', 'Mẫu Tiện Mặt Tựa Van', 25, 7, 'system', 0, NOW(), NOW()),
(35, 'TS-VN-02', 'Mẫu Tiện Trục Van', 26, 7, 'system', 0, NOW(), NOW()),
(36, 'TS-VN-03', 'Mẫu Tiện Đế Van', 27, 7, 'system', 0, NOW(), NOW()),
-- Line 9
(37, 'TS-HW2-01', 'Mẫu Hàn MIG W200', 31, 9, 'system', 0, NOW(), NOW()),
(38, 'TS-HW2-02', 'Mẫu Hàn TIG W200', 32, 9, 'system', 0, NOW(), NOW()),
-- Line 10
(39, 'TS-LB2-01', 'Mẫu Lắp Cánh Bùn B2', 33, 10, 'system', 0, NOW(), NOW()),
(40, 'TS-LB2-02', 'Mẫu Test Bơm Bùn B2', 34, 10, 'system', 0, NOW(), NOW());


-- ============================================================================
-- 4. TRAINING PLANS — WAITING_MANAGER (cho MNG Pending Approvals)
-- ============================================================================

INSERT INTO training_plans (id, form_code, title, start_date, end_date, team_id, line_id, status, current_version, note,
                            min_training_per_day, max_training_per_day, created_by)
VALUES
-- Section 1 → Group 1 (Tiện CNC)
(300, 'TP-MNG-W01', 'KH HL T3/2026 - Tiện Trục Bơm (Chờ MNG)', '2026-03-01', '2026-03-31', 1, 1, 'PENDING_APPROVAL', 1,
 'SV đã duyệt, chờ Manager.', 1, 3, 'tl_tien01'),
(301, 'TP-MNG-W02', 'KH HL T4/2026 - Bơm Chìm S (Chờ MNG)', '2026-04-01', '2026-04-30', 1, 6, 'PENDING_APPROVAL', 1,
 'SV đã duyệt, chờ Manager.', 1, 3, 'tl_tien01'),
-- Section 1 → Group 2 (Phay CNC)
(302, 'TP-MNG-W03', 'KH HL T3/2026 - Phay Vỏ Bơm (Chờ MNG)', '2026-03-01', '2026-03-31', 2, 2, 'PENDING_APPROVAL', 1,
 'SV đã duyệt, chờ Manager.', 1, 3, 'tl_phay01'),
-- Section 1 → Group 3 (Hàn)
(303, 'TP-MNG-W04', 'KH HL T3/2026 - Hàn Thân Bơm (Chờ MNG)', '2026-03-01', '2026-03-31', 3, 3, 'PENDING_APPROVAL', 1,
 'SV đã duyệt, chờ Manager.', 1, 3, 'tl_hanlap01'),
-- Section 2 → Group 4 (Lắp Ráp Bơm)
(304, 'TP-MNG-W05', 'KH HL T3/2026 - Lắp Ráp Máy Bơm (Chờ MNG)', '2026-03-01', '2026-03-31', 4, 5, 'PENDING_APPROVAL',
 1, 'SV đã duyệt, chờ Manager.', 1, 3, 'tl_laprap01'),
-- Section 2 → Group 5 (Lắp Ráp Động Cơ)
(305, 'TP-MNG-W06', 'KH HL T3/2026 - Lắp Ráp Động Cơ (Chờ MNG)', '2026-03-01', '2026-03-31', 5, 4, 'PENDING_APPROVAL',
 1, 'SV đã duyệt, chờ Manager.', 1, 3, 'tl_dongco01');


-- ============================================================================
-- 5. DEFECT PROPOSALS — WAITING_MANAGER (cho MNG Pending Approvals)
-- ============================================================================

INSERT INTO defect_proposals (id, form_code, status, product_line_id, current_version, created_by, delete_flag,
                              created_at, updated_at)
VALUES (100, 'DP-MNG-001', 'PENDING_APPROVAL', 1, 1, 'tl_tien01', 0, DATE_SUB(NOW(), INTERVAL 2 HOUR),
        DATE_SUB(NOW(), INTERVAL 2 HOUR)),
       (101, 'DP-MNG-002', 'PENDING_APPROVAL', 2, 1, 'tl_phay01', 0, DATE_SUB(NOW(), INTERVAL 30 HOUR),
        DATE_SUB(NOW(), INTERVAL 30 HOUR)),
       (102, 'DP-MNG-003', 'PENDING_APPROVAL', 3, 1, 'tl_hanlap01', 0, DATE_SUB(NOW(), INTERVAL 5 HOUR),
        DATE_SUB(NOW(), INTERVAL 5 HOUR)),
       (103, 'DP-MNG-004', 'PENDING_APPROVAL', 5, 1, 'tl_laprap01', 0, DATE_SUB(NOW(), INTERVAL 48 HOUR),
        DATE_SUB(NOW(), INTERVAL 48 HOUR)),
       (104, 'DP-MNG-005', 'PENDING_APPROVAL', 4, 1, 'tl_dongco01', 0, DATE_SUB(NOW(), INTERVAL 10 HOUR),
        DATE_SUB(NOW(), INTERVAL 10 HOUR));


-- ============================================================================
-- 6. TRAINING SAMPLE PROPOSALS — WAITING_MANAGER (cho MNG Pending Approvals)
-- ============================================================================

INSERT INTO training_sample_proposals (id, form_code, status, product_line_id, current_version, created_by, delete_flag,
                                       created_at, updated_at)
VALUES (100, 'TSP-MNG-001', 'PENDING_APPROVAL', 1, 1, 'tl_tien01', 0, DATE_SUB(NOW(), INTERVAL 3 HOUR),
        DATE_SUB(NOW(), INTERVAL 3 HOUR)),
       (101, 'TSP-MNG-002', 'PENDING_APPROVAL', 2, 1, 'tl_phay01', 0, DATE_SUB(NOW(), INTERVAL 26 HOUR),
        DATE_SUB(NOW(), INTERVAL 26 HOUR)),
       (102, 'TSP-MNG-003', 'PENDING_APPROVAL', 5, 1, 'tl_laprap01', 0, DATE_SUB(NOW(), INTERVAL 6 HOUR),
        DATE_SUB(NOW(), INTERVAL 6 HOUR));


-- ============================================================================
-- 7. TRAINING RESULTS + DETAILS cho Section 2 & 3 lines
--    (Để MNG Training Progress có dữ liệu từ nhiều lines)
-- ============================================================================

-- Results cho Group 3 (Hàn), Group 4 (Lắp Bơm), Group 5 (Lắp ĐC)
INSERT INTO training_results (id, training_plan_id, title, form_code, year, team_id, line_id, status, current_version,
                              note, created_by)
VALUES
-- Group 3 (Hàn, line 3)
(300, NULL, 'KQ HL T3/2026 - Hàn Bơm W100', 'TR-MNG-01', 2026, 3, 3, 'COMPLETED', 1, 'Đã hoàn thành.', 'tl_hanlap01'),
-- Group 4 (Lắp Bơm, line 5)
(301, NULL, 'KQ HL T3/2026 - Lắp Ráp Bơm B', 'TR-MNG-02', 2026, 4, 5, 'COMPLETED', 1, 'Đã hoàn thành.', 'tl_laprap01'),
-- Group 5 (Lắp ĐC, line 4)
(302, NULL, 'KQ HL T3/2026 - Lắp Ráp ĐC E', 'TR-MNG-03', 2026, 5, 4, 'ONGOING', 1, 'Đang thực hiện.', 'tl_dongco01'),
-- Group 3 (Hàn W200, line 9)
(303, NULL, 'KQ HL T2/2026 - Hàn W200', 'TR-MNG-04', 2026, 3, 9, 'COMPLETED', 1, 'Đã hoàn thành.', 'tl_hanlap01');


-- Result Details — rải Feb & Mar 2026
INSERT INTO training_result_details (training_result_id, employee_id, training_sample_id, planned_date, actual_date,
                                     status, is_pass, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- Result 300 (Hàn, line 3) — 8 details
(300, 13, NULL, '2026-02-05', '2026-02-05', 'COMPLETED', TRUE, 'Feb - NV013 pass Hàn MIG', 'tl_hanlap01', 0, NOW(),
 NOW()),
(300, 14, NULL, '2026-02-10', '2026-02-10', 'COMPLETED', TRUE, 'Feb - NV014 pass Hàn TIG', 'tl_hanlap01', 0, NOW(),
 NOW()),
(300, 15, NULL, '2026-02-15', '2026-02-15', 'COMPLETED', FALSE, 'Feb - NV015 fail Hàn Điểm', 'tl_hanlap01', 0, NOW(),
 NOW()),
(300, 16, NULL, '2026-02-20', '2026-02-20', 'COMPLETED', TRUE, 'Feb - NV016 pass', 'tl_hanlap01', 0, NOW(), NOW()),
(300, 13, NULL, '2026-03-05', '2026-03-05', 'COMPLETED', TRUE, 'Mar - NV013 pass', 'tl_hanlap01', 0, NOW(), NOW()),
(300, 14, NULL, '2026-03-10', '2026-03-10', 'COMPLETED', TRUE, 'Mar - NV014 pass', 'tl_hanlap01', 0, NOW(), NOW()),
(300, 15, NULL, '2026-03-15', '2026-03-15', 'COMPLETED', TRUE, 'Mar - NV015 pass lần 2', 'tl_hanlap01', 0, NOW(),
 NOW()),
(300, 16, NULL, '2026-03-20', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV016 chưa HL', 'tl_hanlap01', 0, NOW(), NOW()),

-- Result 301 (Lắp Bơm, line 5) — 8 details
(301, 17, NULL, '2026-02-03', '2026-02-03', 'COMPLETED', TRUE, 'Feb - NV017 pass Lắp BCT', 'tl_laprap01', 0, NOW(),
 NOW()),
(301, 18, NULL, '2026-02-08', '2026-02-08', 'COMPLETED', FALSE, 'Feb - NV018 fail Ron', 'tl_laprap01', 0, NOW(), NOW()),
(301, 19, NULL, '2026-02-14', '2026-02-14', 'COMPLETED', TRUE, 'Feb - NV019 pass Test Áp', 'tl_laprap01', 0, NOW(),
 NOW()),
(301, 20, NULL, '2026-02-20', '2026-02-20', 'COMPLETED', TRUE, 'Feb - NV020 pass', 'tl_laprap01', 0, NOW(), NOW()),
(301, 17, NULL, '2026-03-03', '2026-03-03', 'COMPLETED', TRUE, 'Mar - NV017 pass', 'tl_laprap01', 0, NOW(), NOW()),
(301, 18, NULL, '2026-03-08', '2026-03-08', 'COMPLETED', TRUE, 'Mar - NV018 pass lần 2', 'tl_laprap01', 0, NOW(),
 NOW()),
(301, 19, NULL, '2026-03-15', '2026-03-15', 'COMPLETED', FALSE, 'Mar - NV019 fail', 'tl_laprap01', 0, NOW(), NOW()),
(301, 20, NULL, '2026-03-22', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV020 chưa HL', 'tl_laprap01', 0, NOW(), NOW()),

-- Result 302 (Lắp ĐC, line 4) — 8 details
(302, 22, NULL, '2026-02-05', '2026-02-05', 'COMPLETED', TRUE, 'Feb - NV022 pass Lắp Piston', 'tl_dongco01', 0, NOW(),
 NOW()),
(302, 23, NULL, '2026-02-12', '2026-02-12', 'COMPLETED', TRUE, 'Feb - NV023 pass Nắp Máy', 'tl_dongco01', 0, NOW(),
 NOW()),
(302, 24, NULL, '2026-02-18', '2026-02-18', 'COMPLETED', FALSE, 'Feb - NV024 fail Cân Bằng', 'tl_dongco01', 0, NOW(),
 NOW()),
(302, 25, NULL, '2026-02-25', '2026-02-25', 'COMPLETED', TRUE, 'Feb - NV025 pass', 'tl_dongco01', 0, NOW(), NOW()),
(302, 22, NULL, '2026-03-03', '2026-03-03', 'COMPLETED', TRUE, 'Mar - NV022 pass', 'tl_dongco01', 0, NOW(), NOW()),
(302, 23, NULL, '2026-03-10', '2026-03-10', 'COMPLETED', TRUE, 'Mar - NV023 pass', 'tl_dongco01', 0, NOW(), NOW()),
(302, 24, NULL, '2026-03-18', '2026-03-18', 'COMPLETED', FALSE, 'Mar - NV024 fail lần 2', 'tl_dongco01', 0, NOW(),
 NOW()),
(302, 26, NULL, '2026-03-22', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV026 chưa HL', 'tl_dongco01', 0, NOW(), NOW()),

-- Result 303 (Hàn W200, line 9) — 6 details
(303, 13, 37, '2026-02-10', '2026-02-10', 'COMPLETED', TRUE, 'Feb - NV013 pass Hàn W200', 'tl_hanlap01', 0, NOW(),
 NOW()),
(303, 14, 38, '2026-02-18', '2026-02-18', 'COMPLETED', TRUE, 'Feb - NV014 pass TIG W200', 'tl_hanlap01', 0, NOW(),
 NOW()),
(303, 15, 37, '2026-03-05', '2026-03-05', 'COMPLETED', TRUE, 'Mar - NV015 pass', 'tl_hanlap01', 0, NOW(), NOW()),
(303, 16, 38, '2026-03-12', '2026-03-12', 'COMPLETED', FALSE, 'Mar - NV016 fail TIG W200', 'tl_hanlap01', 0, NOW(),
 NOW()),
(303, 13, 37, '2026-03-18', '2026-03-18', 'COMPLETED', TRUE, 'Mar - NV013 pass lần 2', 'tl_hanlap01', 0, NOW(), NOW()),
(303, 14, 38, '2026-03-25', NULL, 'PENDING_REVIEW', NULL, 'Mar - NV014 chưa HL', 'tl_hanlap01', 0, NOW(), NOW());


-- ============================================================================
-- 8. DEFECTS cho Section 2 & 3 lines — phong phú MNG defect trend
-- ============================================================================

INSERT INTO defects (defect_code, defect_description, process_id, detected_date, defect_type, origin_measures,
                     outflow_measures, conclusion, note, created_by, delete_flag, created_at, updated_at)
VALUES
-- Line 3 (Hàn W-Series) — tháng 2 & 3/2026
('MNG-D01', 'Rỗ khí mối hàn MIG thân bơm W100 lô tháng 2', 10, '2026-02-08', 'CLAIM', 'M1', 'M2', 'Điều chỉnh khí',
 'MNG mock', 'system', 0, NOW(), NOW()),
('MNG-D02', 'Nứt mối hàn TIG ống nội W100 tháng 2', 11, '2026-02-15', 'CLAIM', 'M1', 'M2', 'Preheat 150°C', 'MNG mock',
 'system', 0, NOW(), NOW()),
('MNG-D03', 'Biến dạng nhiệt thân bơm tháng 3', 10, '2026-03-05', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Thêm jig', 'MNG mock',
 'system', 0, NOW(), NOW()),
('MNG-D04', 'Hàn điểm giá đỡ bong tháng 3', 12, '2026-03-12', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Tăng dòng', 'MNG mock',
 'system', 0, NOW(), NOW()),
-- Line 5 (Lắp Bơm B-Series)
('MNG-D05', 'Ron cao su rách khi test áp tháng 2', 19, '2026-02-10', 'CLAIM', 'M1', 'M2', 'Đồ gá mới', 'MNG mock',
 'system', 0, NOW(), NOW()),
('MNG-D06', 'Bánh CK lắp ngược gây rung tháng 3', 18, '2026-03-08', 'CLAIM', 'M1', 'M2', 'Marking hướng', 'MNG mock',
 'system', 0, NOW(), NOW()),
('MNG-D07', 'Test áp thất bại mặt bích tháng 3', 20, '2026-03-18', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Kiểm ren',
 'MNG mock', 'system', 0, NOW(), NOW()),
-- Line 4 (Lắp Động Cơ E-Series)
('MNG-D08', 'Piston lắp ngược tháng 2', 14, '2026-02-12', 'CLAIM', 'M1', 'M2', 'Poka-yoke', 'MNG mock', 'system', 0,
 NOW(), NOW()),
('MNG-D09', 'Moment bu-lông nắp máy không đạt tháng 3', 15, '2026-03-10', 'CLAIM', 'M1', 'M2', 'Hiệu chuẩn', 'MNG mock',
 'system', 0, NOW(), NOW()),
('MNG-D10', 'Áp suất dầu < 3bar khi test tháng 3', 17, '2026-03-20', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Bơm dầu mòn',
 'MNG mock', 'system', 0, NOW(), NOW()),
-- Line 9 (Hàn W200)
('MNG-D11', 'Rỗ khí hàn MIG W200 tháng 2', 31, '2026-02-20', 'CLAIM', 'M1', 'M2', 'Kiểm khí', 'MNG mock', 'system', 0,
 NOW(), NOW()),
('MNG-D12', 'Nứt hàn TIG W200 tháng 3', 32, '2026-03-15', 'DEFECTIVE_GOODS', 'M1', 'M2', 'Vật liệu sai', 'MNG mock',
 'system', 0, NOW(), NOW());


-- ============================================================================
-- 9. EMPLOYEE SKILLS bổ sung cho Section 2 & 3 teams — Coverage data
-- ============================================================================

INSERT IGNORE INTO employee_skills (employee_id, process_id, status, certified_date, expiry_date, created_by,
                                    delete_flag, created_at, updated_at)
VALUES
-- Tổ Hàn (team 3, emp 13-16) — processes 10-13
(13, 10, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(13, 11, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(14, 10, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(14, 11, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 12 DAY), 'system_mock', 0, NOW(), NOW()),
(15, 12, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(15, 13, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(16, 10, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(16, 12, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Tổ Lắp Bơm (team 4, emp 17-21) — processes 18-21
(17, 18, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(17, 19, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(18, 18, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 7 DAY), 'system_mock', 0, NOW(), NOW()),
(18, 20, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(19, 19, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(19, 20, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(20, 18, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(20, 21, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Tổ Lắp ĐC (team 5, emp 22-26) — processes 14-17
(22, 14, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(22, 15, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(23, 14, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(23, 16, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 14 DAY), 'system_mock', 0, NOW(), NOW()),
(24, 15, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(24, 17, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(25, 14, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(25, 16, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(26, 17, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
-- Tổ KCS (team 6, emp 27-30) — processes 35-36
(27, 35, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(27, 36, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(28, 35, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW()),
(28, 36, 'PENDING_REVIEW', '2025-03-01', DATE_ADD(@today, INTERVAL 5 DAY), 'system_mock', 0, NOW(), NOW()),
(29, 35, 'REVOKED', '2025-01-01', '2026-01-01', 'system_mock', 0, NOW(), NOW()),
(30, 36, 'VALID', '2024-06-01', '2027-06-01', 'system_mock', 0, NOW(), NOW());


SET FOREIGN_KEY_CHECKS = 1;
