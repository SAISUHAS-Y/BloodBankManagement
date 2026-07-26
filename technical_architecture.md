# Blood Bank Management System - Technical Architecture Documentation

This document explains the technical architecture, directory layouts, execution commands, and compilation troubleshooting of the Blood Bank Management System (BBMS).

---

## 1. Technical Directory Layout

The backend is built as a **Maven Multi-Module Project** to enforce strict separation of concerns, separating shared libraries from core infrastructure services.

```text
D:\BloodBankManagement\backend\
├── pom.xml                               # Parent POM (Centralized dependency versions)
├── docker-compose.yml                    # Local multi-container Docker Composer
├── init-db.sql                           # Shared database MySQL initialization script
├── shared\                               # Common Shared Libraries (Reusable modules)
│   ├── common-core\                      # Constants, DTO envelopes, and base entities
│   ├── common-exception\                 # GlobalExceptionHandler and Custom Exceptions
│   ├── common-contracts\                 # Inter-service event and messaging payloads
│   ├── common-security\                  # JWT filter and token extraction logic
│   ├── common-events\                    # RabbitMQ event wrappers
│   └── common-logging\                   # Logback and MDC correlation trace logging
└── infrastructure\                       # System Microservice Infrastructure
    ├── config-server\                    # Centralized profile-driven Config Server
    ├── discovery-server\                 # Service Registry (Eureka Server)
    └── api-gateway\                      # API Gateway (Routing, CORS, Rate Limiting)
```

---

## 2. Technology Stack

- **Java Version**: Java 25 (LTS)
- **Spring Boot Version**: 4.1.0
- **Spring Cloud Version**: 2025.1.1 (Oakwood release train)
- **Containerization**: Docker Compose
- **Services**: MySQL 9.0, Redis 8.0, RabbitMQ 4.0

---

## 3. Running the Infrastructure

All commands must be executed from the root directory: **`D:\BloodBankManagement`**.

### Step 1: Stop Local Host MySQL (If port 3306 is in use)
If you have a native MySQL instance running natively on Windows, stop it so it doesn't conflict with Docker:
```powershell
# Run in an Administrator PowerShell terminal
net stop mysql
```
*Note: In `docker-compose.yml`, the database container outer port is mapped to `3307` (`3307:3306`) as a fallback to bypass local collisions.*

### Step 2: Build the Modules
Compile all Java modules and infrastructure binaries into executable `.jar` files:
```powershell
mvn -f backend/pom.xml clean package -DskipTests
```

### Step 3: Spin Up Docker Containers
Boot all services (databases, message queues, and Java components) in detached background mode:
```powershell
docker-compose -f backend/docker-compose.yml up -d --force-recreate
```

### Step 4: Verify Service Health
Check the container boot status:
```powershell
docker-compose -f backend/docker-compose.yml ps
```
All six containers (`mysql-db`, `redis-cache`, `rabbitmq-broker`, `config-server`, `discovery-server`, and `api-gateway`) should be marked **`Up (healthy)`**.

---

## 4. Key Gotchas & Solutions

During initial implementation, several critical compilation and runtime issues were solved:

### A. Java 25 & Lombok Compiler Incompatibility
- **Issue**: Standard Lombok `1.18.36` fails on JDK 25 compiler internals, throwing:
  `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`
- **Solution**: Upgraded Lombok globally in the Parent POM to **`1.18.46`** (with full Java 25 native compilation support).

### B. Spring Cloud Gateway WebFlux vs. Servlet Security
- **Issue**: The `api-gateway` is a reactive WebFlux service. Importing a shared security module (`common-security`) that transitively pulls in `spring-boot-starter-security` and Tomcat/Servlet filters causes a startup crash (reactive container vs. servlet container conflict).
- **Solution**: Excluded `spring-boot-starter-security` and `spring-boot-starter-web` from the gateway's import of `common-security`, and added **`spring-security-core`** directly to the gateway classpath. This allows compilation of security classes (like `GrantedAuthority`) without pulling in conflicting servlet web configurations.

### C. Spring Boot 2.4+ Ignored `bootstrap.yml`
- **Issue**: Starting with Spring Boot 2.4, bootstrap properties are ignored by default. `config-server` failed to bind to its custom port `8888` and defaulted to `8080`, breaking Docker health checks.
- **Solution**: Added the **`spring-cloud-starter-bootstrap`** dependency to `config-server`, `discovery-server`, and `api-gateway` to enable native parsing of bootstrap configuration files.

### D. Spring Cloud Version Compatibility Validation Bypass
- **Issue**: Spring Cloud `2025.1.1` checks for compatible Spring Boot versions at boot time and throws a fatal error because Spring Boot `4.1.0` is newer than the supported range.
- **Solution**: Passed the bypass property **`SPRING_CLOUD_COMPATIBILITY_VERIFIER_ENABLED=false`** as an **OS environment variable** in `docker-compose.yml`. This bypasses validation at the JVM boot stage (prior to Spring configuration loading).

### E. Ternary Operator Overload Resolution
- **Issue**: Calling `Jwts.parser().verifyWith(useAsymmetric ? publicKey : symmetricKey)` failed compilation because the compiler resolves the ternary expression as `java.security.Key`, whereas `verifyWith()` has separate overloads for `PublicKey` and `SecretKey`.
- **Solution**: Refactored to use an explicit `if-else` block to construct the verification parser.

---

## 5. Standardized API Response & Exception Pattern

To maintain a consistent interface for the React frontend, all REST microservices must return response payloads conforming to the `ApiResponse<T>` envelope from the `common-core` module.

### A. Envelope JSON Schema

#### 1. Success Response (HTTP 200/201 OK)
```json
{
  "success": true,
  "status": 200,
  "message": "Operation completed successfully",
  "data": {
    "id": 1,
    "name": "Jane Doe",
    "bloodGroup": "O-Neg"
  },
  "timestamp": "2026-07-23T00:58:12Z",
  "traceId": "c8f85f1c-7f5b-4cba-bd30-058b8f2c3d5e"
}
```

#### 2. Failure Response (HTTP 4xx / 5xx)
```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "data": {
    "age": "Age must be at least 18 years to donate"
  },
  "timestamp": "2026-07-23T00:58:15Z",
  "traceId": "c8f85f1c-7f5b-4cba-bd30-058b8f2c3d5e"
}
```

---

### B. Controller Implementation Standard (Success)

When building REST controllers, **never** return raw entities or DTOs directly. Always wrap them in `ApiResponse<T>`:

```java
@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;

    @PostMapping
    public ResponseEntity<ApiResponse<DonorDto>> registerDonor(@Valid @RequestBody DonorRegisterRequest request) {
        DonorDto registeredDonor = donorService.registerDonor(request);
        
        return new ResponseEntity<>(
            ApiResponse.success("Donor registered successfully", registeredDonor),
            HttpStatus.CREATED
        );
    }
}
```

---

### C. Exception Handling Standard (Failure)

Developers should **never** wrap controller return statements in manual try-catch blocks to return a failure status. 

Instead, throw custom domain business exceptions (extending `BaseException`) from the application/service layer. The `common-exception` module's `GlobalExceptionHandler` will automatically intercept it, extract the MDC correlation tracing context, and format the failure payload:

```java
// Throwing in Service Layer
if (donor.getAge() < 18) {
    throw new InvalidInputException("Age must be at least 18 years to donate");
}

// Automatically Intercepted & Formatted:
// GlobalExceptionHandler will catch and return:
// ApiResponse.failure(400, "Age must be at least 18 years to donate", "ERR_INVALID_INPUT", traceId);
```

#### 2. Auto-Intercepted Framework Validation Exceptions
In addition to custom domain exceptions, the handler automatically catches and cleans up built-in Spring/Jakarta validation errors:
- **`MethodArgumentNotValidException`**: Triggered when `@Valid` check fails on a `@RequestBody` object. Returns a map of field-specific error messages.
- **`ConstraintViolationException`**: Triggered when query parameters or path variables fail validation rules (e.g. `@Min(1) Long id`). Returns a clean parameter-specific error map.
- **`HttpMessageNotReadableException`**: Triggered when the request body is missing or the JSON is malformed. Returns `"Malformed JSON request body or missing request body"`.
- **`MethodArgumentTypeMismatchException`**: Triggered when a parameter cannot be parsed into its declared type (e.g. passing a string `"abc"` to a `Long` variable). Returns a user-friendly type coercion error message.


---

## 6. REST API Design Standards (GET, POST, PUT, PATCH, DELETE, QUERY)

To build a modern RESTful API ecosystem, services must utilize HTTP methods in accordance with their semantic definitions:

### A. GET (Retrieve Resource)
- **Semantics**: Safe & Idempotent. Reads data; must not modify server state.
- **Mapping**: `@GetMapping("/{id}")`
- **Example**:
  ```java
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<DonorDto>> getDonorById(@PathVariable Long id) {
      return ResponseEntity.ok(ApiResponse.success(donorService.getById(id)));
  }
  ```

### B. POST (Create Resource)
- **Semantics**: Non-safe & Non-idempotent. Creates a new resource.
- **Mapping**: `@PostMapping`
- **Example**:
  ```java
  @PostMapping
  public ResponseEntity<ApiResponse<DonorDto>> createDonor(@Valid @RequestBody DonorDto dto) {
      return new ResponseEntity<>(ApiResponse.success("Donor registered successfully", donorService.create(dto)), HttpStatus.CREATED);
  }
  ```

### C. PUT (Full Replace Resource)
- **Semantics**: Idempotent. Replaces the target resource representation entirely with the request payload.
- **Mapping**: `@PutMapping("/{id}")`
- **Example**:
  ```java
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<DonorDto>> replaceDonor(@PathVariable Long id, @Valid @RequestBody DonorDto dto) {
      return ResponseEntity.ok(ApiResponse.success("Donor fully updated", donorService.replace(id, dto)));
  }
  ```

### D. PATCH (Partial Update Resource)
- **Semantics**: Non-safe, but designed for partial updates (modifying only specific fields) or executing specific state transitions.
- **Mapping**: `@PatchMapping("/{id}")`
- **Example**:
  ```java
  @PatchMapping("/{id}/status")
  public ResponseEntity<ApiResponse<DonorDto>> updateDonorStatus(@PathVariable Long id, @RequestBody Map<String, String> updates) {
      String status = updates.get("status");
      return ResponseEntity.ok(ApiResponse.success("Donor status updated", donorService.updateStatus(id, status)));
  }
  ```

### E. DELETE (Remove Resource)
- **Semantics**: Idempotent. Decommission/delete a resource.
- **Mapping**: `@DeleteMapping("/{id}")`
- **Example**:
  ```java
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteDonor(@PathVariable Long id) {
      donorService.delete(id);
      return ResponseEntity.ok(ApiResponse.success("Donor deleted successfully", null));
  }
  ```

### F. QUERY (Safe Reading with Request Body)
- **Semantics**: Safe & Idempotent (RFC 10008). Used for complex query/search operations containing large or sensitive search filter conditions that cannot fit cleanly or securely inside URL query parameters (e.g. searching by coordinates, date ranges, and status arrays).
- **Implementation Standard**: Since Spring MVC/WebFlux does not yet have a native `@QueryMapping` for HTTP queries, the standard pattern leaves the `method` parameter open and inspects the HTTP method name. This allows both the raw **`QUERY`** verb and a standard fallback **`POST`** (for clients/proxies that do not support the new QUERY verb yet):
- **Mapping & Example**:
  ```java
  @RequestMapping(value = "/search", method = {RequestMethod.POST}) // Fallback mapping in Spring
  public ResponseEntity<ApiResponse<List<DonorDto>>> searchDonors(
          org.springframework.http.server.reactive.ServerHttpRequest request,
          @RequestBody DonorSearchCriteria criteria) {
      
      // Support both newly standardized HTTP QUERY verb (RFC 10008) and POST fallback
      String httpMethod = request.getMethod().name();
      if (!"QUERY".equalsIgnoreCase(httpMethod) && !"POST".equalsIgnoreCase(httpMethod)) {
          return new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED);
      }

      List<DonorDto> searchResults = donorService.search(criteria);
      return ResponseEntity.ok(ApiResponse.success("Search completed", searchResults));
  }
  ```


