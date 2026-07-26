package com.bloodbank.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Standard envelope for asynchronous events transmitted via RabbitMQ.
 * Centralizes logging, tracing, schema versioning, and routing metadata.
 * 
 * SCHEMA VERSIONING GUIDELINE FOR EVENT CONSUMERS:
 * ------------------------------------------------
 * If a consumer receives an EventEnvelope with an unrecognized or future `schemaVersion`
 * higher than what it was compiled to handle:
 * 1. Log a structured WARN message (including eventId, eventType, and schemaVersion).
 * 2. Gracefully SKIP/ACK the message without throwing a deserialization/runtime exception.
 * 3. DO NOT CRASH the message listener thread or enter an infinite DLQ retry loop.
 *
 * @param <T> The concrete type of the DomainEvent payload
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T extends DomainEvent> {

    private String eventId;
    private String eventType;

    /**
     * Schema version identifier (defaults to 1).
     * Incremented when breaking structural changes occur in event payload contracts.
     */
    @Builder.Default
    private int schemaVersion = 1;

    private Instant occurredAt;
    private T payload;
    private String traceId;

    public static <T extends DomainEvent> EventEnvelope<T> wrap(T event, String traceId) {
        return EventEnvelope.<T>builder()
                .eventId(event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString())
                .eventType(event.getClass().getSimpleName())
                .schemaVersion(1)
                .occurredAt(event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now())
                .payload(event)
                .traceId(traceId)
                .build();
    }

    public static <T extends DomainEvent> EventEnvelope<T> wrap(T event, int schemaVersion, String traceId) {
        return EventEnvelope.<T>builder()
                .eventId(event.getEventId() != null ? event.getEventId() : UUID.randomUUID().toString())
                .eventType(event.getClass().getSimpleName())
                .schemaVersion(schemaVersion)
                .occurredAt(event.getOccurredAt() != null ? event.getOccurredAt() : Instant.now())
                .payload(event)
                .traceId(traceId)
                .build();
    }
}
