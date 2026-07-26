# 🩸 Blood Bank Management System (Enterprise Microservices Platform)

An enterprise-grade, distributed, event-driven microservices architecture built with **Java 25 (LTS)**, **Spring Boot 4.1.0**, **Spring Cloud 2025.1.1 (Oakwood)**, **MySQL**, **Redis**, **RabbitMQ**, **Zipkin**, and **Spring Cloud Netflix Eureka**.

---

## 🏗️ Architecture Overview

The system is organized into decoupled microservices, central infrastructure components, and shared enterprise libraries following the **Database-per-Service** and **Outbox Pattern** standards.

```
                                  +-------------------+
                                  |   API Gateway     |
                                  |   (Port 8080)     |
                                  +---------+---------+
                                            |
        +------------------+----------------+------------------+------------------+
        |                  |                |                  |                  |
+-------v-------+  +-------v-------+  +-----v--------+  +------v-------+  +-------v-------+
| Identity      |  | User Service  |  | Hospital     |  | Blood Bank   |  | Donation      |
| Service (8081)|  | (Port 8083)   |  | Service(8084)|  | Service(8085)|  | Service(8086) |
+---------------+  +---------------+  +--------------+  +--------------+  +---------------+
```

---

## 📦 System Modules & Topology

### 1. Infrastructure Services (`/backend/infrastructure`)
* ⚙️ **`config-server` (Port 8888):** Centralized Spring Cloud Config Server reading `shared-configs/`.
* 🔍 **`discovery-server` (Port 8761):** Eureka Service Discovery Server with Basic Auth (`admin`/`admin123`).
* 🌐 **`api-gateway` (Port 8080):** Spring Cloud Gateway with reactive JWT authentication routing & Swagger UI aggregation.

### 2. Business Microservices (`/backend/services`)
* 🔐 **`identity-service` (Port 8081):** User authentication, JWT issuance, RBAC permissions & password policy.
* 👤 **`user-service` (Port 8083):** User profile management & donor profile registration.
* 🏥 **`hospital-service` (Port 8084):** Hospital registry, staff management & blood request initiation.
* 🩸 **`blood-bank-service` (Port 8085):** Blood bank facility directory & real-time inventory management.
* 💉 **`donation-service` (Port 8086):** Blood donation tracking, donor eligibility check & collection logs.
* 🔄 **`transaction-service` (Port 8087):** Blood request processing, allocation & dispatch management.
* 🔔 **`notification-service` (Port 8088):** Multi-channel notifications (SMS/Email/In-App) via RabbitMQ events.
* 🗂️ **`master-service` (Port 8089):** Reference lookups (Blood Groups, Component Types, Geographical Regions).

### 3. Shared Enterprise Modules (`/backend/shared`)
* 🧰 **`common-core`:** Standard Base Entity, envelope DTO (`ApiResponse<T>`), validation annotations (`@ValidPassword`).
* 🔒 **`common-security`:** JWT token verification filter, SecurityContext parsing & `@HasPermission` AOP aspect.
* 📄 **`common-contracts`:** Inter-service Feign client contracts and shared request/response DTOs.
* 📨 **`common-events`:** Outbox Pattern implementation (`EventOutbox`), RabbitMQ publisher & event DTOs.
* 🚨 **`common-exception`:** Unified `GlobalExceptionHandler` and base domain exceptions (`ResourceNotFoundException`, etc.).
* 🪵 **`common-logging`:** Logstash JSON logging format, MDC request tracing & performance logging aspect.

---

## ⚡ Quick Start Guide

### Step 1: Start Docker Infrastructure
```powershell
docker-compose -f backend\docker-compose.yml up -d mysql-db redis-cache rabbitmq-broker zipkin-tracing
```

### Step 2: Build All Modules
```powershell
mvn -f backend\pom.xml clean install -DskipTests
```

### Step 3: Launch Core Infrastructure Services
```powershell
# 1. Config Server
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\infrastructure\config-server spring-boot:run"

# 2. Discovery Server (Wait 5s)
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\infrastructure\discovery-server spring-boot:run"

# 3. API Gateway (Wait 5s)
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\infrastructure\api-gateway spring-boot:run"
```

### Step 4: Launch Microservices
```powershell
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\identity-service spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\user-service spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\hospital-service spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\blood-bank-service spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\donation-service spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\transaction-service spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\notification-service spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f backend\services\master-service spring-boot:run"
```

---

## 📊 Endpoints & Dashboards

* **API Gateway & Central Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Eureka Service Registry:** [http://localhost:8761](http://localhost:8761) (`admin` / `admin123`)
* **RabbitMQ Management Dashboard:** [http://localhost:15672](http://localhost:15672) (`guest` / `guest`)
* **Zipkin Distributed Tracing UI:** [http://localhost:9411](http://localhost:9411)
