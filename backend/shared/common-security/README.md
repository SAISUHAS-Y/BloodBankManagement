# 🔒 Common Security Library

## Overview
`common-security` provides shared security filters, JWT token parsing utilities, and permission-based Authorization AOP aspects for Spring Boot microservices.

## Contents
* **`JwtTokenProvider`:** Generates and verifies HMAC HS256 JWT tokens.
* **`JwtAuthenticationFilter`:** Intercepts REST requests, parses Bearer tokens, populates Spring Security `SecurityContextHolder`.
* **`@HasPermission("PERM_CODE")`:** AOP annotation placed on REST controller methods to enforce fine-grained role/permission access control.
* **`CustomPermissionEvaluator`:** Evaluates user permissions cached via Caffeine Cache.

## Usage
Add to `pom.xml` and import in application configuration:
```xml
<dependency>
    <groupId>com.bloodbank</groupId>
    <artifactId>common-security</artifactId>
</dependency>
```
