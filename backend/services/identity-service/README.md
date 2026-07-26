# 🔐 Identity Service

## Overview
`identity-service` manages user authentication, JWT token issuance, password security policies, role definitions, and permission assignments.

## Responsibilities
* User login (`/api/v1/auth/login`) & JWT token generation.
* Refresh token rotation (`/api/v1/auth/refresh`) & session revocation.
* User password updates (`/api/v1/auth/change-password`).
* Role & permission management (`/api/v1/admin/roles`, `/api/v1/admin/permissions`).

## Database & Migrations
* **Database Name:** `identity_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `users`, `roles`, `permissions`, `user_roles`, `role_permissions`, `refresh_tokens`, `auth_audit_logs`, `event_outbox`.

## Port & Health Check
* **Port:** `8081`
* **Health Check:** [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)
