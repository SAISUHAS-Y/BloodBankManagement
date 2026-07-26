-- V4__add_version_to_blood_requests.sql
ALTER TABLE blood_requests ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
