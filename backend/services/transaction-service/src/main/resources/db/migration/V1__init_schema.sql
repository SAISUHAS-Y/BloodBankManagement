-- Create Blood Requests Table
CREATE TABLE blood_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT NOT NULL,
    requested_by BIGINT NOT NULL,
    blood_group_id BIGINT NOT NULL,
    component_type_code VARCHAR(50) NOT NULL,
    units_requested DOUBLE NOT NULL,
    urgency VARCHAR(50) NOT NULL DEFAULT 'ROUTINE',
    request_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    patient_name VARCHAR(150) NOT NULL,
    patient_age INT NOT NULL,
    clinical_reason VARCHAR(255),
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_request_hospital ON blood_requests(hospital_id);
CREATE INDEX idx_request_group ON blood_requests(blood_group_id);
CREATE INDEX idx_request_status ON blood_requests(request_status);

-- Create Issuance Records Table
CREATE TABLE issuance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_request_id BIGINT NOT NULL,
    blood_bank_id BIGINT NOT NULL,
    units_issued DOUBLE NOT NULL,
    issued_by BIGINT NOT NULL,
    issued_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cross_match_reference VARCHAR(150),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_issuance_request FOREIGN KEY (blood_request_id) REFERENCES blood_requests(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_issuance_request ON issuance_records(blood_request_id);
CREATE INDEX idx_issuance_bb ON issuance_records(blood_bank_id);
