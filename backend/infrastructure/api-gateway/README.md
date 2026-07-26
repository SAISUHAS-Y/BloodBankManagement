# 🌐 API Gateway

## Overview
`api-gateway` is the single entry point to the Blood Bank Management Platform built on **Spring Cloud Gateway (WebFlux / Netty)**. It handles API routing, JWT token validation, rate limiting, CORS configuration, and aggregated Swagger UI documentation.

## Features
* **Reactive Gateway Routing:** Dynamic routing based on Eureka service registry names.
* **Central Swagger UI Aggregator:** Aggregates `/v3/api-docs` across all domain microservices at `http://localhost:8080/swagger-ui.html`.
* **JWT Filter Validation:** Validates symmetric HMAC HS256 tokens and passes user identity headers down to downstream microservices.

## Port & Health Check
* **Port:** `8080`
* **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
* **Health Check:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## How to Run
```powershell
mvn -f backend\infrastructure\api-gateway spring-boot:run
```
