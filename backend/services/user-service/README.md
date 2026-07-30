# 👤 User Service (v2 Enterprise Architecture)

## Overview
`user-service` manages donor and staff profile lifecycles, donor eligibility calculations, case notes, staff organizational hierarchy and transfers, audit history tracking, event outbox publishing, bulk import/export, and analytical reporting for the Blood Bank platform.

## Key Capabilities (v2 Upgrade)

### Phase 1 — Foundation Hardening
* **Specification-based Dynamic Search**: Multi-field query filtering for donors (`DonorSpecifications`) and staff (`StaffSpecifications`) replacing legacy query parameters.
* **Structured DTOs**: `DonorSearchRequest` and `StaffSearchRequest` supporting dynamic sorting (`sortBy`, `sortDirection`) and zero-indexed pagination (`page`, `size`).
* **Standardized Field Validation**: Multi-tier validation groups (`ValidationGroups.Create`, `ValidationGroups.Update`) and error mapping.

### Phase 2 — Donor Lifecycle & Eligibility
* **Donor Eligibility Engine** (`GET /api/v1/donors/{id}/eligibility`): Calculates next eligible donation date based on `lastDonationDate`, gender intervals (Male: 90 days, Female/Other: 120 days), and deferral/blacklist status.
* **Audit History Trail** (`GET /api/v1/donors/{id}/history`): Tracks profile edits, status updates, deferrals, and actor metadata in `profile_audit_history`.
* **Staff Case Notes** (`POST/GET /api/v1/donors/{id}/notes`): Free-text case notes for donor interaction records separate from medical questionnaires.
* **Bulk Import & Export**: CSV file bulk upload with row-level validation reports (`POST /api/v1/donors/bulk-import`) and sensitive-data gated CSV export (`GET /api/v1/donors/export`).

### Phase 3 — Staff Lifecycle & Org Management
* **Staff Employment Status** (`PATCH /api/v1/staff/{id}/status`): State transitions (`ACTIVE`, `ON_LEAVE`, `SUSPENDED`, `TERMINATED`) with reason logging.
* **Facility Transfers** (`PATCH /api/v1/staff/{id}/transfer`): Reassignments between blood banks and hospitals with audit tracking.
* **Reporting Hierarchy** (`GET /api/v1/staff/{id}/reports`): Hierarchical tree traversal via nullable `reportingManagerId`.

### Phase 4 — Event-Driven Enhancements
* **RabbitMQ Outbox Pattern**: Staging and dispatching `DonorStatusChangedEvent` and `StaffStatusChangedEvent` using transactional `event_outbox`.
* **Identity Sync Consumer**: `UserLifecycleEventListener` for reacting to identity user lifecycle events.

### Phase 5 — Analytics & Reporting
* **Donor Analytics Summary** (`GET /api/v1/donors/analytics/summary`): Retention rates, average donation frequencies, active-to-deferred ratios, state and blood group breakdowns.
* **Staff Headcount Analytics** (`GET /api/v1/staff/analytics/headcount`): Staff distributions by status, designation, blood bank, and hospital.

---

## API Endpoints Reference

### Donor Endpoints
| Method | Endpoint | Permission | Description |
|---|---|---|---|
| POST | `/api/v1/donors` | `DONOR_CREATE` | Register a new donor profile |
| PUT | `/api/v1/donors/{id}` | `DONOR_MANAGE` | Update donor profile |
| GET | `/api/v1/donors/{id}` | `DONOR_VIEW` | Basic non-sensitive donor view |
| GET | `/api/v1/donors` | `DONOR_VIEW` | Paginated non-sensitive donor specification search |
| GET | `/api/v1/donors/{id}/sensitive` | `DONOR_VIEW_SENSITIVE` | Full sensitive donor details |
| GET | `/api/v1/donors/sensitive` | `DONOR_VIEW_SENSITIVE` | Paginated full sensitive donor specification search |
| GET | `/api/v1/donors/{id}/eligibility` | `DONOR_VIEW` | Calculate next eligible donation date & status |
| GET | `/api/v1/donors/{id}/history` | `DONOR_VIEW_SENSITIVE` | Audit trail of profile changes |
| POST | `/api/v1/donors/bulk-import` | `DONOR_MANAGE` | Upload CSV for bulk registration |
| GET | `/api/v1/donors/export` | `DONOR_VIEW_SENSITIVE` | Export donors matching criteria to CSV |
| POST | `/api/v1/donors/{id}/notes` | `DONOR_MANAGE` | Add staff case note |
| GET | `/api/v1/donors/{id}/notes` | `DONOR_VIEW` | Fetch case notes for donor |
| GET | `/api/v1/donors/analytics/summary` | `DONOR_MANAGE` | Donor metrics & distribution summary |
| PATCH | `/api/v1/donors/{id}/defer` | `DONOR_MANAGE` | Defer or blacklist donor |
| PATCH | `/api/v1/donors/{id}/record-donation` | `DONOR_MANAGE` | Idempotent S2S donation recording |

### Staff Endpoints
| Method | Endpoint | Permission | Description |
|---|---|---|---|
| POST | `/api/v1/staff` | `STAFF_MANAGE` | Register staff profile |
| PUT | `/api/v1/staff/{id}` | `STAFF_MANAGE` | Update staff profile |
| GET | `/api/v1/staff/{id}` | `STAFF_MANAGE` | Get staff details by ID |
| GET | `/api/v1/staff/user/{identityUserId}` | `STAFF_MANAGE` | Get staff profile by identity user ID |
| GET | `/api/v1/staff` | `STAFF_MANAGE` | Paginated staff specification search |
| PATCH | `/api/v1/staff/{id}/status` | `STAFF_MANAGE` | Update employment status with reason |
| PATCH | `/api/v1/staff/{id}/transfer` | `STAFF_MANAGE` | Transfer staff between facility locations |
| GET | `/api/v1/staff/{id}/reports` | `STAFF_MANAGE` | Fetch direct reports for manager |
| GET | `/api/v1/staff/{id}/history` | `STAFF_MANAGE` | Fetch staff profile audit history |
| GET | `/api/v1/staff/analytics/headcount` | `STAFF_MANAGE` | Staff headcount breakdown |

---

## Database Schema & Migrations

* **Database:** `user_db` (Port 3306/3307)
* **Flyway Migrations:** `src/main/resources/db/migration/`
  * `V1__init_schema.sql` — Initial `donor_profiles` and `staff_profiles` schemas.
  * `V2__add_idempotency.sql` — Idempotency record tables.
  * `V3__add_event_outbox.sql` — Transactional event outbox schema.
  * `V6__add_donation_idempotency.sql` — Idempotent donation records.
  * `V7__add_donor_lifecycle_tables.sql` — `profile_audit_history` and `donor_notes` tables.
  * `V8__add_staff_status_and_hierarchy.sql` — `staff_status` enum column & `reporting_manager_id` foreign key.

---

## Port & Configuration
* **Service Port:** `8083`
* **Health Check:** `http://localhost:8083/actuator/health`
