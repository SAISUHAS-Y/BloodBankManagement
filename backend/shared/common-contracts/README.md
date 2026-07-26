# 📄 Common Contracts Library

## Overview
`common-contracts` exposes inter-service Spring Cloud OpenFeign client definitions and shared request/response DTOs for type-safe RPC calls between microservices.

## Inter-Service Feign Clients
* **`UserFeignClient`:** Inter-service client targeting `user-service`.
* **`HospitalFeignClient`:** Inter-service client targeting `hospital-service`.
* **`BloodBankFeignClient`:** Inter-service client targeting `blood-bank-service`.
* **`MasterFeignClient`:** Inter-service client targeting `master-service`.

## Usage
Include in microservice `pom.xml` and enable Feign Clients on the main application class:
```java
@EnableFeignClients(basePackages = "com.bloodbank.common.contracts.client")
```
