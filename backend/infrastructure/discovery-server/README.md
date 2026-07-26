# 🔍 Discovery Server (Eureka)

## Overview
`discovery-server` is the central service registry powered by **Spring Cloud Netflix Eureka Server**. Microservices dynamically register their network location (IP/Host & Port) with Eureka to enable client-side load balancing via Spring Cloud OpenFeign.

## Security & Credentials
* **Authentication:** HTTP Basic Auth enabled via `Spring Security`.
* **Username:** `admin`
* **Password:** `admin123`

## Port & Dashboard
* **Port:** `8761`
* **Dashboard URL:** [http://localhost:8761](http://localhost:8761)

## How to Run
```powershell
mvn -f backend\infrastructure\discovery-server spring-boot:run
```
