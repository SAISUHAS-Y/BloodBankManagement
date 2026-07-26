-- V5__seed_default_admin.sql

INSERT INTO users (id, username, email, password_hash, full_name, is_active, is_locked, created_by, updated_by)
VALUES (1, 'admin', 'admin@bloodbank.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System Admin', TRUE, FALSE, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO user_roles (user_id, role_id, created_by, updated_by)
VALUES (1, 1, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);
