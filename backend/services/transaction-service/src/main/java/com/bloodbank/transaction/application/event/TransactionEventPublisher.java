package com.bloodbank.transaction.application.event;

import com.bloodbank.common.events.outbox.EventPublisher;
import com.bloodbank.common.events.transaction.BloodRequestCreatedEvent;
import com.bloodbank.transaction.application.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventPublisher {

    private final EventPublisher eventPublisher;

    public void publishBloodRequestCreated(Long requestId, Long hospitalId, String hospitalName,
                                            String bloodGroupCode, String componentTypeCode,
                                            Double unitsRequested, String urgency, Instant requiredBy,
                                            String traceId) {
        BloodRequestCreatedEvent payload = BloodRequestCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .requestId(requestId)
                .hospitalId(hospitalId)
                .hospitalName(hospitalName)
                .bloodGroupCode(bloodGroupCode)
                .componentTypeCode(componentTypeCode)
                .unitsRequested(unitsRequested)
                .urgency(urgency)
                .requiredBy(requiredBy)
                .build();

        String routingKey = "transaction.request.created";

        log.info("[TRANSACTION SERVICE] Staging BloodRequestCreatedEvent: exchange={} routingKey={} requestId={} urgency={}",
                RabbitConfig.TRANSACTION_EXCHANGE, routingKey, requestId, urgency);

        eventPublisher.publish(RabbitConfig.TRANSACTION_EXCHANGE, routingKey, payload);
    }
}
