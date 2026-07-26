# 🔄 Transaction Service

## Overview
`transaction-service` handles emergency and routine blood request workflows, blood component unit allocations, dispatching, and audit logging.

## Responsibilities
* Receive & process hospital blood requests (`/api/v1/transactions/requests`).
* Match & allocate blood units from available inventory.
* Dispatch and fulfillment tracking.

## Database & Migrations
* **Database Name:** `transaction_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `blood_requests`, `blood_allocations`, `dispatches`, `event_outbox`.

## Port & Health Check
* **Port:** `8087`
* **Health Check:** [http://localhost:8087/actuator/health](http://localhost:8087/actuator/health)
