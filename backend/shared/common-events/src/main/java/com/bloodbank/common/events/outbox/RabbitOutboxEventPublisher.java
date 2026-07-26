package com.bloodbank.common.events.outbox;

import com.bloodbank.common.core.context.RequestContext;
import com.bloodbank.common.events.DomainEvent;
import com.bloodbank.common.events.EventEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitOutboxEventPublisher implements EventPublisher {

    private final EventOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    /**
     * TRANSACTIONAL OUTBOX PATTERN IMPLEMENTATION:
     * ---------------------------------------------
     * RATIONALE: Publishing directly to RabbitMQ inside a business transaction creates the dual-write problem:
     * If the DB transaction commits successfully but the network connection to RabbitMQ fails (or RabbitMQ crashes),
     * the event is permanently lost, leaving downstream microservices out of sync.
     *
     * SOLUTION: Instead of publishing to RabbitMQ immediately, this method converts the event into an EventEnvelope
     * and persists a row into the service's `event_outbox` DB table WITHIN THE SAME ATOMIC DB TRANSACTION.
     * A background poller (OutboxPollerScheduler) then reliably picks up PENDING rows, publishes them to RabbitMQ,
     * and marks them PUBLISHED upon confirmation.
     */
    @Override
    @Transactional
    public <T extends DomainEvent> void publish(String exchange, String routingKey, T event) {
        try {
            String traceId = RequestContext.getTraceId();
            EventEnvelope<T> envelope = EventEnvelope.wrap(event, traceId);
            String payloadJson = objectMapper.writeValueAsString(envelope);

            EventOutbox outbox = EventOutbox.builder()
                    .eventId(envelope.getEventId())
                    .eventType(envelope.getEventType())
                    .exchange(exchange)
                    .routingKey(routingKey)
                    .payload(payloadJson)
                    .traceId(traceId)
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();

            outboxRepository.save(outbox);
            log.info("[OUTBOX ENQUEUE] Saved event outbox record. EventId: {}, Type: {}, Exchange: {}, RoutingKey: {}",
                    envelope.getEventId(), envelope.getEventType(), exchange, routingKey);
        } catch (Exception e) {
            log.error("Failed to enqueue event to outbox table", e);
            throw new IllegalStateException("Failed to stage event for transactional outbox", e);
        }
    }
}
