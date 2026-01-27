-- ============================================================================
-- ANOMALY TRAINING SYSTEM - CORE DATABASE SCHEMA
-- Version: 1.0
-- Description: Initialize core tables (NO approval logic)
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- Drop all tables if exists
DROP TABLE IF EXISTS training_result_detail_history;
DROP TABLE IF EXISTS training_result_detail;
DROP TABLE IF EXISTS training_result_history;
DROP TABLE IF EXISTS training_result;
DROP TABLE IF EXISTS training_plan_detail_history;
DROP TABLE IF EXISTS training_plan_detail;
DROP TABLE IF EXISTS training_plan_history;
DROP TABLE IF EXISTS training_plan;
DROP TABLE IF EXISTS training_topics;
DROP TABLE IF EXISTS training_topic_report_detail_history;
DROP TABLE IF EXISTS training_topic_report_detail;
DROP TABLE IF EXISTS training_topic_report_history;
DROP TABLE IF EXISTS training_topic_report;
DROP TABLE IF EXISTS defects;
DROP TABLE IF EXISTS defect_report_detail_history;
DROP TABLE IF EXISTS defect_report_detail;
DROP TABLE IF EXISTS defect_report_history;
DROP TABLE IF EXISTS defect_report;
DROP TABLE IF EXISTS employee_skills;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS product_groups;
DROP TABLE IF EXISTS processes;
DROP TABLE IF EXISTS `groups`;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS notification_queue;
DROP TABLE IF EXISTS notification_settings;
DROP TABLE IF EXISTS notification_templates;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS users;

-- ============================================================================
-- PART 1: MASTER DATA - USERS & ORGANIZATION
-- ============================================================================

-- 1.1 USERS
CREATE TABLE users
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT,
    username          VARCHAR(50)                                                                NOT NULL UNIQUE,
    email             VARCHAR(100)                                                               NOT NULL UNIQUE,
    password_hash     VARCHAR(255),
    full_name         VARCHAR(100)                                                               NOT NULL,
    role              ENUM ('ADMIN', 'MANAGER', 'SUPERVISOR', 'TEAM_LEADER', 'FINAL_INSPECTION') NOT NULL,
    is_active         BOOLEAN                                                                             DEFAULT TRUE,

    -- OAuth support
    oauth_provider    ENUM ('LOCAL', 'MICROSOFT')                                                         DEFAULT 'LOCAL',
    oauth_provider_id VARCHAR(255),

    -- BaseEntity
    delete_flag       BOOLEAN                                                                    NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP                                                                           DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(255),
    updated_at        TIMESTAMP                                                                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by        VARCHAR(255),

    INDEX idx_role (role),
    INDEX idx_active (is_active),
    INDEX idx_oauth (oauth_provider, oauth_provider_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.2 REFRESH TOKENS
CREATE TABLE refresh_tokens
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(500) NOT NULL,
    expires_at  TIMESTAMP    NOT NULL,
    revoked     BOOLEAN               DEFAULT FALSE,
    device_info VARCHAR(255),
    ip_address  VARCHAR(45),

    delete_flag BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(255),

    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    INDEX idx_token (token(191)),
    INDEX idx_user_active (user_id, revoked, expires_at),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.3 SECTIONS (Xưởng/Bộ phận)
CREATE TABLE sections
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    name        VARCHAR(100) NOT NULL,
    manager_id  BIGINT       NOT NULL,

    delete_flag BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(255),

    FOREIGN KEY (manager_id) REFERENCES users (id),
    INDEX idx_manager (manager_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.4 GROUPS (Dây chuyền sản xuất)
CREATE TABLE `groups`
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    section_id    BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    supervisor_id BIGINT       NOT NULL,

    delete_flag   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(255),

    FOREIGN KEY (section_id) REFERENCES sections (id),
    FOREIGN KEY (supervisor_id) REFERENCES users (id),
    INDEX idx_section (section_id),
    INDEX idx_supervisor (supervisor_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.5 TEAMS (Tổ sản xuất)
CREATE TABLE teams
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id       BIGINT       NOT NULL,
    name           VARCHAR(100) NOT NULL,
    team_leader_id BIGINT       NOT NULL,

    delete_flag    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(255),
    updated_at     TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    FOREIGN KEY (team_leader_id) REFERENCES users (id),
    INDEX idx_group (group_id),
    INDEX idx_team_leader (team_leader_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.6 EMPLOYEES (Công nhân)
CREATE TABLE employees
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_code VARCHAR(20)  NOT NULL UNIQUE,
    full_name     VARCHAR(100) NOT NULL,
    team_id       BIGINT       NOT NULL,
    status        ENUM ('ACTIVE', 'MATERNITY_LEAVE', 'RESIGNED') DEFAULT 'ACTIVE',

    delete_flag   BOOLEAN      NOT NULL                          DEFAULT FALSE,
    created_at    TIMESTAMP                                      DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_at    TIMESTAMP                                      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(255),

    FOREIGN KEY (team_id) REFERENCES teams (id),
    INDEX idx_team (team_id),
    INDEX idx_status (status),
    INDEX idx_employee_code (employee_code),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.7 PROCESSES (Công đoạn/Vị trí kỹ thuật)
CREATE TABLE processes
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id         BIGINT       NOT NULL,
    code             VARCHAR(20)  NOT NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    classification   TINYINT      NOT NULL DEFAULT 4 COMMENT '1,2,3=Quan trọng cần FI ký, 4=Thường',
    standard_time_jt DECIMAL(10, 2) COMMENT 'Thời gian tiêu chuẩn (giây)',

    delete_flag      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    UNIQUE KEY uk_group_code (group_id, code),
    INDEX idx_classification (classification),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.8 EMPLOYEE_SKILLS (Chứng chỉ tay nghề)
CREATE TABLE employee_skills
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id    BIGINT  NOT NULL,
    process_id     BIGINT  NOT NULL,
    is_qualified   BOOLEAN          DEFAULT TRUE,
    certified_date DATE,
    expiry_date    DATE,

    delete_flag    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(255),
    updated_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),

    FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    FOREIGN KEY (process_id) REFERENCES processes (id) ON DELETE CASCADE,
    UNIQUE KEY uk_employee_process (employee_id, process_id),
    INDEX idx_qualified (is_qualified),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 1.9 PRODUCT_GROUPS (Nhóm sản phẩm)
CREATE TABLE product_groups
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id     BIGINT      NOT NULL,
    product_code VARCHAR(50) NOT NULL,

    delete_flag  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_at   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    UNIQUE KEY uk_group_product (group_id, product_code),
    INDEX idx_product_code (product_code),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 2: DEFECT MANAGEMENT (Quản lý lỗi quá khứ)
-- ============================================================================

-- 2.1 DEFECTS (Master data - Lỗi đã được duyệt)
CREATE TABLE defects
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    defect_description TEXT    NOT NULL,
    process_id         BIGINT  NOT NULL,
    detected_date      DATE    NOT NULL,
    is_escaped         BOOLEAN          DEFAULT FALSE COMMENT 'Lỗi lọt ra ngoài?',
    note               TEXT,

    delete_flag        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    FOREIGN KEY (process_id) REFERENCES processes (id),
    INDEX idx_process (process_id),
    INDEX idx_detected_date (detected_date),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.2 DEFECT_REPORT (Header báo cáo lỗi)
CREATE TABLE defect_report
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id        BIGINT  NOT NULL,
    status          ENUM (
        'DRAFT',
        'WAITING_SV',
        'REJECTED_BY_SV',
        'WAITING_MANAGER',
        'REJECTED_BY_MANAGER',
        'APPROVED'
        )                            DEFAULT 'DRAFT',
    current_version INT              DEFAULT 1,

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    INDEX idx_group (group_id),
    INDEX idx_status (status),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.3 DEFECT_REPORT_DETAIL (Chi tiết báo cáo)
CREATE TABLE defect_report_detail
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    defect_report_id   BIGINT                              NOT NULL,
    defect_id          BIGINT COMMENT 'NULL=CREATE mới, có giá trị=UPDATE/DELETE defect đã có',
    report_type        ENUM ('CREATE', 'UPDATE', 'DELETE') NOT NULL DEFAULT 'CREATE',
    defect_description TEXT                                NOT NULL,
    process_id         BIGINT                              NOT NULL,
    detected_date      DATE                                NOT NULL,
    is_escaped         BOOLEAN                                      DEFAULT FALSE,
    note               TEXT,

    delete_flag        BOOLEAN                             NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    FOREIGN KEY (defect_report_id) REFERENCES defect_report (id) ON DELETE CASCADE,
    FOREIGN KEY (defect_id) REFERENCES defects (id) ON DELETE SET NULL,
    FOREIGN KEY (process_id) REFERENCES processes (id),
    INDEX idx_report (defect_report_id),
    INDEX idx_defect (defect_id),
    INDEX idx_process (process_id),
    INDEX idx_report_type (report_type),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.4 DEFECT_REPORT_HISTORY (Lịch sử reject/sửa - Header)
CREATE TABLE defect_report_history
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    defect_report_id BIGINT  NOT NULL,
    version          INT     NOT NULL,

    -- Snapshot
    group_id         BIGINT,
    group_name       VARCHAR(100),
    recorded_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    delete_flag      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (defect_report_id) REFERENCES defect_report (id) ON DELETE CASCADE,
    INDEX idx_report (defect_report_id),
    INDEX idx_version (version),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2.5 DEFECT_REPORT_DETAIL_HISTORY (Lịch sử chi tiết)
CREATE TABLE defect_report_detail_history
(
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    defect_report_history_id BIGINT  NOT NULL,

    -- Snapshot
    defect_id                BIGINT,
    report_type              VARCHAR(20),
    defect_description       TEXT,
    process_id               BIGINT,
    process_code             VARCHAR(20),
    process_name             VARCHAR(200),
    detected_date            DATE,
    is_escaped               BOOLEAN,
    note                     TEXT,

    delete_flag              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(255),
    updated_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by               VARCHAR(255),

    FOREIGN KEY (defect_report_history_id) REFERENCES defect_report_history (id) ON DELETE CASCADE,
    INDEX idx_history (defect_report_history_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 3: TRAINING TOPIC (Mẫu huấn luyện)
-- ============================================================================

-- 3.1 TRAINING_TOPICS (Master data - Mẫu huấn luyện đã duyệt)
CREATE TABLE training_topics
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_id      BIGINT       NOT NULL,
    defect_id       BIGINT COMMENT 'Link đến lỗi quá khứ (nếu có)',
    category_name   VARCHAR(200) NOT NULL COMMENT 'Hạng mục huấn luyện',
    training_sample TEXT COMMENT 'Mẫu huấn luyện',
    training_detail TEXT         NOT NULL COMMENT 'Nội dung cần huấn luyện',
    note            TEXT,

    delete_flag     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (process_id) REFERENCES processes (id),
    FOREIGN KEY (defect_id) REFERENCES defects (id) ON DELETE SET NULL,
    INDEX idx_process (process_id),
    INDEX idx_defect (defect_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 3.2 TRAINING_TOPIC_REPORT (Header báo cáo mẫu huấn luyện)
CREATE TABLE training_topic_report
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id        BIGINT  NOT NULL,
    status          ENUM (
        'DRAFT',
        'WAITING_SV',
        'REJECTED_BY_SV',
        'WAITING_MANAGER',
        'REJECTED_BY_MANAGER',
        'APPROVED'
        )                            DEFAULT 'DRAFT',
    current_version INT              DEFAULT 1,

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    INDEX idx_group (group_id),
    INDEX idx_status (status),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 3.3 TRAINING_TOPIC_REPORT_DETAIL (Chi tiết báo cáo)
CREATE TABLE training_topic_report_detail
(
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_topic_report_id BIGINT                              NOT NULL,
    training_topic_id        BIGINT COMMENT 'NULL=CREATE mới, có giá trị=UPDATE/DELETE topic đã có',
    report_type              ENUM ('CREATE', 'UPDATE', 'DELETE') NOT NULL DEFAULT 'CREATE',
    process_id               BIGINT                              NOT NULL,
    defect_id                BIGINT COMMENT 'Link đến lỗi quá khứ',
    category_name            VARCHAR(200)                        NOT NULL,
    training_sample          TEXT,
    training_detail          TEXT                                NOT NULL,
    note                     TEXT,

    delete_flag              BOOLEAN                             NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(255),
    updated_at               TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by               VARCHAR(255),

    FOREIGN KEY (training_topic_report_id) REFERENCES training_topic_report (id) ON DELETE CASCADE,
    FOREIGN KEY (training_topic_id) REFERENCES training_topics (id) ON DELETE SET NULL,
    FOREIGN KEY (process_id) REFERENCES processes (id),
    FOREIGN KEY (defect_id) REFERENCES defects (id) ON DELETE SET NULL,
    INDEX idx_report (training_topic_report_id),
    INDEX idx_topic (training_topic_id),
    INDEX idx_process (process_id),
    INDEX idx_defect (defect_id),
    INDEX idx_report_type (report_type),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 3.4 TRAINING_TOPIC_REPORT_HISTORY (Lịch sử - Header)
CREATE TABLE training_topic_report_history
(
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_topic_report_id BIGINT  NOT NULL,
    version                  INT     NOT NULL,

    -- Snapshot
    group_id                 BIGINT,
    group_name               VARCHAR(100),
    recorded_at              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    delete_flag              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(255),
    updated_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by               VARCHAR(255),

    FOREIGN KEY (training_topic_report_id) REFERENCES training_topic_report (id) ON DELETE CASCADE,
    INDEX idx_report (training_topic_report_id),
    INDEX idx_version (version),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 3.5 TRAINING_TOPIC_REPORT_DETAIL_HISTORY (Lịch sử chi tiết)
CREATE TABLE training_topic_report_detail_history
(
    id                               BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_topic_report_history_id BIGINT  NOT NULL,

    -- Snapshot
    training_topic_id                BIGINT,
    report_type                      VARCHAR(20),
    process_id                       BIGINT,
    process_code                     VARCHAR(20),
    process_name                     VARCHAR(200),
    defect_id                        BIGINT,
    category_name                    VARCHAR(200),
    training_sample                  TEXT,
    training_detail                  TEXT,
    note                             TEXT,

    delete_flag                      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by                       VARCHAR(255),
    updated_at                       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                       VARCHAR(255),

    FOREIGN KEY (training_topic_report_history_id) REFERENCES training_topic_report_history (id) ON DELETE CASCADE,
    INDEX idx_history (training_topic_report_history_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 4: TRAINING PLAN (Kế hoạch huấn luyện)
-- ============================================================================

-- 4.1 TRAINING_PLAN (Header kế hoạch)
CREATE TABLE training_plan
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    form_code       VARCHAR(50)      DEFAULT 'TR_PLAN',
    month_start     DATE    NOT NULL,
    month_end       DATE    NOT NULL,
    group_id        BIGINT  NOT NULL,
    status          ENUM (
        'DRAFT',
        'WAITING_SV',
        'REJECTED_BY_SV',
        'WAITING_MANAGER',
        'REJECTED_BY_MANAGER',
        'APPROVED'
        )                            DEFAULT 'DRAFT',
    current_version INT              DEFAULT 1,
    note            TEXT,

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    INDEX idx_group (group_id),
    INDEX idx_status (status),
    INDEX idx_month_range (month_start, month_end),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 4.2 TRAINING_PLAN_DETAIL (Chi tiết kế hoạch)
CREATE TABLE training_plan_detail
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_plan_id BIGINT  NOT NULL,
    employee_id      BIGINT  NOT NULL,
    process_id       BIGINT  NOT NULL,
    target_month     DATE    NOT NULL COMMENT 'Tháng thực hiện',
    planned_date     DATE    NOT NULL COMMENT 'Ngày dự kiến',
    status           ENUM ('PENDING', 'DONE', 'MISSED') DEFAULT 'PENDING',
    note             TEXT,

    delete_flag      BOOLEAN NOT NULL                   DEFAULT FALSE,
    created_at       TIMESTAMP                          DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP                          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (training_plan_id) REFERENCES training_plan (id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees (id),
    FOREIGN KEY (process_id) REFERENCES processes (id),
    INDEX idx_plan (training_plan_id),
    INDEX idx_employee (employee_id),
    INDEX idx_process (process_id),
    INDEX idx_target_month (target_month),
    INDEX idx_planned_date (planned_date),
    INDEX idx_status (status),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 4.3 TRAINING_PLAN_HISTORY (Lịch sử - Header)
CREATE TABLE training_plan_history
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_plan_id BIGINT  NOT NULL,
    version          INT     NOT NULL,

    -- Snapshot
    form_code        VARCHAR(50),
    month_start      DATE,
    month_end        DATE,
    group_id         BIGINT,
    group_name       VARCHAR(100),
    note             TEXT,
    recorded_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    delete_flag      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (training_plan_id) REFERENCES training_plan (id) ON DELETE CASCADE,
    INDEX idx_plan (training_plan_id),
    INDEX idx_version (version),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 4.4 TRAINING_PLAN_DETAIL_HISTORY (Lịch sử chi tiết)
CREATE TABLE training_plan_detail_history
(
    id                       BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_plan_history_id BIGINT  NOT NULL,

    -- Snapshot
    employee_id              BIGINT,
    employee_code            VARCHAR(20),
    employee_name            VARCHAR(100),
    process_id               BIGINT,
    process_code             VARCHAR(20),
    process_name             VARCHAR(200),
    target_month             DATE,
    planned_date             DATE,
    status                   VARCHAR(20),
    note                     TEXT,

    delete_flag              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(255),
    updated_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by               VARCHAR(255),

    FOREIGN KEY (training_plan_history_id) REFERENCES training_plan_history (id) ON DELETE CASCADE,
    INDEX idx_history (training_plan_history_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 5: TRAINING RESULT (Kết quả huấn luyện)
-- ============================================================================

-- 5.1 TRAINING_RESULT (Header kết quả)
CREATE TABLE training_result
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    form_code       VARCHAR(50)      DEFAULT 'TR_RESULT',
    year            INT     NOT NULL,
    group_id        BIGINT  NOT NULL,
    status          ENUM (
        'ON_GOING',
        'DONE',
        'WAITING_MANAGER',
        'REJECTED_BY_MANAGER',
        'APPROVED_BY_MANAGER'
        )                            DEFAULT 'ON_GOING',
    current_version INT              DEFAULT 1,
    note            TEXT,

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    INDEX idx_group (group_id),
    INDEX idx_year (year),
    INDEX idx_status (status),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 5.2 TRAINING_RESULT_DETAIL (Chi tiết kết quả - từng lần test)
CREATE TABLE training_result_detail
(
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_result_id      BIGINT  NOT NULL,
    training_plan_detail_id BIGINT  NOT NULL COMMENT 'Link về kế hoạch',
    training_topic_id       BIGINT COMMENT 'Mẫu huấn luyện sử dụng',

    -- Thời gian
    planned_date            DATE    NOT NULL,
    actual_date             DATE,
    product_group_id        BIGINT COMMENT 'Mã sản phẩm đang chạy lúc test',
    time_in                 TIME COMMENT 'Giờ đưa mẫu vào',
    time_out                TIME COMMENT 'Giờ lấy mẫu ra',
    training_sample         VARCHAR(255) COMMENT 'Mẫu huấn luyện sử dụng',
    status                  ENUM ('PENDING', 'DONE', 'NEED_SIGN', 'WAITING_SV', 'REJECTED_BY_SV', 'APPROVED') DEFAULT 'PENDING',
    -- Kết quả
    detection_time          INT COMMENT 'Thời gian phát hiện (giây)',
    is_pass                 BOOLEAN COMMENT 'TRUE=Pass, FALSE=Fail',
    remedial_action         TEXT COMMENT 'Đối sách nếu Fail',
    note                    TEXT COMMENT 'Comment anything',

    -- Chữ ký (Ký xác nhận thực hiện test)
    signature_pro_in        BIGINT COMMENT 'TL Sản xuất ký lúc vào',
    signature_fi_in         BIGINT COMMENT 'TL Kiểm tra ký lúc vào (nullable nếu classification=4)',
    signature_pro_out       BIGINT COMMENT 'TL Sản xuất ký lúc ra',
    signature_fi_out        BIGINT COMMENT 'TL Kiểm tra ký lúc ra (nullable nếu classification=4)',

    delete_flag             BOOLEAN NOT NULL DEFAULT FALSE,
    created_at              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(255),
    updated_at              TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by              VARCHAR(255),

    FOREIGN KEY (training_result_id) REFERENCES training_result (id) ON DELETE CASCADE,
    FOREIGN KEY (training_plan_detail_id) REFERENCES training_plan_detail (id),
    FOREIGN KEY (training_topic_id) REFERENCES training_topics (id) ON DELETE SET NULL,
    FOREIGN KEY (product_group_id) REFERENCES product_groups (id),
    FOREIGN KEY (signature_fi_in) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (signature_fi_out) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_result (training_result_id),
    INDEX idx_plan_detail (training_plan_detail_id),
    INDEX idx_topic (training_topic_id),
    INDEX idx_actual_date (actual_date),
    INDEX idx_is_pass (is_pass),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 5.3 TRAINING_RESULT_HISTORY (Lịch sử - Header)
CREATE TABLE training_result_history
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_result_id BIGINT  NOT NULL,
    version            INT     NOT NULL,

    -- Snapshot
    form_code          VARCHAR(50),
    year               INT,
    group_id           BIGINT,
    group_name         VARCHAR(100),
    note               TEXT,
    recorded_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    delete_flag        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    FOREIGN KEY (training_result_id) REFERENCES training_result (id) ON DELETE CASCADE,
    INDEX idx_result (training_result_id),
    INDEX idx_version (version),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 5.4 TRAINING_RESULT_DETAIL_HISTORY (Lịch sử chi tiết)
CREATE TABLE training_result_detail_history
(
    id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_result_history_id BIGINT  NOT NULL,
    training_result_detail_id  BIGINT  NOT NULL,

    -- Snapshot
    training_topic_id          BIGINT,
    planned_date               DATE,
    actual_date                DATE,
    product_group_id           BIGINT,
    time_in                    TIME,
    time_out                   TIME,
    training_sample            VARCHAR(255),
    detection_time             INT,
    is_pass                    BOOLEAN,
    remedial_action            TEXT,

    -- Signature snapshot
    signature_pro_in           BIGINT,
    signature_pro_in_name      VARCHAR(100),
    signature_fi_in            BIGINT,
    signature_fi_in_name       VARCHAR(100),
    signature_pro_out          BIGINT,
    signature_pro_out_name     VARCHAR(100),
    signature_fi_out           BIGINT,
    signature_fi_out_name      VARCHAR(100),

    delete_flag                BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by                 VARCHAR(255),
    updated_at                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                 VARCHAR(255),

    FOREIGN KEY (training_result_history_id) REFERENCES training_result_history (id) ON DELETE CASCADE,
    FOREIGN KEY (training_result_detail_id) REFERENCES training_result_detail (id) ON DELETE CASCADE,
    INDEX idx_history (training_result_history_id),
    INDEX idx_detail (training_result_detail_id),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 6: NOTIFICATION SYSTEM
-- ============================================================================

-- 6.1 NOTIFICATION_TEMPLATES
CREATE TABLE notification_templates
(
    code             VARCHAR(50) PRIMARY KEY,
    subject_template VARCHAR(255) NOT NULL,
    body_template    TEXT         NOT NULL,
    description      VARCHAR(500),

    delete_flag      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 6.2 NOTIFICATION_SETTINGS
CREATE TABLE notification_settings
(
    id                    BIGINT PRIMARY KEY AUTO_INCREMENT,
    template_code         VARCHAR(50) NOT NULL,
    is_enabled            BOOLEAN              DEFAULT TRUE,
    remind_before_days    INT                  DEFAULT 3,
    is_persistent         BOOLEAN              DEFAULT FALSE,
    remind_interval_hours INT                  DEFAULT 24,
    max_reminders         INT                  DEFAULT 5,
    preferred_send_time   TIME                 DEFAULT '08:00:00',
    escalate_after_days   INT,

    delete_flag           BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(255),
    updated_at            TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by            VARCHAR(255),

    FOREIGN KEY (template_code) REFERENCES notification_templates (code) ON DELETE CASCADE,
    INDEX idx_template (template_code),
    INDEX idx_enabled (is_enabled),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 6.3 NOTIFICATION_QUEUE
CREATE TABLE notification_queue
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    recipient_user_id    BIGINT       NOT NULL,
    cc_list              TEXT,
    notification_type    VARCHAR(50)  NOT NULL,
    related_entity_id    BIGINT,
    related_entity_table VARCHAR(50),
    subject              VARCHAR(255) NOT NULL,
    body                 TEXT         NOT NULL,
    status               ENUM ('PENDING', 'SENDING', 'SENT', 'FAILED') DEFAULT 'PENDING',
    retry_count          INT                                           DEFAULT 0,
    max_retries          INT                                           DEFAULT 3,
    error_message        TEXT,
    scheduled_at         TIMESTAMP                                     DEFAULT CURRENT_TIMESTAMP,
    sent_at              TIMESTAMP    NULL,

    delete_flag          BOOLEAN      NOT NULL                         DEFAULT FALSE,
    created_at           TIMESTAMP                                     DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(255),
    updated_at           TIMESTAMP                                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by           VARCHAR(255),

    FOREIGN KEY (recipient_user_id) REFERENCES users (id),
    FOREIGN KEY (notification_type) REFERENCES notification_templates (code),
    INDEX idx_recipient (recipient_user_id),
    INDEX idx_status (status),
    INDEX idx_scheduled (scheduled_at),
    INDEX idx_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- END OF CORE SCHEMA
-- ============================================================================