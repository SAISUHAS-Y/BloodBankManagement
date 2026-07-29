# 🩸 Blood Bank Management System - Backend Platform

Welcome to the backend architecture for the **Blood Bank Management System**. Built on Java 25 and Spring Boot 4.1.0, this distributed system uses a microservices architecture with clean modular boundaries, event-driven messaging via RabbitMQ, Outbox pattern transactional guarantees, and centralized Security & Discovery.

---

## 🛠️ Technology Stack

* **Core Platform:** Java 25 (LTS), Spring Boot 4.1.0, Spring Cloud 2025.1.1 (Oakwood)
* **Data Tier:** MySQL 8.x/9.x, Redis 8.x, Flyway Migrations, Spring Data JPA + Hibernate
* **Messaging & Events:** RabbitMQ 4.x (Transactional Outbox Pattern)
* **Security & Token Discovery:** Spring Security 6 + JWT (HS256/RS256 Signature Verification, JWKS Key Discovery)
* **API Documentation:** SpringDoc OpenAPI 3.1, Swagger UI (Centralized Aggregation)
* **Observability:** Spring Boot Actuator, Micrometer, OpenTelemetry, Zipkin Distributed Tracing

---

## 🗺️ Microservices Topology & Port Directory

| Module / Service Name | Relative Path | Port | Description |
| :--- | :--- | :---: | :--- |
| **`config-server`** | [`infrastructure/config-server`](infrastructure/config-server) | `8888` | Centralized Spring Cloud Config Server serving shared YAML profiles. |
| **`discovery-server`** | [`infrastructure/discovery-server`](infrastructure/discovery-server) | `8761` | Eureka Service Registry for service discovery. |
| **`api-gateway`** | [`infrastructure/api-gateway`](infrastructure/api-gateway) | `8080` | Reactive API Gateway with CORS, JWT security filter, and Swagger UI aggregation. |
| **`identity-service`** | [`services/identity-service`](services/identity-service) | `8081` | User Authentication, MFA (2FA), RBAC Role Governance, Permission Matrix, Active Sessions, and Security Audit Logs. |
| **`master-service`** | [`services/master-service`](services/master-service) | `8082` | Reference Master Data (Blood Groups, Component Types, Geographical Regions). |
| **`user-service`** | [`services/user-service`](services/user-service) | `8083` | User Profiles, Donor Registration, and Medical Profile Details. |
| **`hospital-service`** | [`services/hospital-service`](services/hospital-service) | `8084` | Hospital Directory, Staff Assignments, and Request Initiation. |
| **`blood-bank-service`** | [`services/blood-bank-service`](services/blood-bank-service) | `8085` | Blood Bank Directory, Unit Storage, and Real-time Inventory Management. |
| **`donation-service`** | [`services/donation-service`](services/donation-service) | `8086` | Blood Donation Tracking, Eligibility Checks, and Collection Logs. |
| **`transaction-service`** | [`services/transaction-service`](services/transaction-service) | `8087` | Blood Request Processing, Cross-matching, Unit Allocation, and Fulfillment. |
| **`notification-service`** | [`services/notification-service`](services/notification-service) | `8088` | Asynchronous Email/SMS/In-App Notifications via RabbitMQ. |

---

## 🧰 Shared Libraries (`/backend/shared`)

* **`common-core`:** Standard Base Entity, envelope DTO (`ApiResponse<T>`), and validation annotations.
* **`common-security`:** JWT token verification filter, SecurityContext parser, and `@HasPermission` AOP aspect.
* **`common-contracts`:** Shared inter-service request/response contracts and Feign client interfaces.
* **`common-events`:** Outbox Pattern implementation (`EventOutbox`), RabbitMQ publisher, and event payloads.
* **`common-exception`:** Unified `GlobalExceptionHandler` and domain exception hierarchy.
* **`common-logging`:** Logstash JSON log formatter, MDC correlation ID propagation, and performance tracing.

---

## ⚡ Quick Start Guide

```powershell
# 1. Build backend reactor
mvn clean package -DskipTests

# 2. Run infrastructure containers
docker-compose up -d

# 3. Access Swagger UI API Documentation
# Gateway Unified Swagger UI: http://localhost:8080/swagger-ui.html
```
