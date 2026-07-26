-- Create Hospitals Table
CREATE TABLE hospitals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    registration_number VARCHAR(100) NOT NULL UNIQUE,
    hospital_type_code VARCHAR(50) NOT NULL,
    state_id BIGINT NOT NULL,
    district_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    address_line VARCHAR(255),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_hospital_reg_number ON hospitals(registration_number);
CREATE INDEX idx_hospital_location ON hospitals(state_id, district_id, city_id);
CREATE INDEX idx_hospital_email ON hospitals(email);

-- Create Hospital Contacts Table
CREATE TABLE hospital_contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_contact_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_contact_hospital_id ON hospital_contacts(hospital_id);
