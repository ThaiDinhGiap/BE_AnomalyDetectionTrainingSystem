CREATE TABLE priority_snapshots
(
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id          BIGINT  NOT NULL,
    policy_id        BIGINT  NOT NULL,
    policy_snapshot  JSON    NOT NULL,

    training_plan_id BIGINT  NULL,

    delete_flag      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(255),
    updated_at       TIMESTAMP        DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(255),

    FOREIGN KEY (team_id) REFERENCES teams (id),
    FOREIGN KEY (policy_id) REFERENCES priority_policies (id),
    FOREIGN KEY (training_plan_id) REFERENCES training_plans (id)
) ENGINE = InnoDB;

CREATE TABLE priority_snapshot_details
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_id   BIGINT       NOT NULL,
    employee_id   BIGINT       NOT NULL,
    employee_code VARCHAR(20)  NOT NULL,
    full_name     VARCHAR(100) NOT NULL,

    tier_order    INT          NOT NULL,
    tier_name     VARCHAR(100),
    sort_rank     INT          NOT NULL,
    priority_tags JSON,
    metric_values JSON         NOT NULL,

    delete_flag   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(255),
    updated_at    TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(255),

    FOREIGN KEY (snapshot_id) REFERENCES priority_snapshots (id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES employees (id),
    INDEX idx_psd_snapshot (snapshot_id),
    INDEX idx_psd_rank (snapshot_id, sort_rank)
) ENGINE = InnoDB;