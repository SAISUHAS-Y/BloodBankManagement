-- V6__add_donation_idempotency.sql
ALTER TABLE donors ADD COLUMN last_processed_donation_id BIGINT UNIQUE NULL;

CREATE TABLE IF NOT EXISTS processed_donations (
    donation_id BIGINT PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
