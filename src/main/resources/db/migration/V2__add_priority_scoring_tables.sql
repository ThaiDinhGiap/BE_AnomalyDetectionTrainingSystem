-- V2__add_priority_scoring_tables.sql
-- Priority Scoring System tables

CREATE TABLE priority_policies
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_code     VARCHAR(50) UNIQUE           NOT NULL,
    policy_name     VARCHAR(100)                 NOT NULL,
    entity_type     ENUM ('EMPLOYEE', 'PROCESS') NOT NULL,
    effective_date  DATE                         NOT NULL,
    expiration_date DATE,
    status          ENUM ('DRAFT', 'ACTIVE', 'ARCHIVED')  DEFAULT 'DRAFT',
    description     TEXT,

    delete_flag     BOOLEAN                      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP                             DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(255),
    updated_at      TIMESTAMP                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(255),

    INDEX idx_priority_policies_type_status (entity_type, status),
    INDEX idx_priority_policies_effective (effective_date, expiration_date),
    INDEX idx_priority_policies_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE priority_tiers
(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    policy_id           BIGINT               NOT NULL,
    tier_order          INT                  NOT NULL,
    tier_name           VARCHAR(100)         NOT NULL,
    filter_logic        ENUM ('AND', 'OR')   NOT NULL DEFAULT 'AND' COMMENT 'Thoả mãn TẤT CẢ hay MỘT TRONG',

    ranking_metric      VARCHAR(50)          NOT NULL,
    ranking_direction   ENUM ('ASC', 'DESC') NOT NULL,

    secondary_metric    VARCHAR(50),
    secondary_direction ENUM ('ASC', 'DESC'),

    is_active           BOOLEAN                       DEFAULT TRUE,

    delete_flag         BOOLEAN              NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_at          TIMESTAMP                     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255),

    FOREIGN KEY (policy_id) REFERENCES priority_policies (id) ON DELETE CASCADE,
    UNIQUE KEY uk_priority_tiers_policy_order (policy_id, tier_order),
    INDEX idx_priority_tiers_policy (policy_id),
    INDEX idx_priority_tiers_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE priority_tier_filters
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT,
    tier_id      BIGINT                                       NOT NULL,
    metric_name  VARCHAR(50)                                  NOT NULL COMMENT 'Tên metric (VD: days_since_last_training)',
    operator     ENUM ('GT', 'GTE', 'LT', 'LTE', 'EQ', 'NEQ') NOT NULL COMMENT '>, >=, <, <=, =, !=',
    filter_value VARCHAR(100)                                 NOT NULL COMMENT 'Giá trị so sánh',
    filter_unit  VARCHAR(20) COMMENT 'Đơn vị (Ngày, %, True/False)',
    filter_order INT                                          NOT NULL DEFAULT 0,

    delete_flag  BOOLEAN                                      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP                                             DEFAULT CURRENT_TIMESTAMP,
    created_by   VARCHAR(255),
    updated_at   TIMESTAMP                                             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by   VARCHAR(255),

    FOREIGN KEY (tier_id) REFERENCES priority_tiers (id) ON DELETE CASCADE,
    INDEX idx_priority_tier_filters_tier (tier_id),
    INDEX idx_priority_tier_filters_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE computed_metrics
(
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    metric_name        VARCHAR(50) UNIQUE                                      NOT NULL,
    display_name       VARCHAR(100)                                            NOT NULL,
    entity_type        ENUM ('EMPLOYEE', 'PROCESS')                            NOT NULL,
    compute_method     ENUM ('PROPERTY', 'SQL', 'EXTENSION', 'CLASSIFICATION') NOT NULL,
    compute_definition VARCHAR(2000)                                           NOT NULL,
    return_type        ENUM ('INT', 'DECIMAL', 'BOOLEAN', 'STRING')            NOT NULL,
    unit               VARCHAR(20) COMMENT 'Đơn vị hiển thị (Ngày, %, True/False)',
    description        TEXT,
    is_active          BOOLEAN                                                          DEFAULT TRUE,

    delete_flag        BOOLEAN                                                 NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP                                                        DEFAULT CURRENT_TIMESTAMP,
    created_by         VARCHAR(255),
    updated_at         TIMESTAMP                                                        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by         VARCHAR(255),

    INDEX idx_computed_metrics_entity (entity_type, is_active),
    INDEX idx_computed_metrics_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE metric_classifications
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
    classification_name  VARCHAR(50)  NOT NULL,
    metric_source        VARCHAR(50)  NOT NULL,
    condition_expression VARCHAR(500) NOT NULL,
    output_level         INT          NOT NULL,
    output_label         VARCHAR(50),
    priority             INT          NOT NULL,
    is_active            BOOLEAN               DEFAULT TRUE,

    delete_flag          BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by           VARCHAR(255),
    updated_at           TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by           VARCHAR(255),

    INDEX idx_metric_classifications_name (classification_name, priority),
    INDEX idx_metric_classifications_delete_flag (delete_flag)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Seed data for computed_metrics
INSERT INTO computed_metrics (metric_name, display_name, entity_type, compute_method, compute_definition, return_type,
                              unit, description, is_active, created_by)
VALUES ('days_since_last_training', 'Thời gian chưa huấn luyện', 'EMPLOYEE', 'SQL',
        'SELECT DATEDIFF(CURDATE(), MAX(trd.actual_date)) FROM training_result_details trd WHERE trd.employee_id = :entityId AND trd.is_pass = TRUE AND trd.delete_flag = FALSE',
        'INT', 'Ngày', 'Số ngày kể từ lần huấn luyện đạt gần nhất', TRUE, 'system'),
       ('fail_rate', 'Tỷ lệ trượt huấn luyện', 'EMPLOYEE', 'SQL',
        'SELECT ROUND(COUNT(CASE WHEN trd.is_pass = FALSE THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 2) FROM training_result_details trd WHERE trd.employee_id = :entityId AND trd.delete_flag = FALSE',
        'DECIMAL', '%', 'Tỷ lệ phần trăm các lần huấn luyện không đạt', TRUE, 'system'),
       ('years_of_service', 'Thâm niên làm việc', 'EMPLOYEE', 'PROPERTY', 'employee.yearsOfService', 'INT', 'Năm',
        'Số năm làm việc tại công ty', TRUE, 'system'),
       ('is_on_watchlist', 'Nằm trong danh sách theo dõi', 'EMPLOYEE', 'PROPERTY', 'employee.isOnWatchlist', 'BOOLEAN',
        'True/False', 'Nhân viên có nằm trong danh sách theo dõi đặc biệt', TRUE, 'system'),
       ('process_classification', 'Phân loại công đoạn', 'PROCESS', 'CLASSIFICATION', 'classification_level', 'INT',
        'Cấp', 'Phân loại mức độ quan trọng của công đoạn (1-4)', TRUE, 'system'),
       ('total_defects', 'Tổng số lỗi', 'PROCESS', 'SQL',
        'SELECT COUNT(*) FROM defects d WHERE d.process_id = :entityId AND d.delete_flag = FALSE', 'INT', 'Lỗi',
        'Tổng số lỗi đã ghi nhận tại công đoạn', TRUE, 'system');
