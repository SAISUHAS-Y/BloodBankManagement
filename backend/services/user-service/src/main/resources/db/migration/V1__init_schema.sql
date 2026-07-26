-- Create Donor Profiles Table
CREATE TABLE donor_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identity_user_id BIGINT,
    full_name VARCHAR(150) NOT NULL,
    dob DATE NOT NULL,
    gender_code VARCHAR(50) NOT NULL,
    blood_group_id BIGINT NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    address_line VARCHAR(255),
    state_id BIGINT NOT NULL,
    district_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    id_type VARCHAR(50),
    id_number VARCHAR(50),
    donor_status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    last_donation_date DATE,
    total_donations INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_donor_identity_user ON donor_profiles(identity_user_id);
CREATE INDEX idx_donor_blood_group ON donor_profiles(blood_group_id);
CREATE INDEX idx_donor_location ON donor_profiles(state_id, district_id, city_id);
CREATE INDEX idx_donor_email ON donor_profiles(email);
CREATE INDEX idx_donor_phone ON donor_profiles(phone);

-- Create Staff Profiles Table
CREATE TABLE staff_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identity_user_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    blood_bank_id BIGINT,
    hospital_id BIGINT,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_staff_identity_user ON staff_profiles(identity_user_id);
CREATE INDEX idx_staff_blood_bank ON staff_profiles(blood_bank_id);
CREATE INDEX idx_staff_hospital ON staff_profiles(hospital_id);
