-- ============================================================================
-- V3__e_sign_approval_core.sql
-- Purpose:
--   - Add PIN-based e-sign support (user_signature_pins)
--   - Add configurable approval flow steps per entity_type (approval_flow_steps)
--   - Add approval action log with versioning (approval_actions)
-- Notes:
--   - Only 2 approval levels: SUPERVISOR (step_order=1) -> MANAGER (step_order=2)
--   - Use ReportStatus convention:
--       DRAFT -> WAITING_SV -> WAITING_MANAGER -> APPROVED
--       REJECTED_BY_SV / REJECTED_BY_MANAGER, then TL must REVISE to return to DRAFT
--   - approval_actions.step_order convention:
--       -1 = REVISE (TL)
--        0 = SUBMIT (TL)
--        1 = SV APPROVE/REJECT
--        2 = MG APPROVE/REJECT
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1) USER SIGNATURE PINs (PIN-only e-sign)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS user_signature_pins;

CREATE TABLE user_signature_pins (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                     user_id BIGINT NOT NULL UNIQUE,

    -- Store PIN using strong hashing (bcrypt recommended)
                                     pin_hash VARCHAR(255) NOT NULL,

    -- Anti-bruteforce
                                     failed_attempts INT NOT NULL DEFAULT 0,
                                     locked_until TIMESTAMP NULL,

    -- Rotation policy (optional but recommended)
                                     last_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     expires_at TIMESTAMP NULL,

    -- BaseEntity fields
                                     delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(255),
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     updated_by VARCHAR(255),

                                     CONSTRAINT fk_user_signature_pins_user
                                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

                                     INDEX idx_user_signature_pins_user (user_id),
                                     INDEX idx_user_signature_pins_locked (locked_until),
                                     INDEX idx_user_signature_pins_delete_flag (delete_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- 2) APPROVAL FLOW CONFIG (entity_type-only)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS approval_flow_steps;

CREATE TABLE approval_flow_steps (
                                     id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Which entity is governed by this workflow
                                     entity_type VARCHAR(50) NOT NULL COMMENT 'e.g. DEFECT_REPORT, TRAINING_TOPIC_REPORT, TRAINING_PLAN',

    -- Order in chain: 1=SUPERVISOR, 2=MANAGER (fixed in current scope)
                                     step_order INT NOT NULL,

    -- Required role to perform this step
                                     approver_role ENUM('SUPERVISOR', 'MANAGER') NOT NULL,

                                     is_active BOOLEAN NOT NULL DEFAULT TRUE,

    -- BaseEntity fields
                                     delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by VARCHAR(255),
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                     updated_by VARCHAR(255),

                                     UNIQUE KEY uk_approval_flow_steps_entity_order (entity_type, step_order),
                                     INDEX idx_approval_flow_steps_entity_active (entity_type, is_active),
                                     INDEX idx_approval_flow_steps_delete_flag (delete_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- 3) APPROVAL ACTIONS LOG (versioned, audit-friendly)
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS approval_actions;

CREATE TABLE approval_actions (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,

    -- Target entity
                                  entity_type VARCHAR(50) NOT NULL COMMENT 'e.g. DEFECT_REPORT, TRAINING_TOPIC_REPORT, TRAINING_PLAN',
                                  entity_id BIGINT NOT NULL,

    -- Snapshot version at the time of action (supports "sign multiple times by version")
                                  entity_version INT NOT NULL,

    -- Step order convention:
    --   -1 = REVISE (TL)
    --    0 = SUBMIT (TL)
    --    1 = SV approve/reject
    --    2 = MG approve/reject
                                  step_order INT NOT NULL,

    -- Snapshot of required role (for audit; config can change later)
                                  required_role ENUM('TEAM_LEADER', 'SUPERVISOR', 'MANAGER', 'FINAL_INSPECTION', 'ADMIN') NOT NULL,

                                  action ENUM('REVISE', 'SUBMIT', 'APPROVE', 'REJECT') NOT NULL,

                                  performed_by_user_id BIGINT NOT NULL,
                                  performed_by_username VARCHAR(50) NOT NULL,
                                  performed_by_full_name VARCHAR(100) NOT NULL,
                                  performed_by_role ENUM('TEAM_LEADER', 'SUPERVISOR', 'MANAGER', 'FINAL_INSPECTION', 'ADMIN') NOT NULL,

                                  comment TEXT,
                                  reject_reason TEXT,

                                  performed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Audit environment
                                  ip_address VARCHAR(45),
                                  user_agent TEXT,
                                  device_info VARCHAR(255),

    -- Integrity (hash of canonical content at signing/submitting time)
                                  content_hash CHAR(64) COMMENT 'SHA-256 hex of entity snapshot (header + details + version)',

    -- BaseEntity fields
                                  delete_flag BOOLEAN NOT NULL DEFAULT FALSE,
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  created_by VARCHAR(255),
                                  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                  updated_by VARCHAR(255),

                                  CONSTRAINT fk_approval_actions_user
                                      FOREIGN KEY (performed_by_user_id) REFERENCES users(id) ON DELETE RESTRICT,

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- 4) SEED DEFAULT FLOWS (SV -> MANAGER) for core entity types
-- ----------------------------------------------------------------------------
-- You can add more entity_type rows later without code changes.

INSERT INTO approval_flow_steps (entity_type, step_order, approver_role, is_active, created_by)
VALUES
-- DEFECT_REPORT: SV -> MANAGER
('DEFECT_REPORT', 1, 'SUPERVISOR', TRUE, 'system'),
('DEFECT_REPORT', 2, 'MANAGER', TRUE, 'system'),

-- TRAINING_TOPIC_REPORT: SV -> MANAGER
('TRAINING_TOPIC_REPORT', 1, 'SUPERVISOR', TRUE, 'system'),
('TRAINING_TOPIC_REPORT', 2, 'MANAGER', TRUE, 'system'),

-- TRAINING_PLAN: SV -> MANAGER
('TRAINING_PLAN', 1, 'SUPERVISOR', TRUE, 'system'),
('TRAINING_PLAN', 2, 'MANAGER', TRUE, 'system');

SET FOREIGN_KEY_CHECKS = 1;