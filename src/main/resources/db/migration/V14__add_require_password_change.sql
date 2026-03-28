-- ============================================================================
-- V14: Add require_password_change and allow nullable email
-- ============================================================================

-- 1. Make email column nullable
ALTER TABLE users MODIFY COLUMN email VARCHAR(100) NULL;

-- 2. Add require_password_change column
ALTER TABLE users ADD COLUMN require_password_change BOOLEAN DEFAULT FALSE NOT NULL;
