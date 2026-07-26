# 💉 Donation Service

## Overview
`donation-service` tracks blood donation events, donor eligibility evaluations, donation appointments, and blood bag lab testing status.

## Responsibilities
* Log donation drives & appointment bookings (`/api/v1/donations`).
* Donor eligibility verification (minimum 56-day gap rule).
* Blood bag lab test results recording & status tracking.

## Database & Migrations
* **Database Name:** `donation_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `donations`, `donation_drives`, `lab_test_results`, `event_outbox`.

## Port & Health Check
* **Port:** `8086`
* **Health Check:** [http://localhost:8086/actuator/health](http://localhost:8086/actuator/health)
