-- V4__add_unique_registration_number.sql
ALTER TABLE hospitals ADD CONSTRAINT uk_hospital_reg_num UNIQUE (registration_number);
