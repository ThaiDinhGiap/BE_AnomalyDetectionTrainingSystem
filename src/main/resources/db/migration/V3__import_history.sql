CREATE TABLE import_histories (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                import_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                user_id BIGINT NOT NULL,
                                status ENUM('PASS', 'FAIL') NOT NULL,
                                file_path VARCHAR(255),
                                import_type VARCHAR(50) NOT NULL,
                                import_error_description JSON,

                                delete_flag BOOLEAN               NOT NULL DEFAULT FALSE,
                                created_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
                                created_by  VARCHAR(255),
                                updated_at  TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                updated_by  VARCHAR(255),

                                CONSTRAINT fk_import_history_user
                                    FOREIGN KEY (user_id) REFERENCES users(id),

                                INDEX idx_import_history_user_id (user_id),
                                INDEX idx_import_history_import_type (import_type),
                                INDEX idx_import_history_import_date (import_date)
);