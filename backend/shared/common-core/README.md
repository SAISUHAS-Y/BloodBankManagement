# 🧰 Common Core Library

## Overview
`common-core` contains base classes, shared utilities, validation annotations, and core DTO envelopes used across all microservices in the Blood Bank Management platform.

## Contents
* **`BaseEntity`:** Extended by all JPA entities. Contains `id`, `created_at`, `updated_at`, `created_by`, `updated_by`, and `is_deleted` soft-delete flag.
* **`ApiResponse<T>`:** Unified REST HTTP JSON response envelope carrying status code, timestamp, message, and payload data.
* **`RequestContext`:** ThreadLocal context holder preserving incoming request metadata (trace ID, user ID, user IP, roles).
* **`@ValidPassword`:** Custom Jakarta Validation annotation enforcing password complexity rules (min 8 characters, at least 1 letter and 1 digit).

## Usage
Include as a Maven dependency in any service:
```xml
<dependency>
    <groupId>com.bloodbank</groupId>
    <artifactId>common-core</artifactId>
</dependency>
```
