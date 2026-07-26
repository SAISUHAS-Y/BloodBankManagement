package com.bloodbank.master.application.event;

import com.bloodbank.common.events.EventEnvelope;
import com.bloodbank.common.events.master.LookupItemChangedEvent;
import com.bloodbank.master.application.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishLookupItemChanged(String categoryCode, String itemCode, String changeType, String traceId) {
        LookupItemChangedEvent payload = LookupItemChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .categoryCode(categoryCode)
                .itemCode(itemCode)
                .changeType(changeType)
                .build();

        EventEnvelope<LookupItemChangedEvent> envelope = EventEnvelope.wrap(payload, traceId);
        String routingKey = String.format("lookup.changed.%s", categoryCode.toLowerCase());

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    log.info("Transaction committed successfully. Publishing LookupItemChangedEvent for category={} item={}", categoryCode, itemCode);
                    try {
                        rabbitTemplate.convertAndSend(RabbitConfig.MASTER_EXCHANGE, routingKey, envelope);
                    } catch (Exception e) {
                        log.error("Failed to publish LookupItemChangedEvent after transaction commit: {}", e.getMessage(), e);
                    }
                }
            });
        } else {
            log.info("Publishing LookupItemChangedEvent to RabbitMQ exchange={} routingKey={}", RabbitConfig.MASTER_EXCHANGE, routingKey);
            rabbitTemplate.convertAndSend(RabbitConfig.MASTER_EXCHANGE, routingKey, envelope);
        }
    }
}
