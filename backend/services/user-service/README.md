# 👤 User Service

## Overview
`user-service` manages application user profiles, contact information, staff profiles, and blood donor registrations.

## Responsibilities
* User profile management (`/api/v1/users`).
* Donor profile registration & medical questionnaire records.
* Staff assignment & department directory.

## Database & Migrations
* **Database Name:** `user_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `user_profiles`, `donor_profiles`, `staff_profiles`, `event_outbox`.

## Port & Health Check
* **Port:** `8083`
* **Health Check:** [http://localhost:8083/actuator/health](http://localhost:8083/actuator/health)
