-- ANOMALY TRAINING SYSTEM - CORE DATABASE SCHEMA

SET FOREIGN_KEY_CHECKS = 0;

-- DROP ALL TABLES (đúng thứ tự dependency ngược)

DROP TABLE IF EXISTS approval_detail_comments;
DROP TABLE IF EXISTS approval_required_actions;
DROP TABLE IF EXISTS approval_action_reject_reasons;
DROP TABLE IF EXISTS required_actions;
DROP TABLE IF EXISTS reject_reasons;
DROP TABLE IF EXISTS training_sample_reviews;
DROP TABLE IF EXISTS training_sample_review_configs;
DROP TABLE IF EXISTS training_result_detail_history;
DROP TABLE IF EXISTS training_result_detail;
DROP TABLE IF EXISTS training_result_history;
DROP TABLE IF EXISTS training_result;
DROP TABLE IF EXISTS training_plan_detail_history;
DROP TABLE IF EXISTS training_plan_detail;
DROP TABLE IF EXISTS training_plan_history;
DROP TABLE IF EXISTS training_plan;
DROP TABLE IF EXISTS training_sample_proposal_detail_history;
DROP TABLE IF EXISTS training_sample_proposal_detail;
DROP TABLE IF EXISTS training_sample_proposal_history;
DROP TABLE IF EXISTS training_sample_proposal;
DROP TABLE IF EXISTS training_samples;
DROP TABLE IF EXISTS defect_proposal_detail_history;
DROP TABLE IF EXISTS defect_proposal_detail;
DROP TABLE IF EXISTS defect_proposal_history;
DROP TABLE IF EXISTS defect_proposal;
DROP TABLE IF EXISTS defects;
DROP TABLE IF EXISTS product_process;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS employee_skills;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS teams;
DROP TABLE IF EXISTS processes;
DROP TABLE IF EXISTS product_lines;
DROP TABLE IF EXISTS `groups`;
DROP TABLE IF EXISTS sections;
DROP TABLE IF EXISTS notification_queue;
DROP TABLE IF EXISTS notification_settings;
DROP TABLE IF EXISTS notification_templates;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS permissions;
DROP TABLE IF EXISTS modules;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS refresh_tokens;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS approval_flow_steps;
DROP TABLE IF EXISTS approval_actions;


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
    employee_code VARCHAR(20)  NOT NULL UNIQUE,
    -- OAuth support
    oauth_provider    ENUM ('LOCAL', 'MICROSOFT')                                                         DEFAULT 'LOCAL',
    oauth_provider_id VARCHAR(255),

    -- BaseEntity
    delete_flag       BOOLEAN                                                                    NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP                                                                           DEFAULT CURRENT_TIMESTAMP,
    created_by        VARCHAR(255),
    updated_at        TIMESTAMP                                                                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by        VARCHAR(255),

    INDEX idx_users_role (role),
    INDEX idx_users_active (is_active),
    INDEX idx_users_oauth (oauth_provider, oauth_provider_id),
    INDEX idx_users_delete_flag (delete_flag)
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
    INDEX idx_refresh_tokens_token (token(191)),
    INDEX idx_refresh_tokens_user_active (user_id, revoked, expires_at),
    INDEX idx_refresh_tokens_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.3 ROLES
CREATE TABLE roles
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_code    VARCHAR(50) NOT NULL UNIQUE,
    display_name NVARCHAR(100),
    description  NVARCHAR(500),
    is_system    BOOLEAN     NOT NULL DEFAULT FALSE,
    is_active    BOOLEAN     NOT NULL DEFAULT TRUE,

    delete_flag  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_at   TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),

    INDEX idx_roles_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.4 MODULES
CREATE TABLE modules
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_code  VARCHAR(50)   NOT NULL UNIQUE,
    display_name NVARCHAR(200) NOT NULL,
    description  NVARCHAR(500),
    sort_order   INT                    DEFAULT 0,

    delete_flag  BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_at   TIMESTAMP              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),

    INDEX idx_modules_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.5 PERMISSIONS
CREATE TABLE permissions
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    permission_code VARCHAR(100)  NOT NULL UNIQUE,
    display_name    NVARCHAR(200) NOT NULL,
    description     NVARCHAR(500),
    module_id       BIGINT,
    action          VARCHAR(50)   NOT NULL,
    sort_order      INT                    DEFAULT 0,
    is_system       BOOLEAN       NOT NULL DEFAULT TRUE,

    delete_flag     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP              DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP              DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    CONSTRAINT fk_permissions_module FOREIGN KEY (module_id) REFERENCES modules (id),
    INDEX idx_permissions_module_id (module_id),
    INDEX idx_permissions_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.6 USER_ROLES
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (role_id) REFERENCES roles (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.7 ROLE_PERMISSIONS
CREATE TABLE role_permissions
(
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles (id),
    FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.8 SECTIONS (Xưởng/Bộ phận)
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
    INDEX idx_sections_manager (manager_id),
    INDEX idx_sections_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.9 GROUPS (Dây chuyền sản xuất)
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
    INDEX idx_groups_section (section_id),
    INDEX idx_groups_supervisor (supervisor_id),
    INDEX idx_groups_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.10 TEAMS (Tổ sản xuất)
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
    INDEX idx_teams_group (group_id),
    INDEX idx_teams_team_leader (team_leader_id),
    INDEX idx_teams_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.11 EMPLOYEES (Công nhân)
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
    INDEX idx_employees_team (team_id),
    INDEX idx_employees_status (status),
    INDEX idx_employees_code (employee_code),
    INDEX idx_employees_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.12 PRODUCT LINES (Dòng sản phẩm)
CREATE TABLE product_lines
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id    BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,

    delete_flag BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(255),

    FOREIGN KEY (group_id) REFERENCES `groups` (id),
    INDEX idx_product_lines_group (group_id),
    INDEX idx_product_lines_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.13 PROCESSES (Công đoạn/Vị trí kỹ thuật)
CREATE TABLE processes
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_line_id  BIGINT       NOT NULL,
    code             VARCHAR(20)  NOT NULL,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    classification   TINYINT      NOT NULL DEFAULT 4
        COMMENT '1,2,3=Quan trọng cần FI ký, 4=Thường',
    standard_time_jt DECIMAL(10, 2)
        COMMENT 'Thời gian tiêu chuẩn (giây)',

    delete_flag      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (product_line_id) REFERENCES product_lines (id),
    UNIQUE KEY uk_processes_product_line_code (product_line_id, code),
    INDEX idx_processes_classification (classification),
    INDEX idx_processes_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.14 PRODUCTS (Sản phẩm cụ thể)
CREATE TABLE products
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    code        VARCHAR(50) NOT NULL UNIQUE COMMENT 'Mã sản phẩm (VD: 0750)',
    name        VARCHAR(255) COMMENT 'Tên sản phẩm',
    description TEXT COMMENT 'Mô tả',

    delete_flag BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP            DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_at  TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(255),

    INDEX idx_products_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.15 PRODUCT_PROCESS (N:M giữa Product và Process)
CREATE TABLE product_process
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id       BIGINT  NOT NULL,
    process_id       BIGINT  NOT NULL,
    standard_time_jt DECIMAL(10, 2),

    delete_flag      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE,
    FOREIGN KEY (process_id) REFERENCES processes (id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_process (product_id, process_id),
    INDEX idx_product_process_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 1.16 EMPLOYEE_SKILLS (Chứng chỉ tay nghề)
CREATE TABLE employee_skills
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    employee_id    BIGINT  NOT NULL,
    process_id     BIGINT  NOT NULL,
    status         ENUM ('REVOKED', 'PENDING_REVIEW', 'VALID'),
    certified_date DATE,
    expiry_date    DATE,

    delete_flag    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(255),
    updated_at     TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by     VARCHAR(255),

    FOREIGN KEY (employee_id) REFERENCES employees (id) ON DELETE CASCADE,
    FOREIGN KEY (process_id) REFERENCES processes (id) ON DELETE CASCADE,
    UNIQUE KEY uk_employee_skills (employee_id, process_id),
    INDEX idx_employee_skills_delete_flag (delete_flag)
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
    origin_cause       VARCHAR(255),
    outflow_cause      VARCHAR(255),
    cause_point        VARCHAR(255),
    note               TEXT,

    delete_flag        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    FOREIGN KEY (process_id) REFERENCES processes (id),
    INDEX idx_defects_process (process_id),
    INDEX idx_defects_detected_date (detected_date),
    INDEX idx_defects_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 2.2 DEFECT_PROPOSAL (Header đề xuất lỗi)
CREATE TABLE defect_proposals
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_line_id BIGINT  NOT NULL,
    status          ENUM ('DRAFT', 'WAITING_SV', 'REJECTED_BY_SV',
        'WAITING_MANAGER', 'REJECTED_BY_MANAGER', 'APPROVED')
                                     DEFAULT 'DRAFT',
    current_version INT              DEFAULT 1,
    form_code       VARCHAR(255),

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (product_line_id) REFERENCES product_lines (id),
    INDEX idx_defect_proposals_product_line (product_line_id),
    INDEX idx_defect_proposals_status (status),
    INDEX idx_defect_proposals_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 2.3 DEFECT_PROPOSAL_DETAIL (Chi tiết đề xuất)
CREATE TABLE defect_proposal_details
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    defect_proposal_id BIGINT                              NOT NULL,
    defect_id          BIGINT COMMENT 'NULL=CREATE mới, có giá trị=UPDATE/DELETE defect đã có',
    proposal_type      ENUM ('CREATE', 'UPDATE', 'DELETE') NOT NULL DEFAULT 'CREATE',
    defect_description TEXT                                NOT NULL,
    process_id         BIGINT                              NOT NULL,
    detected_date      DATE                                NOT NULL,
    is_escaped         BOOLEAN                                      DEFAULT FALSE,
    note               TEXT,
    origin_cause       VARCHAR(255),
    outflow_cause      VARCHAR(255),
    cause_point        VARCHAR(255),

    delete_flag        BOOLEAN                             NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    FOREIGN KEY (defect_proposal_id) REFERENCES defect_proposals (id) ON DELETE CASCADE,
    FOREIGN KEY (defect_id) REFERENCES defects (id) ON DELETE SET NULL,
    FOREIGN KEY (process_id) REFERENCES processes (id),
    INDEX idx_defect_proposal_details_proposal (defect_proposal_id),
    INDEX idx_defect_proposal_details_defect (defect_id),
    INDEX idx_defect_proposal_details_process (process_id),
    INDEX idx_defect_proposal_details_type (proposal_type),
    INDEX idx_defect_proposal_details_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 2.4 DEFECT_PROPOSAL_HISTORY (Lịch sử - Header)
CREATE TABLE defect_proposal_history
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    defect_proposal_id BIGINT  NOT NULL,
    version            INT     NOT NULL,

    -- Snapshot
    product_line_id    BIGINT,
    form_code          VARCHAR(255),
    recorded_at        TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    delete_flag        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    FOREIGN KEY (defect_proposal_id) REFERENCES defect_proposals (id) ON DELETE CASCADE,
    INDEX idx_defect_proposal_history_proposal (defect_proposal_id),
    INDEX idx_defect_proposal_history_version (version),
    INDEX idx_defect_proposal_history_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 2.5 DEFECT_PROPOSAL_DETAIL_HISTORY (Lịch sử chi tiết)
CREATE TABLE defect_proposal_detail_history
(
    id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
    defect_proposal_history_id BIGINT  NOT NULL,

    -- Snapshot
    defect_id                  BIGINT,
    proposal_type              VARCHAR(20),
    defect_description         TEXT,
    process_id                 BIGINT,
    process_code               VARCHAR(20),
    process_name               VARCHAR(200),
    detected_date              DATE,
    is_escaped                 BOOLEAN,
    note                       TEXT,
    origin_cause               VARCHAR(255),
    outflow_cause              VARCHAR(255),
    cause_point                VARCHAR(255),

    delete_flag                BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by                 VARCHAR(255),
    updated_at                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                 VARCHAR(255),

    FOREIGN KEY (defect_proposal_history_id) REFERENCES defect_proposal_history (id) ON DELETE CASCADE,
    INDEX idx_defect_proposal_dh_history (defect_proposal_history_id),
    INDEX idx_defect_proposal_dh_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 3: TRAINING SAMPLE (Mẫu huấn luyện)
-- ============================================================================

-- 3.1 TRAINING_SAMPLES (Master data - Mẫu huấn luyện đã duyệt)
CREATE TABLE training_samples
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    process_id           BIGINT       NOT NULL,
    product_line_id      BIGINT       NOT NULL,
    defect_id            BIGINT,
    category_name        VARCHAR(200) NOT NULL COMMENT 'Hạng mục huấn luyện',
    training_description TEXT         NOT NULL COMMENT 'Nội dung huấn luyện',
    product_id           BIGINT COMMENT 'Mã sản phẩm áp dụng',
    sample_code          VARCHAR(20) COMMENT 'Mã mẫu (M1.1.1)',
    process_order        INT          NOT NULL COMMENT 'Thứ tự công đoạn',
    category_order       INT          NOT NULL COMMENT 'Thứ tự hạng mục trong công đoạn',
    content_order        INT          NOT NULL COMMENT 'Thứ tự nội dung trong hạng mục',
    note                 TEXT,

    delete_flag          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(255),
    updated_at           TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by           VARCHAR(255),

    FOREIGN KEY (process_id) REFERENCES processes (id),
    FOREIGN KEY (product_line_id) REFERENCES product_lines (id),
    FOREIGN KEY (defect_id) REFERENCES defects (id) ON DELETE SET NULL,
    FOREIGN KEY (product_id) REFERENCES products (id),
    UNIQUE KEY uk_training_samples_code (product_line_id, sample_code),
    INDEX idx_training_samples_process (process_id),
    INDEX idx_training_samples_product_line (product_line_id),
    INDEX idx_training_samples_product (product_id),
    INDEX idx_training_samples_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 3.2 TRAINING_SAMPLE_PROPOSALS (Header đề xuất mẫu huấn luyện)
CREATE TABLE training_sample_proposals
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_line_id BIGINT  NOT NULL,
    status          ENUM ('DRAFT', 'WAITING_SV', 'REJECTED_BY_SV',
        'WAITING_MANAGER', 'REJECTED_BY_MANAGER', 'APPROVED')
                                     DEFAULT 'DRAFT',
    current_version INT              DEFAULT 1,
    form_code       VARCHAR(255),

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (product_line_id) REFERENCES product_lines (id),
    INDEX idx_training_sample_proposals_pl (product_line_id),
    INDEX idx_training_sample_proposals_status (status),
    INDEX idx_training_sample_proposals_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 3.3 TRAINING_SAMPLE_PROPOSAL_DETAILS (Chi tiết đề xuất)
CREATE TABLE training_sample_proposal_details
(
    id                          BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_sample_proposal_id BIGINT                              NOT NULL,
    training_sample_id          BIGINT COMMENT 'NULL=CREATE mới, có giá trị=UPDATE/DELETE sample đã có',
    proposal_type               ENUM ('CREATE', 'UPDATE', 'DELETE') NOT NULL,
    process_id                  BIGINT                              NOT NULL,
    product_id                  BIGINT COMMENT 'Mã sản phẩm áp dụng',
    defect_id                   BIGINT COMMENT 'Link đến lỗi quá khứ',
    category_name               VARCHAR(200)                        NOT NULL,
    training_sample_code        VARCHAR(20),
    training_description        TEXT                                NOT NULL,
    note                        TEXT,

    delete_flag                 BOOLEAN                             NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP,
    created_by                  VARCHAR(255),
    updated_at                  TIMESTAMP                                    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(255),

    FOREIGN KEY (training_sample_proposal_id) REFERENCES training_sample_proposals (id) ON DELETE CASCADE,
    FOREIGN KEY (training_sample_id) REFERENCES training_samples (id) ON DELETE SET NULL,
    FOREIGN KEY (process_id) REFERENCES processes (id),
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (defect_id) REFERENCES defects (id) ON DELETE SET NULL,
    INDEX idx_ts_proposal_details_proposal (training_sample_proposal_id),
    INDEX idx_ts_proposal_details_sample (training_sample_id),
    INDEX idx_ts_proposal_details_process (process_id),
    INDEX idx_ts_proposal_details_type (proposal_type),
    INDEX idx_ts_proposal_details_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 3.4 TRAINING_SAMPLE_PROPOSAL_HISTORY (Lịch sử - Header)
CREATE TABLE training_sample_proposal_history
(
    id                          BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_sample_proposal_id BIGINT  NOT NULL,
    version                     INT     NOT NULL,

    -- Snapshot
    product_line_id             BIGINT,
    recorded_at                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    delete_flag                 BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by                  VARCHAR(255),
    updated_at                  TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(255),

    FOREIGN KEY (training_sample_proposal_id) REFERENCES training_sample_proposals (id) ON DELETE CASCADE,
    INDEX idx_ts_proposal_history_proposal (training_sample_proposal_id),
    INDEX idx_ts_proposal_history_version (version),
    INDEX idx_ts_proposal_history_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 3.5 TRAINING_SAMPLE_PROPOSAL_DETAIL_HISTORY (Lịch sử chi tiết)
CREATE TABLE training_sample_proposal_detail_history
(
    id                                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_sample_proposal_history_id BIGINT  NOT NULL,

    -- Snapshot
    training_sample_id                  BIGINT,
    proposal_type                       VARCHAR(20),
    process_id                          BIGINT,
    process_code                        VARCHAR(20),
    process_name                        VARCHAR(200),
    defect_id                           BIGINT,
    category_name                       VARCHAR(200),
    training_sample_code                VARCHAR(20),
    training_description                TEXT,
    product_id                          BIGINT,
    note                                TEXT,

    delete_flag                         BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by                          VARCHAR(255),
    updated_at                          TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                          VARCHAR(255),

    FOREIGN KEY (training_sample_proposal_history_id)
        REFERENCES training_sample_proposal_history (id) ON DELETE CASCADE,
    INDEX idx_ts_proposal_dh_history (training_sample_proposal_history_id),
    INDEX idx_ts_proposal_dh_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 4: TRAINING PLAN (Kế hoạch huấn luyện)
-- ============================================================================

-- 4.1 TRAINING_PLAN (Header kế hoạch)
CREATE TABLE training_plans
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    form_code       VARCHAR(50)      DEFAULT 'TR_PLAN',
    title           TEXT,
    month_start     DATE,
    month_end       DATE,
    team_id         BIGINT,
    line_id         BIGINT COMMENT 'Dây chuyền áp dụng',
    status          ENUM ('DRAFT', 'WAITING_SV', 'REJECTED_BY_SV',
        'WAITING_MANAGER', 'REJECTED_BY_MANAGER', 'APPROVED')
                                     DEFAULT 'DRAFT',
    current_version INT              DEFAULT 1,
    note            TEXT,

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (team_id) REFERENCES `teams` (id),
    FOREIGN KEY (line_id) REFERENCES product_lines (id),
    INDEX idx_training_plans_team (team_id),
    INDEX idx_training_plans_line (line_id),
    INDEX idx_training_plans_status (status),
    INDEX idx_training_plans_month_range (month_start, month_end),
    INDEX idx_training_plans_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 4.2 TRAINING_PLAN_DETAILS (Chi tiết kế hoạch)
CREATE TABLE training_plan_details
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_plan_id BIGINT  NOT NULL,
    employee_id      BIGINT  NOT NULL,
    process_id       BIGINT  NOT NULL,
    target_month     DATE COMMENT 'Tháng thực hiện',
    planned_date     DATE COMMENT 'Ngày dự kiến',
    actual_date      DATE COMMENT 'Ngày thực hiện',
    status           ENUM ('PENDING', 'DONE', 'MISSED') DEFAULT 'PENDING',
    note             TEXT,

    delete_flag      BOOLEAN NOT NULL                   DEFAULT FALSE,
    created_at       TIMESTAMP                          DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP                          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (training_plan_id) REFERENCES training_plans (id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees (id),
    FOREIGN KEY (process_id) REFERENCES processes (id),
    INDEX idx_training_plan_details_plan (training_plan_id),
    INDEX idx_training_plan_details_employee (employee_id),
    INDEX idx_training_plan_details_process (process_id),
    INDEX idx_training_plan_details_target_month (target_month),
    INDEX idx_training_plan_details_planned_date (planned_date),
    INDEX idx_training_plan_details_status (status),
    INDEX idx_training_plan_details_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 4.3 TRAINING_PLAN_HISTORY (Lịch sử - Header)
CREATE TABLE training_plan_history
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_plan_id BIGINT  NOT NULL,
    title            TEXT,
    version          INT     NOT NULL,

    -- Snapshot
    form_code        VARCHAR(50),
    month_start      DATE,
    month_end        DATE,
    team_id          BIGINT,
    line_id          BIGINT,
    note             TEXT,
    recorded_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,

    delete_flag      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (training_plan_id) REFERENCES training_plans (id) ON DELETE CASCADE,
    INDEX idx_training_plan_history_plan (training_plan_id),
    INDEX idx_training_plan_history_version (version),
    INDEX idx_training_plan_history_delete_flag (delete_flag)
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
    process_id               BIGINT,
    target_month             DATE,
    planned_date             DATE,
    actual_date              DATE,
    status                   VARCHAR(20),
    note                     TEXT,

    delete_flag              BOOLEAN NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(255),
    updated_at               TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by               VARCHAR(255),

    FOREIGN KEY (training_plan_history_id) REFERENCES training_plan_history (id) ON DELETE CASCADE,
    INDEX idx_training_plan_dh_history (training_plan_history_id),
    INDEX idx_training_plan_dh_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 5: TRAINING RESULT (Kết quả huấn luyện)
-- ============================================================================

-- 5.1 TRAINING_RESULTS (Header kết quả)
CREATE TABLE training_results
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_plan_id BIGINT COMMENT 'Link tới kế hoạch gốc',
    title            TEXT,
    form_code        VARCHAR(50),
    year             INT     NOT NULL,
    team_id          BIGINT  NOT NULL,
    line_id          BIGINT COMMENT 'Dây chuyền áp dụng',
    status           ENUM ('ON_GOING', 'DONE', 'WAITING_MANAGER',
        'REJECTED_BY_MANAGER', 'APPROVED')
                                      DEFAULT 'ON_GOING',
    current_version  INT              DEFAULT 1,
    note             TEXT,

    delete_flag      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (training_plan_id) REFERENCES training_plans (id),
    FOREIGN KEY (team_id) REFERENCES `teams` (id),
    FOREIGN KEY (line_id) REFERENCES product_lines (id),
    INDEX idx_training_results_plan (training_plan_id),
    INDEX idx_training_results_team (team_id),
    INDEX idx_training_results_year (year),
    INDEX idx_training_results_status (status),
    INDEX idx_training_results_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 5.2 TRAINING_RESULT_DETAILS (Chi tiết kết quả - từng lần test)
CREATE TABLE training_result_details
(
    id                      BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_result_id      BIGINT  NOT NULL,
    training_plan_detail_id BIGINT  NOT NULL COMMENT 'Link về kế hoạch',

    -- Định danh ai làm gì
    employee_id             BIGINT  NOT NULL,
    process_id              BIGINT  NOT NULL,
    training_sample_id      BIGINT COMMENT 'Mẫu huấn luyện sử dụng (từ danh sách)',
    product_id              BIGINT COMMENT 'Mã sản phẩm đang chạy lúc test',
    classification          INT COMMENT 'Phân loại công đoạn',
    training_topic          VARCHAR(255) COMMENT 'Hạng mục huấn luyện bất thường (không thuộc danh sách)',
    sample_code             VARCHAR(20) COMMENT 'Mã mẫu (M1.1.1)',
    cycle_time_standard     DECIMAL(10, 2) COMMENT 'Thời gian chuẩn (giây)',

    -- Thời gian thực tế
    planned_date            DATE    NOT NULL,
    actual_date             DATE,
    time_in                 TIME COMMENT 'Giờ đưa mẫu vào',
    time_start_op           TIME COMMENT 'Giờ bắt đầu thao tác',
    time_out                TIME COMMENT 'Giờ lấy mẫu ra',
    status                  ENUM ('PENDING', 'DONE', 'NEED_SIGN', 'WAITING_SV',
        'REJECTED_BY_SV', 'APPROVED')
                                             DEFAULT 'PENDING',

    -- Kết quả
    detection_time          INT COMMENT 'Thời gian phát hiện (giây)',
    is_pass                 BOOLEAN COMMENT 'TRUE=Pass, FALSE=Fail',
    note                    TEXT COMMENT 'Ghi chú',

    -- Huấn luyện lại
    is_retrained            BOOLEAN COMMENT 'Có phải đây là huấn luyện lại?',

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

    FOREIGN KEY (training_result_id) REFERENCES training_results (id) ON DELETE CASCADE,
    FOREIGN KEY (training_plan_detail_id) REFERENCES training_plan_details (id),
    FOREIGN KEY (employee_id) REFERENCES employees (id),
    FOREIGN KEY (process_id) REFERENCES processes (id),
    FOREIGN KEY (training_sample_id) REFERENCES training_samples (id) ON DELETE SET NULL,
    FOREIGN KEY (product_id) REFERENCES products (id),
    FOREIGN KEY (signature_pro_in) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (signature_fi_in) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (signature_pro_out) REFERENCES users (id) ON DELETE SET NULL,
    FOREIGN KEY (signature_fi_out) REFERENCES users (id) ON DELETE SET NULL,
    INDEX idx_training_result_details_result (training_result_id),
    INDEX idx_training_result_details_emp_proc (employee_id, process_id),
    INDEX idx_training_result_details_actual_date (actual_date),
    INDEX idx_training_result_details_is_pass (is_pass),
    INDEX idx_training_result_details_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 5.3 TRAINING_RESULT_HISTORY (Lịch sử - Header)
CREATE TABLE training_result_history
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_result_id BIGINT,
    version            INT,
    title              TEXT,

    -- Snapshot
    year               INT,
    team_id            BIGINT,
    line_id            BIGINT,
    status_at_time     VARCHAR(50),
    note               TEXT,

    delete_flag        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    FOREIGN KEY (training_result_id) REFERENCES training_results (id) ON DELETE CASCADE,
    INDEX idx_training_result_history_result_ver (training_result_id, version),
    INDEX idx_training_result_history_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 5.4 TRAINING_RESULT_DETAIL_HISTORY (Lịch sử chi tiết)
CREATE TABLE training_result_detail_history
(
    id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
    training_result_history_id BIGINT,
    training_result_detail_id  BIGINT,

    -- Snapshot IDs
    employee_id                BIGINT,
    process_id                 BIGINT,
    training_sample_id         BIGINT,
    product_id                 BIGINT,

    training_topic             VARCHAR(255) COMMENT 'Hạng mục huấn luyện bất thường (không thuộc danh sách)',
    sample_code                VARCHAR(20) COMMENT 'Mã mẫu (M1.1.1)',
    -- Snapshot dữ liệu test
    classification             INT,
    cycle_time_standard        DECIMAL(10, 2),
    actual_date                DATE,
    time_in                    TIME,
    time_start_op              TIME,
    time_out                   TIME,
    detection_time             INT,
    is_pass                    BOOLEAN,

    -- Snapshot người ký
    signature_pro_in_name      VARCHAR(100),
    signature_fi_in_name       VARCHAR(100),
    signature_pro_out_name     VARCHAR(100),
    signature_fi_out_name      VARCHAR(100),

    delete_flag                BOOLEAN NOT NULL DEFAULT FALSE,
    created_at                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by                 VARCHAR(255),
    updated_at                 TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by                 VARCHAR(255),

    FOREIGN KEY (training_result_history_id) REFERENCES training_result_history (id) ON DELETE CASCADE,
    INDEX idx_training_result_dh_history (training_result_history_id),
    INDEX idx_training_result_dh_delete_flag (delete_flag)
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

    INDEX idx_notification_templates_delete_flag (delete_flag)
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
    INDEX idx_notification_settings_template (template_code),
    INDEX idx_notification_settings_enabled (is_enabled),
    INDEX idx_notification_settings_delete_flag (delete_flag)
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
    INDEX idx_notification_queue_recipient (recipient_user_id),
    INDEX idx_notification_queue_status (status),
    INDEX idx_notification_queue_scheduled (scheduled_at),
    INDEX idx_notification_queue_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 7: REJECTION & APPROVAL SUPPORT
-- ============================================================================

-- 7.1 REJECT_REASONS (Danh mục lý do từ chối)
CREATE TABLE reject_reasons
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(100) NOT NULL COMMENT 'Nhóm lý do (VD: Dữ liệu, Quy trình, Nội dung)',
    reason_name   VARCHAR(255) NOT NULL COMMENT 'Tên lý do (VD: Thiếu dữ liệu, Sai quy trình)',

    delete_flag   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(255),

    INDEX idx_reject_reasons_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 7.2 REQUIRED_ACTIONS (Danh mục hành động yêu cầu khi reject)
CREATE TABLE required_actions
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    action_name VARCHAR(255) NOT NULL COMMENT 'Tên hành động (VD: Chỉnh sửa và gửi lại)',

    delete_flag BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(255),
    updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(255),

    INDEX idx_required_actions_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 7.3 APPROVAL_ACTION_REJECT_REASONS (N:M giữa approval_action và reject_reason)
CREATE TABLE approval_action_reject_reasons
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    approval_action_id BIGINT  NOT NULL,
    reject_reason_id   BIGINT  NOT NULL,

    delete_flag        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    CONSTRAINT fk_aarr_approval_action
        FOREIGN KEY (approval_action_id) REFERENCES approval_actions (id) ON DELETE CASCADE,
    CONSTRAINT fk_aarr_reject_reason
        FOREIGN KEY (reject_reason_id) REFERENCES reject_reasons (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_aarr_unique (approval_action_id, reject_reason_id),
    INDEX idx_aarr_approval_action (approval_action_id),
    INDEX idx_aarr_reject_reason (reject_reason_id),
    INDEX idx_aarr_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 7.4 APPROVAL_REQUIRED_ACTIONS (N:M giữa approval_action và required_action)
CREATE TABLE approval_required_actions
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    approval_action_id BIGINT  NOT NULL,
    required_action_id BIGINT  NOT NULL,

    delete_flag        BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    CONSTRAINT fk_ara_approval_action
        FOREIGN KEY (approval_action_id) REFERENCES approval_actions (id) ON DELETE CASCADE,
    CONSTRAINT fk_ara_required_action
        FOREIGN KEY (required_action_id) REFERENCES required_actions (id) ON DELETE RESTRICT,
    UNIQUE KEY uk_ara_unique (approval_action_id, required_action_id),
    INDEX idx_ara_approval_action (approval_action_id),
    INDEX idx_ara_required_action (required_action_id),
    INDEX idx_ara_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 7.5 APPROVAL_DETAIL_COMMENTS (Góp ý chi tiết theo từng dòng khi reject)
CREATE TABLE approval_detail_comments
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    approval_action_id   BIGINT    NOT NULL,
    entity_id            BIGINT    NOT NULL COMMENT 'ID dòng detail bị góp ý',
    performed_by_user_id BIGINT    NOT NULL,
    entity_version       INT       NOT NULL,
    comment_description  TEXT,
    performed_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit environment
    ip_address           VARCHAR(45),
    user_agent           TEXT,
    device_info          VARCHAR(255),
    content_hash         VARCHAR(64) COMMENT 'SHA-256 hex of entity snapshot',

    delete_flag          BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(255),
    updated_at           TIMESTAMP          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by           VARCHAR(255),

    CONSTRAINT fk_adc_approval_action
        FOREIGN KEY (approval_action_id) REFERENCES approval_actions (id) ON DELETE CASCADE,
    CONSTRAINT fk_adc_performed_by
        FOREIGN KEY (performed_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,
    INDEX idx_adc_approval_action (approval_action_id),
    INDEX idx_adc_entity (entity_id),
    INDEX idx_adc_performed_by (performed_by_user_id),
    INDEX idx_adc_performed_at (performed_at),
    INDEX idx_adc_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- ============================================================================
-- PART 8: ANNUAL REVIEW (Kiểm tra lại mẫu huấn luyện định kỳ)
-- ============================================================================

-- 8.1 TRAINING_SAMPLE_REVIEW_CONFIGS (Cấu hình review định kỳ)
CREATE TABLE training_sample_review_configs
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_line_id BIGINT  NOT NULL,
    trigger_month   INT     NOT NULL DEFAULT 3 COMMENT 'Tháng bắt đầu review (1-12)',
    trigger_day     INT     NOT NULL DEFAULT 1 COMMENT 'Ngày bắt đầu review (1-31)',
    due_days        INT     NOT NULL DEFAULT 30 COMMENT 'Số ngày để hoàn thành review',
    assignee_id     BIGINT COMMENT 'TL phụ trách review (mặc định)',
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,

    delete_flag     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (product_line_id) REFERENCES product_lines (id),
    FOREIGN KEY (assignee_id) REFERENCES users (id),
    UNIQUE KEY uk_review_configs_product_line (product_line_id),
    INDEX idx_review_configs_active (is_active),
    INDEX idx_review_configs_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;


-- 8.2 TRAINING_SAMPLE_REVIEWS (Log từng lần review + JSON snapshot)
CREATE TABLE training_sample_reviews
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_id       BIGINT    NOT NULL,
    product_line_id BIGINT    NOT NULL,
    review_year     INT       NOT NULL COMMENT 'Năm review (2026)',
    due_date        DATE      NOT NULL COMMENT 'Hạn chót phải hoàn thành',
    completed_date  DATE COMMENT 'Ngày thực tế hoàn thành (NULL = chưa xong)',
    reviewed_by     BIGINT    NOT NULL COMMENT 'TL thực hiện review',
    result          ENUM ('PENDING', 'NO_CHANGE', 'CHANGE_PROPOSED', 'OVERDUE')
                              NOT NULL DEFAULT 'PENDING',
    sample_snapshot JSON COMMENT 'Snapshot toàn bộ training_samples tại thời điểm review',
    confirmed_by    BIGINT COMMENT 'SV xác nhận',
    confirmed_at    TIMESTAMP NULL COMMENT 'Thời điểm SV xác nhận',

    delete_flag     BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP          DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP          DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    FOREIGN KEY (config_id) REFERENCES training_sample_review_configs (id),
    FOREIGN KEY (product_line_id) REFERENCES product_lines (id),
    FOREIGN KEY (reviewed_by) REFERENCES users (id),
    FOREIGN KEY (confirmed_by) REFERENCES users (id),
    UNIQUE KEY uk_reviews_period (product_line_id, review_year),
    INDEX idx_reviews_config (config_id),
    INDEX idx_reviews_result (result),
    INDEX idx_reviews_due_date (due_date),
    INDEX idx_reviews_reviewed_by (reviewed_by),
    INDEX idx_reviews_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE approval_flow_steps
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type   VARCHAR(50)                    NOT NULL COMMENT 'e.g. DEFECT_REPORT, TRAINING_TOPIC_REPORT, TRAINING_PLAN',
    step_order    INT                            NOT NULL,
    approver_role ENUM ('SUPERVISOR', 'MANAGER') NOT NULL,
    is_active     BOOLEAN                        NOT NULL DEFAULT TRUE,

    delete_flag   BOOLEAN                        NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP                               DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_at    TIMESTAMP                               DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(255),

    UNIQUE KEY uk_approval_flow_steps_entity_order (entity_type, step_order),
    INDEX idx_approval_flow_steps_entity_active (entity_type, is_active),
    INDEX idx_approval_flow_steps_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE approval_actions
(
    id                     BIGINT PRIMARY KEY AUTO_INCREMENT,
    entity_type            VARCHAR(50)                                                                NOT NULL COMMENT 'e.g. DEFECT_REPORT, TRAINING_TOPIC_REPORT, TRAINING_PLAN',
    entity_id              BIGINT                                                                     NOT NULL,

    entity_version         INT                                                                        NOT NULL,

    -- Step order convention:
    --   -1 = REVISE (TL)
    --    0 = SUBMIT (TL)
    --    1 = SV approve/reject
    --    2 = MG approve/reject
    step_order             INT                                                                        NOT NULL,
    required_role          ENUM ('TEAM_LEADER', 'SUPERVISOR', 'MANAGER', 'FINAL_INSPECTION', 'ADMIN') NOT NULL,
    action                 ENUM ('REVISE', 'SUBMIT', 'APPROVE', 'REJECT')                             NOT NULL,

    performed_by_user_id   BIGINT                                                                     NOT NULL,
    performed_by_username  VARCHAR(50)                                                                NOT NULL,
    performed_by_full_name VARCHAR(100)                                                               NOT NULL,
    performed_by_role      ENUM ('TEAM_LEADER', 'SUPERVISOR', 'MANAGER', 'FINAL_INSPECTION', 'ADMIN') NOT NULL,

    comment                TEXT,

    performed_at           TIMESTAMP                                                                  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit environment
    ip_address             VARCHAR(45),
    user_agent             TEXT,
    device_info            VARCHAR(255),
    content_hash           VARCHAR(64) COMMENT 'SHA-256 hex of entity snapshot (header + details + version)',

    -- BaseEntity fields
    delete_flag            BOOLEAN                                                                    NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMP                                                                           DEFAULT CURRENT_TIMESTAMP,
    created_by             VARCHAR(255),
    updated_at             TIMESTAMP                                                                           DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by             VARCHAR(255),

    CONSTRAINT fk_approval_actions_user
        FOREIGN KEY (performed_by_user_id) REFERENCES users (id) ON DELETE RESTRICT,

    -- Ensures:
    --   - Only one SUBMIT per entity_version (step_order=0)
    --   - Only one REVISE record per entity_version (step_order=-1) (optional but fine)
    --   - Only one decision per step per version (step_order=1,2)
    UNIQUE KEY uk_approval_actions_entity_version_step (entity_type, entity_id, entity_version, step_order),

    INDEX idx_approval_actions_entity (entity_type, entity_id),
    INDEX idx_approval_actions_user (performed_by_user_id),
    INDEX idx_approval_actions_action (action),
    INDEX idx_approval_actions_performed_at (performed_at),
    INDEX idx_approval_actions_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- END OF SCHEMA
-- ============================================================================

