-- V8__add_staff_status_and_hierarchy.sql

ALTER TABLE staff_profiles 
    ADD COLUMN staff_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN reporting_manager_id BIGINT;

ALTER TABLE staff_profiles
    ADD CONSTRAINT fk_staff_reporting_manager FOREIGN KEY (reporting_manager_id) REFERENCES staff_profiles(id);

CREATE INDEX idx_staff_status ON staff_profiles(staff_status);
CREATE INDEX idx_staff_reporting_manager ON staff_profiles(reporting_manager_id);
