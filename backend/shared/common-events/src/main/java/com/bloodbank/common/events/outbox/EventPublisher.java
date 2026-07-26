package com.bloodbank.common.events.outbox;

import com.bloodbank.common.events.DomainEvent;

public interface EventPublisher {
    <T extends DomainEvent> void publish(String exchange, String routingKey, T event);
}
