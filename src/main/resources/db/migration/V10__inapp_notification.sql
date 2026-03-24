CREATE TABLE in_app_notifications
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_id        BIGINT       NOT NULL,
    title               VARCHAR(150) NOT NULL,
    message             TEXT         NOT NULL,
    type                VARCHAR(30)  NOT NULL DEFAULT 'INFO',
    is_read             BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at             DATETIME,
    related_entity_type VARCHAR(50),
    related_entity_id   BIGINT,
    action_url          VARCHAR(500),

    delete_flag         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(255),
    updated_at          TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by          VARCHAR(255),

    CONSTRAINT fk_ian_recipient FOREIGN KEY (recipient_id) REFERENCES users (id),

    INDEX idx_ian_recipient_created (recipient_id, created_at),
    INDEX idx_ian_recipient_read (recipient_id, is_read)
);