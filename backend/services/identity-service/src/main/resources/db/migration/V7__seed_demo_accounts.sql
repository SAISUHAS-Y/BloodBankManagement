-- V7__seed_demo_accounts.sql

-- Ensure all quick demo accounts exist with active status and password 'Password123!'

INSERT INTO users (id, username, email, password_hash, full_name, is_active, is_locked, must_change_password, created_by, updated_by)
VALUES 
(1, 'admin', 'admin@bloodbank.org', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'System Administrator', TRUE, FALSE, FALSE, 'SYSTEM', 'SYSTEM'),
(2, 'hospital_staff', 'staff@cityhospital.org', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Hospital Staff Member', TRUE, FALSE, FALSE, 'SYSTEM', 'SYSTEM'),
(3, 'bank_staff', 'operator@bloodcenter.org', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Blood Bank Operator', TRUE, FALSE, FALSE, 'SYSTEM', 'SYSTEM'),
(4, 'donor_user', 'donor@bloodbank.org', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Voluntary Donor User', TRUE, FALSE, FALSE, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE 
    is_active = TRUE,
    is_locked = FALSE,
    must_change_password = FALSE;

-- Assign roles to demo accounts
INSERT INTO user_roles (user_id, role_id, created_by, updated_by)
VALUES 
(1, 1, 'SYSTEM', 'SYSTEM'), -- SUPER_ADMIN
(2, 4, 'SYSTEM', 'SYSTEM'), -- HOSPITAL
(3, 2, 'SYSTEM', 'SYSTEM'), -- ADMIN
(4, 3, 'SYSTEM', 'SYSTEM')  -- DONOR
ON DUPLICATE KEY UPDATE updated_by = VALUES(updated_by);
