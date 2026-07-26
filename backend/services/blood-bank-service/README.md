# 🩸 Blood Bank Service

## Overview
`blood-bank-service` manages blood bank facility registries, storage units, real-time blood component inventory, and stock updates.

## Responsibilities
* Blood bank facility profile directory (`/api/v1/blood-banks`).
* Real-time blood unit inventory tracking (`/api/v1/inventory`).
* Inventory reservation & release controls.

## Database & Migrations
* **Database Name:** `bloodbank_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `blood_banks`, `blood_inventory`, `blood_units`, `event_outbox`.

## Port & Health Check
* **Port:** `8085`
* **Health Check:** [http://localhost:8085/actuator/health](http://localhost:8085/actuator/health)
