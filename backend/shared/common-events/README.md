# 📨 Common Events Library

## Overview
`common-events` provides the **Transactional Outbox Pattern** implementation and shared RabbitMQ messaging DTOs to guarantee transactional event publishing across microservices.

## Contents
* **`EventOutbox`:** JPA entity mapping the `event_outbox` table.
* **`EventOutboxRepository`:** Spring Data JPA repository for polling pending outbox messages.
* **`RabbitOutboxEventPublisher`:** Publishes domain events to the outbox database table in the same local ACID transaction.
* **`OutboxPollerScheduler`:** Background scheduled poller that reads pending events and publishes them to RabbitMQ exchanges.

## Domain Events Included
* `UserRegisteredEvent`, `DonorCreatedEvent`, `BloodDonatedEvent`, `BloodRequestedEvent`, `BloodDispatchedEvent`, `NotificationTriggeredEvent`.

## Usage
Include in `pom.xml` and enable JPA repository scanning for `com.bloodbank.common`:
```java
@EnableJpaRepositories(basePackages = {"com.bloodbank.<service>", "com.bloodbank.common"})
@AutoConfigurationPackage(basePackages = {"com.bloodbank.<service>", "com.bloodbank.common"})
```
