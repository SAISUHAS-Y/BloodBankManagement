# 🚨 Common Exception Library

## Overview
`common-exception` houses custom enterprise domain exceptions and the global `@RestControllerAdvice` exception handler (`GlobalExceptionHandler`).

## Key Exception Classes
* **`BaseException`:** Root runtime exception for all business domain exceptions.
* **`ResourceNotFoundException`:** Thrown when a requested record is not found (HTTP 404).
* **`InvalidInputException`:** Thrown on business validation failures (HTTP 400).
* **`UnauthorizedException`:** Thrown on unauthenticated access attempts (HTTP 401).
* **`ForbiddenException`:** Thrown on insufficient permission attempts (HTTP 403).
* **`ConflictException`:** Thrown on duplicate constraint violations (HTTP 409).

## Global Exception Handler Behavior
Automatically catches thrown exceptions and formats them into standard `ApiResponse<T>` envelopes with field-level validation maps.
