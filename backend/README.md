# Blood Bank Management System - Backend

Welcome to the backend architecture for the **Blood Bank Management System**. This system is built using a Java Spring Boot microservices architecture, implementing Clean Architecture patterns and production-grade infrastructure tooling.

---

## 🛠️ Tech Stack & Versions
*   **Java**: 25 (LTS)
*   **Spring Boot**: 4.1.0
*   **Spring Cloud**: 2025.1.1 "Oakwood"
*   **Data Tier**: MySQL 9.x, Redis 8.x, Flyway Migrations, Spring Data JPA + Hibernate
*   **Messaging**: RabbitMQ 4.x
*   **Security**: Spring Security 6 + JJWT (Asymmetric RS256 token verification)
*   **Boilerplate & Mapping**: Lombok 1.18.36, MapStruct 1.6.3
*   **Observability**: Spring Boot Actuator, Micrometer, OpenTelemetry

---

## 📂 Project Architecture

The project is organized as a multi-module Maven structure under the `backend/` root directory. Each microservice follows a Clean Architecture design with three sub-modules:
1.  **`-api`**: Spring boot bootstrap, REST controllers, and Security filters.
2.  **`-application`**: Business services, DTOs, and mapping layers (infrastructure-independent).
3.  **`-infrastructure`**: JPA entities, repositories, service implementations, message publishers, and migrations.

---

## 🗺️ System Modules Directory & Port Mapping

| Module Name | Path | Port | Status | Description |
| :--- | :--- | :---: | :---: | :--- |
| **`common-core`** | [shared/common-core/](shared/common-core) | N/A | `COMPLETED` | Shared constants, ApiResponse wrappers, Page response helpers, and BaseEntity. |
| **`common-exception`** | [shared/common-exception/](shared/common-exception) | N/A | `COMPLETED` | Centralized exception hierarchy and RestControllerAdvice. |
| **`common-contracts`** | [shared/common-contracts/](shared/common-contracts) | N/A | `COMPLETED` | Shared DTOs for Feign cross-service communications (JPA-free). |
| **`common-security`** | [shared/common-security/](shared/common-security) | N/A | `COMPLETED` | JWT utilities (Symmetric/Asymmetric), permission annotation, and security filters. |
| **`common-events`** | [shared/common-events/](shared/common-events) | N/A | `COMPLETED` | RabbitMQ event envelopes, marker interfaces, and concrete events. |
| **`common-logging`** | [shared/common-logging/](shared/common-logging) | N/A | `COMPLETED` | MDC correlation filter and ELK-compatible JSON log configuration profiles. |
| **`config-server`** | [infrastructure/config-server/](infrastructure/config-server) | `8888` | `COMPLETED` | Centralized config server serving native profile configurations. |
| **`discovery-server`**| [infrastructure/discovery-server/](infrastructure/discovery-server) | `8761` | `COMPLETED` | Eureka service registry with optimized dev configurations. |
| **`api-gateway`** | [infrastructure/api-gateway/](infrastructure/api-gateway) | `8080` | `COMPLETED` | Routing gateway with dynamic JWT parsing, rate limiting, and CORS. |

---

## 🚀 Running the System Locally

### Step 1: Build all Maven Modules
Compile and package the shared libraries and infrastructure jars:
```bash
mvn clean package -DskipTests
```

### Step 2: Boot Containers via Docker Compose
Start MySQL, Redis, RabbitMQ, and the infrastructure microservices in their correct boot order:
```bash
docker-compose up -d
```

To view logs for specific components:
```bash
docker-compose logs -f config-server
docker-compose logs -f discovery-server
docker-compose logs -f api-gateway
```

### Step 3: Verify Statuses
* **Config Server**: Open `http://localhost:8888/api-gateway/default` to inspect loaded configs.
* **Discovery Server**: Open Eureka Dashboard at `http://localhost:8761` to verify active registrations.
* **API Gateway**: Verify routing by calling Gateway endpoints (e.g., `http://localhost:8080/actuator/health`).

