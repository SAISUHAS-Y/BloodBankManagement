package com.bloodbank.identity.application.event;

import com.bloodbank.common.events.outbox.EventPublisher;
import com.bloodbank.common.events.user.UserRegisteredEvent;
import com.bloodbank.identity.application.config.RabbitConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final EventPublisher eventPublisher;

    public void publishUserRegistered(UserRegisteredEvent event, String traceId) {
        log.info("[IDENTITY SERVICE] Staging UserRegisteredEvent (ID: {}) to event outbox table. Exchange: '{}', Key: '{}'",
                event.getEventId(), RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY);

        eventPublisher.publish(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, event);
    }
}
