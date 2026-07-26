-- 1. Seed States
INSERT INTO states (name, code, created_by, updated_by) VALUES
('Karnataka', 'KA', 'SYSTEM_SEED', 'SYSTEM_SEED'),
('Maharashtra', 'MH', 'SYSTEM_SEED', 'SYSTEM_SEED'),
('Delhi', 'DL', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- 2. Seed Districts for Karnataka (KA)
SET @state_ka = (SELECT id FROM states WHERE code = 'KA');
INSERT INTO districts (state_id, name, created_by, updated_by) VALUES
(@state_ka, 'Bengaluru Urban', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@state_ka, 'Mysuru', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Districts for Maharashtra (MH)
SET @state_mh = (SELECT id FROM states WHERE code = 'MH');
INSERT INTO districts (state_id, name, created_by, updated_by) VALUES
(@state_mh, 'Mumbai Suburban', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@state_mh, 'Pune', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- 3. Seed Cities for Bengaluru Urban (District)
SET @dist_blr = (SELECT id FROM districts WHERE name = 'Bengaluru Urban');
INSERT INTO cities (district_id, name, pincode, created_by, updated_by) VALUES
(@dist_blr, 'Bengaluru', '560001', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@dist_blr, 'Whitefield', '560066', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@dist_blr, 'Electronic City', '560100', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Cities for Mysuru (District)
SET @dist_mys = (SELECT id FROM districts WHERE name = 'Mysuru');
INSERT INTO cities (district_id, name, pincode, created_by, updated_by) VALUES
(@dist_mys, 'Mysuru', '570001', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Cities for Mumbai Suburban (District)
SET @dist_mum = (SELECT id FROM districts WHERE name = 'Mumbai Suburban');
INSERT INTO cities (district_id, name, pincode, created_by, updated_by) VALUES
(@dist_mum, 'Mumbai', '400001', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@dist_mum, 'Andheri', '400053', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Cities for Pune (District)
SET @dist_pune = (SELECT id FROM districts WHERE name = 'Pune');
INSERT INTO cities (district_id, name, pincode, created_by, updated_by) VALUES
(@dist_pune, 'Pune', '411001', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@dist_pune, 'Hinjewadi', '411057', 'SYSTEM_SEED', 'SYSTEM_SEED');
