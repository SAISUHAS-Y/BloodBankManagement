# ⚙️ Config Server

## Overview
`config-server` is the centralized configuration management module powered by **Spring Cloud Config Server**. It serves central YAML configuration files to all microservices in the Blood Bank Management platform.

## Configuration Source
Configurations are stored centrally under `/src/main/resources/shared-configs/`:
* `application.yml`: System-wide defaults (Eureka client URL, Zipkin tracing endpoint, Jackson formatting, etc.).
* `identity-service.yml`: Identity service DB & JWT token parameters.
* `hospital-service.yml`, `user-service.yml`, `blood-bank-service.yml`, etc.

## Port & Health Check
* **Port:** `8888`
* **Health Endpoint:** [http://localhost:8888/actuator/health](http://localhost:8888/actuator/health)

## How to Run
```powershell
mvn -f backend\infrastructure\config-server spring-boot:run
```
