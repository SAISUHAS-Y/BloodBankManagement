# 🔔 Notification Service

## Overview
`notification-service` is an asynchronous event-driven consumer service that listens to RabbitMQ exchanges to send SMS, Email, and In-App notifications.

## Responsibilities
* Consume RabbitMQ domain events (`UserRegisteredEvent`, `BloodRequestedEvent`, etc.).
* Render notification templates & trigger SMS/Email notifications.
* Maintain notification delivery history (`/api/v1/notifications`).

## Database & Migrations
* **Database Name:** `notification_db` (Port 3307 / 3306)
* **Migrations Path:** `/src/main/resources/db/migration/`
* **Tables:** `notification_templates`, `notification_logs`, `event_outbox`.

## Port & Health Check
* **Port:** `8088`
* **Health Check:** [http://localhost:8088/actuator/health](http://localhost:8088/actuator/health)
