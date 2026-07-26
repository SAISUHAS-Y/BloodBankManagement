-- Create Blood Banks Table
CREATE TABLE blood_banks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    license_number VARCHAR(100) NOT NULL UNIQUE,
    blood_bank_type_code VARCHAR(50) NOT NULL,
    state_id BIGINT NOT NULL,
    district_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    address_line VARCHAR(255),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    operating_hours_note VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bb_license_number ON blood_banks(license_number);
CREATE INDEX idx_bb_location ON blood_banks(state_id, district_id, city_id);
CREATE INDEX idx_bb_email ON blood_banks(email);

-- Create Blood Stocks Table
CREATE TABLE blood_stocks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_bank_id BIGINT NOT NULL,
    blood_group_id BIGINT NOT NULL,
    component_type_code VARCHAR(50) NOT NULL,
    units_available DOUBLE NOT NULL DEFAULT 0.0,
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_stock_blood_bank FOREIGN KEY (blood_bank_id) REFERENCES blood_banks(id) ON DELETE CASCADE,
    CONSTRAINT uk_bb_group_comp UNIQUE (blood_bank_id, blood_group_id, component_type_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_stock_bb ON blood_stocks(blood_bank_id);
CREATE INDEX idx_stock_group ON blood_stocks(blood_group_id);
CREATE INDEX idx_stock_component ON blood_stocks(component_type_code);

-- Create Stock Adjustment Logs Table
CREATE TABLE stock_adjustment_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    blood_stock_id BIGINT NOT NULL,
    previous_units DOUBLE NOT NULL,
    new_units DOUBLE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    adjusted_by BIGINT NOT NULL,
    adjusted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_log_blood_stock FOREIGN KEY (blood_stock_id) REFERENCES blood_stocks(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_adj_stock_id ON stock_adjustment_logs(blood_stock_id);
CREATE INDEX idx_adj_user_id ON stock_adjustment_logs(adjusted_by);
