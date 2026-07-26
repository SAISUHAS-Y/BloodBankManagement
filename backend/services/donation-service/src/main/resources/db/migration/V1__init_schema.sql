-- Create Eligibility Checks Table
CREATE TABLE eligibility_checks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_profile_id BIGINT NOT NULL,
    checked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hemoglobin_level DOUBLE NOT NULL,
    weight_kg DOUBLE NOT NULL,
    systolic_bp DOUBLE NOT NULL,
    diastolic_bp DOUBLE NOT NULL,
    is_eligible BOOLEAN NOT NULL,
    deferral_reason_code VARCHAR(50),
    deferred_until_date DATE,
    checked_by BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_eligibility_donor ON eligibility_checks(donor_profile_id);

-- Create Donation Records Table
CREATE TABLE donations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    donor_profile_id BIGINT NOT NULL,
    blood_bank_id BIGINT NOT NULL,
    eligibility_check_id BIGINT NOT NULL,
    blood_group_id BIGINT NOT NULL,
    component_type_code VARCHAR(50) NOT NULL,
    units_collected DOUBLE NOT NULL,
    donation_status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    collected_by BIGINT,
    donation_date DATE NOT NULL,
    notes VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_donation_eligibility FOREIGN KEY (eligibility_check_id) REFERENCES eligibility_checks(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_donation_donor ON donations(donor_profile_id);
CREATE INDEX idx_donation_bb ON donations(blood_bank_id);
CREATE INDEX idx_donation_group ON donations(blood_group_id);
