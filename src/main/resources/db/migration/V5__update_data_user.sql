ALTER TABLE users
    MODIFY COLUMN role ENUM ('MANAGER', 'SUPERVISOR', 'TEAM_LEADER', 'FINAL_INSPECTION', 'ADMIN') NOT NULL;

UPDATE users
SET password_hash = '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO';

INSERT INTO users (username, email, password_hash, full_name, role, is_active, created_by)
VALUES ('admin',
        'admin@sep490.com',
        '$2a$10$FBYVLpW91kJ0ZlradmOB/ujON1kXKLH6UKfbr2eQLNnJX0uB/6RaO',
        'System Administrator',
        'ADMIN',
        TRUE,
        'system');