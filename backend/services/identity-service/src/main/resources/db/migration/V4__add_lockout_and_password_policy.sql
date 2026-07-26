-- V4__add_lockout_and_password_policy.sql

ALTER TABLE users 
ADD COLUMN failed_login_attempts INT NOT NULL DEFAULT 0,
ADD COLUMN locked_until TIMESTAMP NULL,
ADD COLUMN must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

-- Seed default admin user with must_change_password = TRUE
INSERT INTO users (id, username, email, password_hash, full_name, phone, is_active, is_locked, must_change_password, created_by, updated_by)
VALUES (1, 'admin', 'admin@bloodbank.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymY04c6L5b9gZ.rZ6/8tKG', 'System Administrator', '0000000000', TRUE, FALSE, TRUE, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE must_change_password = TRUE;

INSERT INTO user_roles (user_id, role_id, created_by, updated_by)
VALUES (1, 1, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE updated_by = VALUES(updated_by);
