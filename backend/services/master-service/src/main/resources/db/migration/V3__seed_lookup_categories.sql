-- 1. Seed Categories
INSERT INTO lookup_categories (code, name, description, is_system_category, created_by, updated_by) VALUES
('GENDER', 'Gender', 'Gender lookup category', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('DOCUMENT_TYPE', 'Document Type', 'Supported KYC/registration document types', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('TITLE', 'Title', 'User titles', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('ID_TYPE', 'ID Type', 'Identity document categorization', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('DONATION_TYPE', 'Donation Type', 'Available blood/component donation procedures', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('DEFERRAL_REASON', 'Deferral Reason', 'Reasons for donor deferrals with deferral days metadata', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('MARITAL_STATUS', 'Marital Status', 'Marital status options', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('RELATIONSHIP', 'Relationship', 'Emergency contact relationships', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('COMPONENT_TYPE', 'Component Type', 'Blood component separation options', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('HOSPITAL_TYPE', 'Hospital Type', 'Hospital type classifications', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
('BLOOD_BANK_TYPE', 'Blood Bank Type', 'Blood bank type classifications', TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- 2. Seed Items for GENDER (Category ID = 1)
SET @cat_gender = (SELECT id FROM lookup_categories WHERE code = 'GENDER');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_gender, 'MALE', 'Male', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_gender, 'FEMALE', 'Female', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_gender, 'OTHER', 'Other', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for DOCUMENT_TYPE
SET @cat_doc = (SELECT id FROM lookup_categories WHERE code = 'DOCUMENT_TYPE');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_doc, 'AADHAAR', 'Aadhaar Card', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_doc, 'PAN', 'PAN Card', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_doc, 'DRIVING_LICENSE', 'Driving License', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_doc, 'PASSPORT', 'Passport', 40, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for TITLE
SET @cat_title = (SELECT id FROM lookup_categories WHERE code = 'TITLE');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_title, 'MR', 'Mr.', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_title, 'MRS', 'Mrs.', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_title, 'MS', 'Ms.', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_title, 'DR', 'Dr.', 40, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for ID_TYPE
SET @cat_id_type = (SELECT id FROM lookup_categories WHERE code = 'ID_TYPE');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_id_type, 'GOVERNMENT_ID', 'Government Issued ID', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_id_type, 'STUDENT_ID', 'Student Identification Card', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_id_type, 'EMPLOYEE_ID', 'Corporate Employee ID', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for DONATION_TYPE
SET @cat_don_type = (SELECT id FROM lookup_categories WHERE code = 'DONATION_TYPE');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, metadata, created_by, updated_by) VALUES
(@cat_don_type, 'WHOLE_BLOOD', 'Whole Blood', 10, TRUE, '{"minIntervalDays": 90}', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_don_type, 'PLATELETS', 'Platelets (Apheresis)', 20, TRUE, '{"minIntervalDays": 7}', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_don_type, 'PLASMA', 'Plasma (Apheresis)', 30, TRUE, '{"minIntervalDays": 28}', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_don_type, 'DOUBLE_RED_CELLS', 'Double Red Cells', 40, TRUE, '{"minIntervalDays": 112}', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for DEFERRAL_REASON (cater category specific attributes like deferralDays using metadata JSON column)
SET @cat_defer = (SELECT id FROM lookup_categories WHERE code = 'DEFERRAL_REASON');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, metadata, created_by, updated_by) VALUES
(@cat_defer, 'LOW_HEMOGLOBIN', 'Low Hemoglobin Level', 10, TRUE, '{"deferralDays": 28}', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_defer, 'TATTOO', 'Recent Tattoo or Piercing', 20, TRUE, '{"deferralDays": 180}', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_defer, 'MEDICATION', 'Under Restricted Medication', 30, TRUE, '{"deferralDays": 14}', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_defer, 'TRAVEL', 'Recent Travel to Malaria/Dengue Endemic Area', 40, TRUE, '{"deferralDays": 90}', 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_defer, 'UNDERWEIGHT', 'Body Weight Below 45kg', 50, TRUE, '{"deferralDays": 0}', 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for MARITAL_STATUS
SET @cat_marital = (SELECT id FROM lookup_categories WHERE code = 'MARITAL_STATUS');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_marital, 'SINGLE', 'Single', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_marital, 'MARRIED', 'Married', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_marital, 'DIVORCED', 'Divorced', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_marital, 'WIDOWED', 'Widowed', 40, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for RELATIONSHIP
SET @cat_rel = (SELECT id FROM lookup_categories WHERE code = 'RELATIONSHIP');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_rel, 'SPOUSE', 'Spouse', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_rel, 'PARENT', 'Parent', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_rel, 'CHILD', 'Child', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_rel, 'SIBLING', 'Sibling', 40, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_rel, 'OTHER', 'Other', 50, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for COMPONENT_TYPE
SET @cat_comp = (SELECT id FROM lookup_categories WHERE code = 'COMPONENT_TYPE');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_comp, 'RED_BLOOD_CELLS', 'Packed Red Blood Cells (PRBC)', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_comp, 'FRESH_FROZEN_PLASMA', 'Fresh Frozen Plasma (FFP)', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_comp, 'PLATELETS', 'Random Donor Platelets (RDP)', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_comp, 'CRYOPRECIPITATE', 'Cryoprecipitate', 40, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for HOSPITAL_TYPE
SET @cat_hosp_type = (SELECT id FROM lookup_categories WHERE code = 'HOSPITAL_TYPE');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_hosp_type, 'GOVERNMENT', 'Government', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_hosp_type, 'PRIVATE', 'Private', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_hosp_type, 'TRUST', 'Trust', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');

-- Seed Items for BLOOD_BANK_TYPE
SET @cat_bb_type = (SELECT id FROM lookup_categories WHERE code = 'BLOOD_BANK_TYPE');
INSERT INTO lookup_items (category_id, code, label, sort_order, is_active, created_by, updated_by) VALUES
(@cat_bb_type, 'RED_CROSS', 'Red Cross', 10, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_bb_type, 'COMMUNITY', 'Community Based', 20, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED'),
(@cat_bb_type, 'HOSPITAL_BASED', 'Hospital Based', 30, TRUE, 'SYSTEM_SEED', 'SYSTEM_SEED');
