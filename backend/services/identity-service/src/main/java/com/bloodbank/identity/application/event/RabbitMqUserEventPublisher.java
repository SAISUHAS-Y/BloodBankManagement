package com.bloodbank.identity.application.event;

import com.bloodbank.identity.api.config.RabbitMqConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMqUserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishSecurityAuditEvent(String eventType, String username, String details, String ipAddress) {
        try {
            SecurityAuditEventPayload payload = new SecurityAuditEventPayload(eventType, username, details, ipAddress, System.currentTimeMillis());
            rabbitTemplate.convertAndSend(RabbitMqConfig.IDENTITY_EXCHANGE, "identity.audit." + eventType.toLowerCase(), payload);
            log.debug("Published security audit event [{}] to RabbitMQ exchange [{}]", eventType, RabbitMqConfig.IDENTITY_EXCHANGE);
        } catch (Exception e) {
            log.warn("Could not publish audit event to RabbitMQ (bus may be unreachable): {}", e.getMessage());
        }
    }

    public record SecurityAuditEventPayload(String eventType, String username, String details, String ipAddress, long timestamp) {}
}
