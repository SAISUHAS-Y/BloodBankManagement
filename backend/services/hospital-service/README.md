# 🏥 Hospital Service

## Overview
`hospital-service` manages hospital registrations, facility details, authorized hospital staff profiles, and hospital-initiated blood request orders.

## Responsibilities
* Hospital directory management (`/api/v1/hospitals`).
* Hospital staff user registrations.
* Hospital blood requirement requests (`/api/v1/hospitals/{id}/requests`).

## Database & Migrations
* **Database Name:** `hospital_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `hospitals`, `hospital_staff`, `hospital_requests`, `event_outbox`.

## Port & Health Check
* **Port:** `8084`
* **Health Check:** [http://localhost:8084/actuator/health](http://localhost:8084/actuator/health)
