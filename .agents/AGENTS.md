# Project-Scoped Rules - Blood Bank Management System

This file defines guidelines, architecture choices, and custom rules that future development agents must follow in this workspace.

---

## 1. Dependency & Version Standards

- **Java Version**: Always compile and target **Java 25 (LTS)**.
- **Spring Boot & Spring Cloud**: Use **Spring Boot 4.1.0** and **Spring Cloud 2025.1.1 (Oakwood)**.
- **Lombok Version**: Always use Lombok version **`1.18.46`** (or newer) to prevent javac compiler tagging initialization crashes on JDK 25.
- **Bootstrap Properties**: Include the **`spring-cloud-starter-bootstrap`** dependency on the classpath of any microservice to enable the parsing of `bootstrap.yml` configurations (standard behavior in Spring Boot 2.4+).
- **Spring Cloud Version Compatibility**: To prevent Oakwood from crashing during startup verification against Spring Boot 4.1.x, the Spring Cloud verifier check must be deactivated by declaring the OS environment variable `SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false` in the container configuration.

---

## 2. API Design & HTTP Methods Standards

- **Unified Envelope**: All REST controller responses must be wrapped in `com.bloodbank.common.core.dto.ApiResponse<T>`.
- **Status Code Mapping**: Success responses must carry an explicit HTTP status code in the JSON envelope body (default `200`).
- **Framework Validation Handler**:
  - Validation violations on request bodies must return a map of field-specific error messages (intercepted via `MethodArgumentNotValidException`).
  - Validation violations on query parameters/path variables must return a map of parameter-specific error messages (intercepted via `ConstraintViolationException`).
  - Malformed JSON requests must be returned with a clean `400 Bad Request` explaining syntax errors (intercepted via `HttpMessageNotReadableException`).
- **Domain Exceptions**: When input validation or business rules fail in the service layer, throw custom subclasses of `BaseException` (e.g. `InvalidInputException`, `ResourceNotFoundException`). Do not wrap controllers in manual try-catch blocks; let the `GlobalExceptionHandler` format the failure response envelope automatically.
- **RESTful URI Naming Conventions**: URIs must be pluralized nouns, lowercase, and hyphen-separated (e.g., `/api/v1/blood-requests`, NOT `/api/v1/bloodRequests` or `/api/v1/getBloodRequest`). Avoid verbs in URIs.
- **HTTP Verbs Mapping Standards**:
  - **GET**: Read-only, safe, and idempotent. Used for retrieving resource state.
  - **POST**: Non-safe and non-idempotent. Used for resource creation.
  - **PUT**: Idempotent full replacement of the target resource.
  - **PATCH**: Non-safe partial update or explicit resource state transitions.
  - **DELETE**: Idempotent resource removal (supports both soft and hard deletions).
  - **QUERY (RFC 10008)**: Idempotent and safe complex reading utilizing a request body.
    - Implement this by leaving the controller `@RequestMapping` method attribute open to map requests, and checking the method name inside the handler method.
    - Allow both the raw **`QUERY`** verb and a standard **`POST`** fallback to support HTTP query operations on legacy clients.

---

## 3. API Gateway Classpath & Runtime Protection

- **Exclude Tomcat Servlet Transitivities**: When importing `common-security` (or any shared modules containing servlet configurations) into reactive WebFlux components like the `api-gateway`, always exclude `spring-boot-starter-web` and `spring-boot-starter-security` to prevent startup crashes.
- **Compile-Time Classpath Support**: To compile security classes (like `GrantedAuthority` or security filter overrides) in the gateway without pulling in servlet containers, import the lightweight **`spring-security-core`** library directly.

---

## 4. Database Auditing & Entity Standards

- **Audit Fields Requirement**: All database tables and JPA entities in the system must contain standard auditing fields to track record creation, modification, and soft deletion.
- **Entity Extension**: All JPA entity classes must extend the shared base entity **`com.bloodbank.common.core.entity.BaseEntity`** from the `common-core` module. This inherits:
  - `id` (Auto-incrementing primary key)
  - `created_at` (Timestamp of creation)
  - `updated_at` (Timestamp of last modification)
  - `created_by` (Username/system identity of creator)
  - `updated_by` (Username/system identity of last modifier)
  - `is_deleted` (Boolean flag for soft delete operations)
- **Activating Auditing**: Business microservices must activate JPA auditing by placing the **`@EnableJpaAuditing`** annotation on their main Spring Boot Application class or a dedicated configuration class.
- **Deletions Support (Soft vs. Hard)**: The system must support both soft and hard deletions depending on the business use case:
  - **Soft Delete (Standard)**: Retains records in the database for auditing and historical tracking (e.g. Donors, Donations, Inventory logs). Implemented by setting the `is_deleted` flag to `true` (available via the `BaseEntity.delete()` method) and filtering out deleted records in read queries (e.g., using Hibernate's `@SQLRestriction("is_deleted = false")` on the entity).
  - **Hard Delete**: Physically removes records from the database table (e.g. for transient data, error corrections, or transactional cleanups). Implemented using standard Spring Data JPA `delete()` repositories.
- **Indexing Guidelines**: For optimal query performance, maintain database indexing on columns that are frequently searched, filtered, joined, or sorted:
  - **Foreign Keys**: Always index foreign key columns (e.g. `donor_id`, `hospital_id`, `request_id`) to accelerate join operations.
  - **Unique Lookups**: Index unique identifier fields used in query filters (e.g. `email`, `username`, `phone_number`).
  - **Filter Columns**: Index columns frequently used in WHERE conditions (e.g. `blood_group`, `status`, `is_deleted`).
  - **Sorting/Pagination Columns**: Index timestamp fields (e.g. `created_at`) to optimize sorting for paginated queries.
  - **JPA & DDL Registration**: Declare indexes in JPA using `@Table(name = "...", indexes = @Index(name = "idx_...", columnList = "..."))` and write corresponding `CREATE INDEX` statements in database initialization SQL scripts.
- **Schema DDL Consistency**: SQL scripts (such as table definition creation scripts) must define corresponding audit columns for every table:
  ```sql
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  created_by VARCHAR(255),
  updated_by VARCHAR(255),
  is_deleted BOOLEAN NOT NULL DEFAULT FALSE
  ```

### Enterprise JPA & Database Best Practices (Do's & Don'ts)

Future development agents must adhere to these production-level standards for database interactions to prevent performance regressions, memory leaks, and architectural violations:

#### A. What You MUST Do (Best Practices)
1.  **Expose DTOs, Never Entities**: Controllers must never return JPA entities directly. Always map entities to lightweight DTOs in the service layer before returning them. This decouples the database schema from the public API contract and avoids lazy-loading serialization errors.
2.  **Explicit LAZY Loading**: Explicitly set `fetch = FetchType.LAZY` on `@OneToMany`, `@ManyToMany`, and `@OneToOne` mapping annotations. Loading entities eagerly results in massive Cartesian-product joins and severe N+1 query inflation.
3.  **Use Explicit Column/Table Mappings**: Always define `@Table(name = "...")` and `@Column(name = "...")` explicitly. Never rely on Hibernate's default snake-case/camel-case implicit naming strategies, which can change between framework versions.
4.  **Optimize Read Transactions**: Mark read-only service transactions with `@Transactional(readOnly = true)`. This tells Hibernate to disable dirty-checking flush cycles, yielding significant memory and CPU optimization.
5.  **Schema Evolution Safety**: For production environments, always disable auto-DDL generation (`spring.jpa.hibernate.ddl-auto=none` or `validate`). Database schema changes must be driven via controlled SQL DDL scripts or database migration tools (e.g. Liquibase/Flyway).

#### B. What You MUST NOT Do (Anti-Patterns)
1.  **Do NOT Use Lombok `@Data` on JPA Entities**: `@Data` automatically generates `toString()`, `equals()`, and `hashCode()` implementations that recursively traverse all fields and lazy collection properties. In JPA, this triggers immediate `LazyInitializationException` errors or memory exhaustion from circular references.
    *   *Correction*: Use `@Getter` and `@Setter` at the entity class level. Manually override `equals` and `hashCode` comparison strictly using the primary key field (`id`).
2.  **Do NOT Run Database Queries in Loops**: Never call repository operations (`findById`, etc.) inside loops. This triggers multiple round-trips to the database (N+1 execution).
    *   *Correction*: Gather all parameters and use batch-fetch operations (e.g., `findAllById(ids)` or custom JPQL `IN` queries).
3.  **Do NOT perform cross-service SQL joins**: In a microservice architecture, each service must maintain database isolation (shared-nothing pattern). Never write SQL joins across tables owned by different microservices.
    *   *Correction*: Fetch required foreign context by calling the appropriate microservice API (using Feign Clients) or synchronize required lookup data asynchronously using message broker events (RabbitMQ).
4.  **Do NOT Expose Sequential IDs in Public Endpoints**: Never expose auto-incremented primary keys (`id: 12345`) in public APIs or URLs, as this allows attackers to harvest data using simple integer enumeration.
    *   *Correction*: Use secure randomized identifiers (like UUIDs or hash-keys) for client-facing resource identification.

---

## 5. Code Reusability & Shared Module Standards

- **Use Shared Modules for Common Configurations**: If a configuration class (such as custom Jackson serializers, RabbitMQ templates, or thread pool allocators) is identical across multiple services, define it once inside the appropriate shared library (e.g. `common-core`, `common-security`, `common-events`) rather than copy-pasting it into service application folders.
- **Feign Contracts**: Expose all inter-service client Feign definitions and their matching request/response DTOs inside `common-contracts`.
- **Targeted Application Packages**: Service-specific folders must only contain code that contains custom business logic or configurations unique to that microservice. For common boilerplate configs, import the shared configurations explicitly (e.g. using Spring's `@Import(SharedConfig.class)` annotation).

---

## 6. Documentation Maintenance & README Standards

- **Mandatory README Maintenance**: Future development agents MUST maintain and update the `README.md` file in any root (`/`), infrastructure (`backend/infrastructure/*`), service (`backend/services/*`), or shared module (`backend/shared/*`) folder whenever making modifications to:
  - Service ports, routes, or environment configurations.
  - REST endpoints, Feign client contracts, or DTO schemas.
  - Database schema tables, Flyway migrations, or indexes.
  - Shared security filters, event DTOs, domain exceptions, or logging utilities.

