
-- 1) Mapping external_employee_id -> internal employees.id
--    Why needed?
--      External employeeId is NOT your employees.id.
--      This table lets you join external schedules to your employee table reliably.
-- ----------------------------------------------------------------------------
CREATE TABLE external_employee_mappings (
                                            id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Identify which external system this mapping belongs to
                                            source_system VARCHAR(64) NOT NULL,

    -- Employee id from external system (string for compatibility)
                                            external_employee_id VARCHAR(64) NOT NULL,

    -- Internal FK to employees table
                                            employee_id BIGINT NOT NULL,

    -- Active mapping flag (disable mapping without deleting it)
                                            active BOOLEAN NOT NULL DEFAULT TRUE,

    -- Soft delete + audit fields
                                            delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                            created_by VARCHAR(255),
                                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                            updated_by VARCHAR(255),

    -- Prevent duplicate mapping for same external system + external employee id
                                            UNIQUE KEY uk_external_employee_mapping (source_system, external_employee_id),

    -- Useful index when debugging schedule -> employee linkage
                                            INDEX idx_external_employee_mappings_employee (employee_id),
                                            INDEX idx_external_employee_mappings_delete_flag (delete_flag),

                                            CONSTRAINT fk_external_employee_mappings_employee
                                                FOREIGN KEY (employee_id) REFERENCES employees (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 2) Sync batches: each time your job calls external API, create a batch row
--    Why needed?
--      - Audit & monitoring
--      - Reprocess a specific batch
--      - Track statistics (insert/update/fail)
-- ----------------------------------------------------------------------------
CREATE TABLE external_schedule_sync_batches (
                                                id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Which external system
                                                source_system VARCHAR(64) NOT NULL,
                                                source_endpoint VARCHAR(255),
                                                source_version VARCHAR(32),

    -- Timezone for interpreting external times
                                                timezone VARCHAR(64) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',

    -- Sync scope: you said "sync by product line (dây chuyền)"
                                                product_line_id BIGINT NOT NULL,
                                                date_from DATE NOT NULL,
                                                date_to DATE NOT NULL,

    -- When the sync started/ended
                                                requested_at TIMESTAMP NULL,
                                                finished_at TIMESTAMP NULL,

    -- Batch run status
                                                status ENUM('RUNNING','SUCCESS','PARTIAL_FAILED','FAILED') NOT NULL DEFAULT 'RUNNING',

    -- Stats
                                                total_received INT NOT NULL DEFAULT 0,
                                                total_inserted INT NOT NULL DEFAULT 0,
                                                total_updated INT NOT NULL DEFAULT 0,
                                                total_failed INT NOT NULL DEFAULT 0,

    -- Store batch-level error (e.g., API failed)
                                                error_message TEXT NULL,

    -- Soft delete + audit
                                                delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                created_by VARCHAR(255),
                                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                                updated_by VARCHAR(255),

                                                INDEX idx_esb_line_date (product_line_id, date_from, date_to),
                                                INDEX idx_esb_status (status),
                                                INDEX idx_esb_delete_flag (delete_flag),

                                                CONSTRAINT fk_esb_product_line
                                                    FOREIGN KEY (product_line_id) REFERENCES product_lines(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 3) Raw records: store raw JSON per employee/day per batch
--    Why needed?
--      - "Lưu hết tất cả" fields even if you don't normalize them yet
--      - Debug differences between external and internal
--      - Reprocess without calling external API again
--
-- Unique key:
--   (sync_batch_id, external_employee_id, work_date) because no sourceScheduleId
-- ----------------------------------------------------------------------------
CREATE TABLE external_schedule_raw_records (
                                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                               sync_batch_id BIGINT NOT NULL,

                                               external_employee_id VARCHAR(64) NOT NULL,
                                               work_date DATE NOT NULL,

    -- quick filter columns extracted from raw_json (optional but useful)
                                               role_code VARCHAR(50),
                                               day_type VARCHAR(50),
                                               shift_code VARCHAR(20),

    -- entire record from external system
                                               raw_json JSON NOT NULL,

    -- processing status
                                               processed BOOLEAN NOT NULL DEFAULT FALSE,
                                               processed_at TIMESTAMP NULL,
                                               process_error TEXT NULL,

                                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                               UNIQUE KEY uk_raw_batch_emp_date (sync_batch_id, external_employee_id, work_date),
                                               INDEX idx_raw_batch (sync_batch_id),
                                               INDEX idx_raw_processed (processed),

                                               CONSTRAINT fk_raw_batch
                                                   FOREIGN KEY (sync_batch_id) REFERENCES external_schedule_sync_batches(id)
                                                       ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 4) Normalized daily schedule header (1 row per employee per day)
--    Why store header?
--      - Fast query by product line/team/date
--      - Aggregation for training planning (working days, days off, available capacity)
--      - Keep a "summary" even when there are multiple segments
--
-- Key choice:
--   UNIQUE(employee_id, work_date) -> Upsert without sourceScheduleId.
-- ----------------------------------------------------------------------------
CREATE TABLE work_schedules (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,

                                work_date DATE NOT NULL,

    -- Denormalized "scope at that date"
    -- (because employee may move teams/lines over time; schedule must represent reality at that date)
                                product_line_id BIGINT NOT NULL,
                                team_id BIGINT NOT NULL,

    -- FK to internal employee table
                                employee_id BIGINT NOT NULL,

    -- Trace external source
                                source_system VARCHAR(64) NOT NULL,
                                external_employee_id VARCHAR(64) NOT NULL,

    -- Link to batch & raw record for auditing
                                sync_batch_id BIGINT NULL,
                                raw_record_id BIGINT NULL,

    -- Flexible role/day/attendance fields (avoid ENUM lock-in)
                                role_code VARCHAR(50) NULL,            -- e.g., EMPLOYEE, ROLE_TEAM_LEADER, MANAGER...
                                day_type VARCHAR(50) NOT NULL,         -- e.g., WORKING_DAY, HOLIDAY, ANNUAL_LEAVE...
                                attendance_status VARCHAR(50) NOT NULL,-- e.g., PLANNED, APPROVED, ACTUAL...

    -- Shift summary at day level (detailed segments can override)
                                shift_code VARCHAR(20) NOT NULL,       -- e.g., DAY, NIGHT, OFF
                                shift_start_time TIME NULL,
                                shift_end_time TIME NULL,
                                break_minutes INT NOT NULL DEFAULT 0,
                                is_overnight BOOLEAN NOT NULL DEFAULT FALSE,

    -- Minutes summary
                                planned_minutes INT NOT NULL DEFAULT 0,
                                actual_minutes INT NULL,
                                ot_minutes_planned INT NOT NULL DEFAULT 0,
                                ot_minutes_approved INT NOT NULL DEFAULT 0,
                                late_minutes INT NOT NULL DEFAULT 0,
                                early_leave_minutes INT NOT NULL DEFAULT 0,

    -- Holiday summary (nullable)
                                holiday_code VARCHAR(50) NULL,
                                holiday_name VARCHAR(100) NULL,

    -- Leave summary (nullable)
                                leave_type VARCHAR(50) NULL,
                                leave_is_paid BOOLEAN NULL,
                                leave_minutes INT NULL,
                                leave_approval_status VARCHAR(20) NULL,
                                leave_reason VARCHAR(255) NULL,

                                note VARCHAR(255) NULL,

    -- tags / extra attributes at day-level
                                tags_json JSON NULL,

    -- Soft delete + audit
                                delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                created_by VARCHAR(255),
                                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                updated_by VARCHAR(255),

    -- Upsert natural key for the day
                                UNIQUE KEY uk_ws_emp_date (employee_id, work_date),

                                INDEX idx_ws_line_date (product_line_id, work_date),
                                INDEX idx_ws_team_date (team_id, work_date),
                                INDEX idx_ws_employee_date (employee_id, work_date),
                                INDEX idx_ws_external_emp_date (source_system, external_employee_id, work_date),
                                INDEX idx_ws_day_type (day_type),
                                INDEX idx_ws_shift_code (shift_code),
                                INDEX idx_ws_delete_flag (delete_flag),

                                CONSTRAINT fk_ws_employee
                                    FOREIGN KEY (employee_id) REFERENCES employees(id),

                                CONSTRAINT fk_ws_team
                                    FOREIGN KEY (team_id) REFERENCES teams(id),

                                CONSTRAINT fk_ws_product_line
                                    FOREIGN KEY (product_line_id) REFERENCES product_lines(id),

                                CONSTRAINT fk_ws_batch
                                    FOREIGN KEY (sync_batch_id) REFERENCES external_schedule_sync_batches(id),

                                CONSTRAINT fk_ws_raw_record
                                    FOREIGN KEY (raw_record_id) REFERENCES external_schedule_raw_records(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- 5) Segments inside a day (required by your request)
--    Use cases:
--      - Half-day leave (morning work, afternoon leave)
--      - Split shifts
--      - Move to other team/line within the day
--
-- Important:
--   - Segment times are TIME only. If a segment crosses midnight, you must define a convention:
--        * either store is_overnight in segment_tags_json, OR
--        * interpret end_time < start_time as overnight.
--   - If you want strict support for cross-date segments, add start_datetime/end_datetime instead.
-- ----------------------------------------------------------------------------
CREATE TABLE work_schedule_segments (
                                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                        work_schedule_id BIGINT NOT NULL,

                                        segment_no INT NOT NULL,               -- 1.n chronological within day

                                        start_time TIME NOT NULL,
                                        end_time TIME NOT NULL,

    -- Optional override scope per segment (if moved during day)
                                        product_line_id BIGINT NULL,
                                        team_id BIGINT NULL,

    -- Segment classification (flexible)
                                        segment_type VARCHAR(30) NOT NULL,     -- WORK / LEAVE / HOLIDAY / TRAINING / OTHER
                                        role_code VARCHAR(50) NULL,

    -- Segment-level shift & minutes
                                        shift_code VARCHAR(20) NULL,
                                        planned_minutes INT NULL,
                                        actual_minutes INT NULL,
                                        ot_minutes INT NULL,

    -- Leave detail per segment (if segment_type=LEAVE)
                                        leave_type VARCHAR(50) NULL,
                                        leave_is_paid BOOLEAN NULL,
                                        leave_minutes INT NULL,
                                        leave_approval_status VARCHAR(20) NULL,
                                        leave_reason VARCHAR(255) NULL,

    -- Holiday detail per segment (rare but supported)
                                        holiday_code VARCHAR(50) NULL,
                                        holiday_name VARCHAR(100) NULL,

    -- For training planning: process/workstation at segment level (optional)
                                        process_ids_json JSON NULL,            -- e.g., ["OP1","OP2"]
                                        workstation_id VARCHAR(64) NULL,

                                        note VARCHAR(255) NULL,
                                        tags_json JSON NULL,

                                        delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                        created_by VARCHAR(255),
                                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                        updated_by VARCHAR(255),

                                        UNIQUE KEY uk_wss_ws_seg (work_schedule_id, segment_no),

                                        INDEX idx_wss_ws (work_schedule_id),
                                        INDEX idx_wss_team (team_id),
                                        INDEX idx_wss_line (product_line_id),
                                        INDEX idx_wss_segment_type (segment_type),
                                        INDEX idx_wss_delete_flag (delete_flag),

                                        CONSTRAINT fk_wss_ws
                                            FOREIGN KEY (work_schedule_id) REFERENCES work_schedules(id)
                                                ON DELETE CASCADE,

                                        CONSTRAINT fk_wss_team
                                            FOREIGN KEY (team_id) REFERENCES teams(id),

                                        CONSTRAINT fk_wss_product_line
                                            FOREIGN KEY (product_line_id) REFERENCES product_lines(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SAMPLE DATA for:
--  - external_employee_mappings
--  - external_schedule_sync_batches
--  - external_schedule_raw_records
--  - work_schedules
--  - work_schedule_segments
--
-- Notes:
--  - These inserts assume you ALREADY have:
--      * product_lines row with id = 1
--      * teams row with id = 1 (belongs to product line/group depending on your schema)
--      * employees rows with ids = 1001, 1002
--  - If your IDs differ, change the values below accordingly.
--  - This file is meant as a reference / sample, not guaranteed to run without adapting IDs.
-- ============================================================================

START TRANSACTION;

-- ----------------------------------------------------------------------------
-- 0) Assumed existing master data (NOT inserted here):
--    product_lines: id=1
--    teams:         id=1
--    employees:     id=1001 (EMPLOYEE), id=1002 (ROLE_TEAM_LEADER or manager role)
-- ----------------------------------------------------------------------------

-- ----------------------------------------------------------------------------
-- 1) Create a sync batch
-- ----------------------------------------------------------------------------
INSERT INTO external_schedule_sync_batches (
    source_system,
    source_endpoint,
    source_version,
    timezone,
    product_line_id,
    date_from,
    date_to,
    requested_at,
    finished_at,
    status,
    total_received,
    total_inserted,
    total_updated,
    total_failed,
    error_message,
    delete_flag,
    created_by,
    updated_by
) VALUES (
             'EXT_HRM',
             '/api/v1/schedules/export',
             'v1',
             'Asia/Ho_Chi_Minh',
             1,
             '2026-01-01',
             '2026-01-03',
             '2026-03-03 09:00:00',
             '2026-03-03 09:00:05',
             'SUCCESS',
             4,
             4,
             0,
             0,
             NULL,
             FALSE,
             'system',
             'system'
         );

-- Capture batch id for later inserts (MySQL session variable)
SET @sync_batch_id := LAST_INSERT_ID();

-- ----------------------------------------------------------------------------
-- 2) Map external_employee_id -> internal employees.id
-- ----------------------------------------------------------------------------
INSERT INTO external_employee_mappings (
    source_system,
    external_employee_id,
    employee_id,
    active,
    delete_flag,
    created_by,
    updated_by
) VALUES
      ('EXT_HRM', 'EXT-E-1', 1, TRUE, FALSE, 'system', 'system'),
      ('EXT_HRM', 'EXT-E-2', 2, TRUE, FALSE, 'system', 'system');

-- ----------------------------------------------------------------------------
-- 3) Insert RAW external records (employee/day)
-- ----------------------------------------------------------------------------

-- 3.1 Employee 1001 - Working day with 2 segments (morning WORK, afternoon WORK)
INSERT INTO external_schedule_raw_records (
    sync_batch_id,
    external_employee_id,
    work_date,
    role_code,
    day_type,
    shift_code,
    raw_json,
    processed,
    processed_at,
    process_error
) VALUES (
             @sync_batch_id,
             'EXT-E-00001',
             '2026-01-01',
             'EMPLOYEE',
             'WORKING_DAY',
             'DAY',
             JSON_OBJECT(
                     'externalEmployeeId','EXT-E-00001',
                     'date','2026-01-01',
                     'role','EMPLOYEE',
                     'dayType','WORKING_DAY',
                     'shift', JSON_OBJECT('code','DAY','start','08:00','end','17:00','breakMinutes',60),
                     'segments', JSON_ARRAY(
                             JSON_OBJECT('segmentNo',1,'type','WORK','start','08:00','end','12:00','processIds', JSON_ARRAY('OP1','OP2')),
                             JSON_OBJECT('segmentNo',2,'type','WORK','start','13:00','end','17:00','processIds', JSON_ARRAY('OP3'))
                                 )
             ),
             TRUE,
             '2026-03-03 09:00:02',
             NULL
         );

SET @raw_id_emp1_d1 := LAST_INSERT_ID();

-- 3.2 Employee 1001 - Half-day annual leave (morning WORK, afternoon LEAVE)
INSERT INTO external_schedule_raw_records (
    sync_batch_id,
    external_employee_id,
    work_date,
    role_code,
    day_type,
    shift_code,
    raw_json,
    processed,
    processed_at,
    process_error
) VALUES (
             @sync_batch_id,
             'EXT-E-1',
             '2026-01-02',
             'EMPLOYEE',
             'HALF_DAY_ANNUAL_LEAVE',
             'DAY',
             JSON_OBJECT(
                     'externalEmployeeId','EXT-E-00001',
                     'date','2026-01-02',
                     'role','EMPLOYEE',
                     'dayType','HALF_DAY_ANNUAL_LEAVE',
                     'shift', JSON_OBJECT('code','DAY','start','08:00','end','17:00','breakMinutes',60),
                     'segments', JSON_ARRAY(
                             JSON_OBJECT('segmentNo',1,'type','WORK','start','08:00','end','12:00','processIds', JSON_ARRAY('OP1')),
                             JSON_OBJECT('segmentNo',2,'type','LEAVE','leaveType','ANNUAL_LEAVE','isPaid',TRUE,'start','13:00','end','17:00','leaveMinutes',240,'approvalStatus','APPROVED','reason','Nghỉ nửa ngày')
                                 )
             ),
             TRUE,
             '2026-03-03 09:00:02',
             NULL
         );

SET @raw_id_emp1_d2 := LAST_INSERT_ID();

-- 3.3 Employee 1002 (Team leader/manager role) - Holiday (no work)
INSERT INTO external_schedule_raw_records (
    sync_batch_id,
    external_employee_id,
    work_date,
    role_code,
    day_type,
    shift_code,
    raw_json,
    processed,
    processed_at,
    process_error
) VALUES (
             @sync_batch_id,
             'EXT-E-2',
             '2026-01-01',
             'ROLE_TEAM_LEADER',
             'HOLIDAY',
             'OFF',
             JSON_OBJECT(
                     'externalEmployeeId','EXT-E-00002',
                     'date','2026-01-01',
                     'role','ROLE_TEAM_LEADER',
                     'dayType','HOLIDAY',
                     'holiday', JSON_OBJECT('code','NEW_YEAR','name','Tết dương lịch'),
                     'shift', JSON_OBJECT('code','OFF'),
                     'segments', JSON_ARRAY(
                             JSON_OBJECT('segmentNo',1,'type','HOLIDAY','start','00:00','end','23:59','holidayCode','NEW_YEAR','holidayName','Tết dương lịch')
                                 )
             ),
             TRUE,
             '2026-03-03 09:00:02',
             NULL
         );

SET @raw_id_emp2_d1 := LAST_INSERT_ID();

-- 3.4 Employee 1002 - Night shift crosses midnight (segment end < start)
INSERT INTO external_schedule_raw_records (
    sync_batch_id,
    external_employee_id,
    work_date,
    role_code,
    day_type,
    shift_code,
    raw_json,
    processed,
    processed_at,
    process_error
) VALUES (
             @sync_batch_id,
             'EXT-E-2',
             '2026-01-03',
             'ROLE_TEAM_LEADER',
             'WORKING_DAY',
             'NIGHT',
             JSON_OBJECT(
                     'externalEmployeeId','EXT-E-00002',
                     'date','2026-01-03',
                     'role','ROLE_TEAM_LEADER',
                     'dayType','WORKING_DAY',
                     'shift', JSON_OBJECT('code','NIGHT','start','20:00','end','05:00','breakMinutes',60,'isOvernight',TRUE),
                     'segments', JSON_ARRAY(
                             JSON_OBJECT('segmentNo',1,'type','WORK','start','20:00','end','05:00','shiftCode','NIGHT','plannedMinutes',480,'otMinutes',60,'tags', JSON_ARRAY('OVERNIGHT'))
                                 )
             ),
             TRUE,
             '2026-03-03 09:00:02',
             NULL
         );

SET @raw_id_emp2_d3 := LAST_INSERT_ID();

-- ----------------------------------------------------------------------------
-- 4) Insert NORMALIZED work_schedules (linked to product_lines/teams/employees + raw/batch)
-- ----------------------------------------------------------------------------

-- Employee 1001 - 2026-01-01 working day
INSERT INTO work_schedules (
    work_date,
    product_line_id,
    team_id,
    employee_id,
    source_system,
    external_employee_id,
    sync_batch_id,
    raw_record_id,
    role_code,
    day_type,
    attendance_status,
    shift_code,
    shift_start_time,
    shift_end_time,
    break_minutes,
    is_overnight,
    planned_minutes,
    actual_minutes,
    ot_minutes_planned,
    ot_minutes_approved,
    late_minutes,
    early_leave_minutes,
    holiday_code,
    holiday_name,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES (
             '2026-01-01',
             1,
             1,
             1,
             'EXT_HRM',
             'EXT-E-1',
             @sync_batch_id,
             @raw_id_emp1_d1,
             'EMPLOYEE',
             'WORKING_DAY',
             'APPROVED',
             'DAY',
             '08:00:00',
             '17:00:00',
             60,
             FALSE,
             480,
             NULL,
             0,
             0,
             0,
             0,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             'Ca ngày thường',
             JSON_ARRAY('AUTO_SYNC'),
             FALSE,
             'system',
             'system'
         );

SET @ws_emp1_d1 := LAST_INSERT_ID();

-- Employee 1001 - 2026-01-02 half-day annual leave summary at header
INSERT INTO work_schedules (
    work_date,
    product_line_id,
    team_id,
    employee_id,
    source_system,
    external_employee_id,
    sync_batch_id,
    raw_record_id,
    role_code,
    day_type,
    attendance_status,
    shift_code,
    shift_start_time,
    shift_end_time,
    break_minutes,
    is_overnight,
    planned_minutes,
    actual_minutes,
    ot_minutes_planned,
    ot_minutes_approved,
    late_minutes,
    early_leave_minutes,
    holiday_code,
    holiday_name,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES (
             '2026-01-02',
             1,
             1,
             1,
             'EXT_HRM',
             'EXT-E-1',
             @sync_batch_id,
             @raw_id_emp1_d2,
             'EMPLOYEE',
             'HALF_DAY_ANNUAL_LEAVE',
             'APPROVED_LEAVE',
             'DAY',
             '08:00:00',
             '17:00:00',
             60,
             FALSE,
             240,
             NULL,
             0,
             0,
             0,
             0,
             NULL,
             NULL,
             'ANNUAL_LEAVE',
             TRUE,
             240,
             'APPROVED',
             'Nghỉ nửa ngày',
             NULL,
             JSON_ARRAY('AUTO_SYNC','LEAVE_HALF_DAY'),
             FALSE,
             'system',
             'system'
         );

SET @ws_emp1_d2 := LAST_INSERT_ID();

-- Employee 1002 - 2026-01-01 holiday
INSERT INTO work_schedules (
    work_date,
    product_line_id,
    team_id,
    employee_id,
    source_system,
    external_employee_id,
    sync_batch_id,
    raw_record_id,
    role_code,
    day_type,
    attendance_status,
    shift_code,
    shift_start_time,
    shift_end_time,
    break_minutes,
    is_overnight,
    planned_minutes,
    actual_minutes,
    ot_minutes_planned,
    ot_minutes_approved,
    late_minutes,
    early_leave_minutes,
    holiday_code,
    holiday_name,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES (
             '2026-01-01',
             1,
             1,
             2,
             'EXT_HRM',
             'EXT-E-2',
             @sync_batch_id,
             @raw_id_emp2_d1,
             'ROLE_TEAM_LEADER',
             'HOLIDAY',
             'HOLIDAY',
             'OFF',
             NULL,
             NULL,
             0,
             FALSE,
             0,
             0,
             0,
             0,
             0,
             0,
             'NEW_YEAR',
             'Tết dương lịch',
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             JSON_ARRAY('AUTO_SYNC','HOLIDAY'),
             FALSE,
             'system',
             'system'
         );

SET @ws_emp2_d1 := LAST_INSERT_ID();

-- Employee 1002 - 2026-01-03 night shift overnight
INSERT INTO work_schedules (
    work_date,
    product_line_id,
    team_id,
    employee_id,
    source_system,
    external_employee_id,
    sync_batch_id,
    raw_record_id,
    role_code,
    day_type,
    attendance_status,
    shift_code,
    shift_start_time,
    shift_end_time,
    break_minutes,
    is_overnight,
    planned_minutes,
    actual_minutes,
    ot_minutes_planned,
    ot_minutes_approved,
    late_minutes,
    early_leave_minutes,
    holiday_code,
    holiday_name,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES (
             '2026-01-03',
             1,
             1,
             2,
             'EXT_HRM',
             'EXT-E-2',
             @sync_batch_id,
             @raw_id_emp2_d3,
             'ROLE_TEAM_LEADER',
             'WORKING_DAY',
             'APPROVED',
             'NIGHT',
             '20:00:00',
             '05:00:00',
             60,
             TRUE,
             480,
             NULL,
             60,
             60,
             0,
             0,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             NULL,
             'Ca đêm + OT',
             JSON_ARRAY('AUTO_SYNC','OVERNIGHT','OT'),
             FALSE,
             'system',
             'system'
         );

SET @ws_emp2_d3 := LAST_INSERT_ID();

-- ----------------------------------------------------------------------------
-- 5) Insert SEGMENTS (linked to work_schedules.id)
-- ----------------------------------------------------------------------------

-- Segments for employee 1001 - 2026-01-01 (2 work segments)
INSERT INTO work_schedule_segments (
    work_schedule_id,
    segment_no,
    start_time,
    end_time,
    product_line_id,
    team_id,
    segment_type,
    role_code,
    shift_code,
    planned_minutes,
    actual_minutes,
    ot_minutes,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    holiday_code,
    holiday_name,
    process_ids_json,
    workstation_id,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES
      (
          @ws_emp1_d1,
          1,
          '08:00:00',
          '12:00:00',
          1,
          1,
          'WORK',
          'EMPLOYEE',
          'DAY',
          240,
          NULL,
          0,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          JSON_ARRAY('OP1','OP2'),
          'WS-01',
          NULL,
          JSON_ARRAY('AUTO_SYNC'),
          FALSE,
          'system',
          'system'
      ),
      (
          @ws_emp1_d1,
          2,
          '13:00:00',
          '17:00:00',
          1,
          1,
          'WORK',
          'EMPLOYEE',
          'DAY',
          240,
          NULL,
          0,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          JSON_ARRAY('OP3'),
          'WS-02',
          NULL,
          JSON_ARRAY('AUTO_SYNC'),
          FALSE,
          'system',
          'system'
      );

-- Segments for employee 1001 - 2026-01-02 (morning WORK + afternoon LEAVE)
INSERT INTO work_schedule_segments (
    work_schedule_id,
    segment_no,
    start_time,
    end_time,
    product_line_id,
    team_id,
    segment_type,
    role_code,
    shift_code,
    planned_minutes,
    actual_minutes,
    ot_minutes,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    holiday_code,
    holiday_name,
    process_ids_json,
    workstation_id,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES
      (
          @ws_emp1_d2,
          1,
          '08:00:00',
          '12:00:00',
          1,
          1,
          'WORK',
          'EMPLOYEE',
          'DAY',
          240,
          NULL,
          0,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          JSON_ARRAY('OP1'),
          'WS-01',
          NULL,
          JSON_ARRAY('AUTO_SYNC'),
          FALSE,
          'system',
          'system'
      ),
      (
          @ws_emp1_d2,
          2,
          '13:00:00',
          '17:00:00',
          1,
          1,
          'LEAVE',
          'EMPLOYEE',
          'DAY',
          0,
          0,
          0,
          'ANNUAL_LEAVE',
          TRUE,
          240,
          'APPROVED',
          'Nghỉ nửa ngày',
          NULL,
          NULL,
          NULL,
          NULL,
          NULL,
          JSON_ARRAY('AUTO_SYNC','LEAVE_HALF_DAY'),
          FALSE,
          'system',
          'system'
      );

-- Segment for employee 1002 - 2026-01-01 HOLIDAY full day
INSERT INTO work_schedule_segments (
    work_schedule_id,
    segment_no,
    start_time,
    end_time,
    product_line_id,
    team_id,
    segment_type,
    role_code,
    shift_code,
    planned_minutes,
    actual_minutes,
    ot_minutes,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    holiday_code,
    holiday_name,
    process_ids_json,
    workstation_id,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES
    (
        @ws_emp2_d1,
        1,
        '00:00:00',
        '23:59:00',
        1,
        1,
        'HOLIDAY',
        'ROLE_TEAM_LEADER',
        'OFF',
        0,
        0,
        0,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'NEW_YEAR',
        'Tết dương lịch',
        NULL,
        NULL,
        NULL,
        JSON_ARRAY('AUTO_SYNC','HOLIDAY'),
        FALSE,
        'system',
        'system'
    );

-- Segment for employee 1002 - 2026-01-03 NIGHT (overnight, end_time < start_time)
INSERT INTO work_schedule_segments (
    work_schedule_id,
    segment_no,
    start_time,
    end_time,
    product_line_id,
    team_id,
    segment_type,
    role_code,
    shift_code,
    planned_minutes,
    actual_minutes,
    ot_minutes,
    leave_type,
    leave_is_paid,
    leave_minutes,
    leave_approval_status,
    leave_reason,
    holiday_code,
    holiday_name,
    process_ids_json,
    workstation_id,
    note,
    tags_json,
    delete_flag,
    created_by,
    updated_by
) VALUES
    (
        @ws_emp2_d3,
        1,
        '20:00:00',
        '05:00:00',
        1,
        1,
        'WORK',
        'ROLE_TEAM_LEADER',
        'NIGHT',
        480,
        NULL,
        60,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        NULL,
        'WS-TL-01',
        'Overnight shift example',
        JSON_ARRAY('AUTO_SYNC','OVERNIGHT','OT'),
        FALSE,
        'system',
        'system'
    );

COMMIT;