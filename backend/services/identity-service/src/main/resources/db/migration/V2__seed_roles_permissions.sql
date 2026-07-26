-- V2__seed_roles_permissions.sql

-- 1. Seed starter system roles
INSERT INTO roles (id, name, description, is_system_role, created_by, updated_by)
VALUES 
(1, 'SUPER_ADMIN', 'Super administrator with all permissions', TRUE, 'SYSTEM', 'SYSTEM'),
(2, 'ADMIN', 'General administrator with operations permissions', TRUE, 'SYSTEM', 'SYSTEM'),
(3, 'DONOR', 'Blood donor user', TRUE, 'SYSTEM', 'SYSTEM'),
(4, 'HOSPITAL', 'Hospital staff user', TRUE, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 2. Seed domain permissions for all microservice controllers
INSERT INTO permissions (id, code, description, module, created_by, updated_by)
VALUES
(1, 'USER_MANAGE', 'Manage application users and roles', 'USER', 'SYSTEM', 'SYSTEM'),
(2, 'STAFF_MANAGE', 'Manage staff profiles and assignments', 'USER', 'SYSTEM', 'SYSTEM'),
(3, 'DONOR_CREATE', 'Register new blood donors', 'DONOR', 'SYSTEM', 'SYSTEM'),
(4, 'DONOR_MANAGE', 'Update and manage blood donor profiles', 'DONOR', 'SYSTEM', 'SYSTEM'),
(5, 'DONOR_VIEW', 'View blood donor details and history', 'DONOR', 'SYSTEM', 'SYSTEM'),
(6, 'MASTER_MANAGE', 'Manage master reference lookup data', 'MASTER', 'SYSTEM', 'SYSTEM'),
(7, 'HOSPITAL_MANAGE', 'Manage hospital profiles and locations', 'HOSPITAL', 'SYSTEM', 'SYSTEM'),
(8, 'HOSPITAL_VIEW', 'View hospital information', 'HOSPITAL', 'SYSTEM', 'SYSTEM'),
(9, 'BLOOD_BANK_MANAGE', 'Manage blood bank facilities', 'BLOODBANK', 'SYSTEM', 'SYSTEM'),
(10, 'BLOOD_BANK_VIEW', 'View blood bank facility information', 'BLOODBANK', 'SYSTEM', 'SYSTEM'),
(11, 'BLOOD_STOCK_MANAGE', 'Update blood inventory stock levels', 'INVENTORY', 'SYSTEM', 'SYSTEM'),
(12, 'BLOOD_STOCK_VIEW', 'View blood inventory stock levels', 'INVENTORY', 'SYSTEM', 'SYSTEM'),
(13, 'DONATION_CREATE', 'Log new blood donations', 'DONATION', 'SYSTEM', 'SYSTEM'),
(14, 'DONATION_APPROVE', 'Approve or reject blood donations', 'DONATION', 'SYSTEM', 'SYSTEM'),
(15, 'DONATION_VIEW', 'View blood donation records', 'DONATION', 'SYSTEM', 'SYSTEM'),
(16, 'REQUEST_CREATE', 'Submit blood requests', 'TRANSACTION', 'SYSTEM', 'SYSTEM'),
(17, 'REQUEST_APPROVE', 'Approve or reject blood requests', 'TRANSACTION', 'SYSTEM', 'SYSTEM'),
(18, 'REQUEST_FULFILL', 'Issue blood units for requests', 'TRANSACTION', 'SYSTEM', 'SYSTEM'),
(19, 'REQUEST_VIEW', 'View blood requests and issuances', 'TRANSACTION', 'SYSTEM', 'SYSTEM'),
(20, 'NOTIFICATION_TEMPLATE_MANAGE', 'Manage notification templates', 'NOTIFICATION', 'SYSTEM', 'SYSTEM'),
(21, 'NOTIFICATION_VIEW', 'View notification delivery audit logs', 'NOTIFICATION', 'SYSTEM', 'SYSTEM'),
(22, 'BLOODBANK_MANAGE', 'Legacy blood bank manage permission', 'BLOODBANK', 'SYSTEM', 'SYSTEM'),
(23, 'INVENTORY_VIEW', 'Legacy inventory view permission', 'INVENTORY', 'SYSTEM', 'SYSTEM'),
(24, 'INVENTORY_MANAGE', 'Legacy inventory manage permission', 'INVENTORY', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE description = VALUES(description), module = VALUES(module);

-- 3. Map all permissions to SUPER_ADMIN role (role_id = 1)
INSERT INTO role_permissions (role_id, permission_id, created_by, updated_by)
VALUES
(1, 1, 'SYSTEM', 'SYSTEM'), (1, 2, 'SYSTEM', 'SYSTEM'), (1, 3, 'SYSTEM', 'SYSTEM'),
(1, 4, 'SYSTEM', 'SYSTEM'), (1, 5, 'SYSTEM', 'SYSTEM'), (1, 6, 'SYSTEM', 'SYSTEM'),
(1, 7, 'SYSTEM', 'SYSTEM'), (1, 8, 'SYSTEM', 'SYSTEM'), (1, 9, 'SYSTEM', 'SYSTEM'),
(1, 10, 'SYSTEM', 'SYSTEM'), (1, 11, 'SYSTEM', 'SYSTEM'), (1, 12, 'SYSTEM', 'SYSTEM'),
(1, 13, 'SYSTEM', 'SYSTEM'), (1, 14, 'SYSTEM', 'SYSTEM'), (1, 15, 'SYSTEM', 'SYSTEM'),
(1, 16, 'SYSTEM', 'SYSTEM'), (1, 17, 'SYSTEM', 'SYSTEM'), (1, 18, 'SYSTEM', 'SYSTEM'),
(1, 19, 'SYSTEM', 'SYSTEM'), (1, 20, 'SYSTEM', 'SYSTEM'), (1, 21, 'SYSTEM', 'SYSTEM'),
(1, 22, 'SYSTEM', 'SYSTEM'), (1, 23, 'SYSTEM', 'SYSTEM'), (1, 24, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE updated_by = VALUES(updated_by);

-- 4. Map operational permissions to ADMIN role (role_id = 2)
INSERT INTO role_permissions (role_id, permission_id, created_by, updated_by)
VALUES
(2, 1, 'SYSTEM', 'SYSTEM'), (2, 2, 'SYSTEM', 'SYSTEM'), (2, 3, 'SYSTEM', 'SYSTEM'),
(2, 4, 'SYSTEM', 'SYSTEM'), (2, 5, 'SYSTEM', 'SYSTEM'), (2, 6, 'SYSTEM', 'SYSTEM'),
(2, 7, 'SYSTEM', 'SYSTEM'), (2, 8, 'SYSTEM', 'SYSTEM'), (2, 9, 'SYSTEM', 'SYSTEM'),
(2, 10, 'SYSTEM', 'SYSTEM'), (2, 11, 'SYSTEM', 'SYSTEM'), (2, 12, 'SYSTEM', 'SYSTEM'),
(2, 13, 'SYSTEM', 'SYSTEM'), (2, 14, 'SYSTEM', 'SYSTEM'), (2, 15, 'SYSTEM', 'SYSTEM'),
(2, 16, 'SYSTEM', 'SYSTEM'), (2, 17, 'SYSTEM', 'SYSTEM'), (2, 18, 'SYSTEM', 'SYSTEM'),
(2, 19, 'SYSTEM', 'SYSTEM'), (2, 20, 'SYSTEM', 'SYSTEM'), (2, 21, 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE updated_by = VALUES(updated_by);
