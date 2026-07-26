# 🗂️ Master Service

## Overview
`master-service` manages system-wide reference lookup datasets (Blood Groups, Component Types, Geographical Regions/Districts, Hospital Types).

## Responsibilities
* Serve blood group master data (`/api/v1/master/blood-groups`).
* Serve component type lookup data (`/api/v1/master/component-types`).
* Serve geographical region lookup data (`/api/v1/master/regions`).
* Cache reference lookups using Redis Cache (`@EnableCaching`).

## Database & Migrations
* **Database Name:** `master_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `master_blood_groups`, `master_components`, `master_regions`, `event_outbox`.

## Port & Health Check
* **Port:** `8089`
* **Health Check:** [http://localhost:8089/actuator/health](http://localhost:8089/actuator/health)
