# 🔐 Identity & Security Service

## Overview
`identity-service` is the central security, authentication, and access control microservice of the Blood Bank Management platform. It handles user authentication, JWT token issuance, multi-factor authentication (2FA), password security policies, role governance with parent-child inheritance, permission matrix inspection, active device session management, and security audit log analytics.

---

## 🎯 Key Capabilities & API Sections

### 1. User Authentication & Login (`/api/v1/auth`)
* **Login & MFA:** User authentication (`/login`), 2-step TOTP/backup code verification (`/verify-mfa`).
* **Session Lifecycle:** Refresh token rotation (`/refresh`), single device logout (`/logout`), global multi-device logout (`/logout-all`).
* **Account Recovery & Password Policy:** Change password with 5-password history rule (`/change-password`), forgot password email token request (`/forgot-password`), reset password (`/reset-password`), email verification (`/verify-email`).

### 2. User Account Management (`/api/v1/admin/users`)
* **Account Creation & Lookup:** Create new user profiles (`POST /admin/users`), list all users (`GET /admin/users`), search with multi-field JPA filters (`POST /admin/users/search`).
* **State Control:** Activate (`/activate`), deactivate (`/deactivate`), lock (`/lock`), unlock (`/unlock`), suspend (`/suspend`), and soft-delete (`DELETE /admin/users/{id}`).
* **Role Assignment:** Reassign user roles (`POST /admin/users/assign-roles`).

### 3. Roles Management (`/api/v1/admin/roles`)
* **Role Governance:** Create custom system roles (`POST`), list all roles (`GET`), get role details (`GET /{id}`), enable (`/{id}/enable`), disable (`/{id}/disable`), soft-delete custom roles (`DELETE /{id}`).
* **Role Hierarchy & Inheritance:** View parent-child inheritance rules (`GET /hierarchy`), dynamically update role hierarchy (`PUT /hierarchy`).

### 4. Permissions Management (`/api/v1/admin/permissions`)
* **Matrix Grid:** Interactive Role-Permission Matrix grid (`GET /matrix`).
* **Permission Catalog:** View all permissions (`GET`), system modules (`GET /modules`), permission categories (`GET /categories`), permissions grouped by module (`GET /grouped-by-module`).

### 5. Two-Factor Authentication (2FA) (`/api/v1/mfa`)
* **TOTP 2FA Setup:** Generate secret key and QR code URI (`POST /setup`).
* **Enable/Disable:** Enable 2FA and receive 8 single-use emergency backup codes (`POST /enable`), disable 2FA (`POST /disable`).
* **Emergency Recovery:** Regenerate emergency backup codes (`POST /generate-backup-codes`).

### 6. Active Device Sessions (`/api/v1/sessions`)
* **Session Inspection:** List all active device sessions for current user (`GET`), view current device details (`GET /me`).
* **Session Revocation:** Remotely log out a target device (`DELETE /{sessionId}`), log out all other devices except current (`DELETE`).

### 7. Security Audit Logs (`/api/v1/admin/security`)
* **Audit Search:** Search authentication audit logs by user, IP address, event type, or date range (`POST /audit-logs/search`).
* **Security Analytics Summary:** View login success/failed metrics and top suspicious IPs (`GET /audit-logs/analytics`).

### 8. Security Keys (JWKS) (`/.well-known/jwks.json`)
* **JWKS Discovery:** Public RSA key discovery endpoint for stateless JWT signature verification across API Gateway and microservices.

---

## 🗄️ Database & Schema (`identity_db`)

* **Port:** `8081`
* **Database URL:** `jdbc:mysql://localhost:3306/identity_db`
* **Flyway Migrations:** `src/main/resources/db/migration/`
* **Tables:**
  - `users` — User account credentials, status (ACTIVE, DEACTIVATED, LOCKED, SUSPENDED), failed attempt count, lockout timestamp.
  - `roles` — System and custom enterprise role definitions.
  - `permissions` — System permission definitions grouped by module and category.
  - `user_roles` — Junction table mapping users to assigned roles.
  - `role_permissions` — Junction table mapping roles to permissions.
  - `role_inheritance` — Parent-to-child role inheritance rules.
  - `refresh_tokens` — Refresh token tracking, device info, IP address, expiration, and revocation status.
  - `user_mfa` — TOTP secret keys, MFA enabled flags, and single-use emergency backup codes.
  - `password_history` — Password history tracking (enforcing 5-password non-repeatability rule).
  - `auth_audit_logs` — Security audit logs recording login successes, failures, MFA attempts, and account lockouts.
  - `event_outbox` — Transactional outbox for RabbitMQ domain event publishing.

---

## 🚀 Running & Verification

```powershell
# Build service
mvn -f backend\services\identity-service\pom.xml clean install -DskipTests

# Run service
mvn -f backend\services\identity-service spring-boot:run
```

* **Swagger UI / OpenAPI Documentation:** [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
* **Actuator Health Check:** [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)
