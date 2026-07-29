package com.bloodbank.identity.api.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String IDENTITY_EXCHANGE = "identity.events";
    public static final String AUDIT_QUEUE = "identity.audit.queue";
    public static final String USER_EVENTS_QUEUE = "identity.user.events.queue";
    public static final String AUDIT_ROUTING_KEY = "identity.audit.#";
    public static final String USER_EVENTS_ROUTING_KEY = "identity.user.#";

    @Bean
    public TopicExchange identityExchange() {
        return new TopicExchange(IDENTITY_EXCHANGE, true, false);
    }

    @Bean
    public Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true);
    }

    @Bean
    public Queue userEventsQueue() {
        return new Queue(USER_EVENTS_QUEUE, true);
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange identityExchange) {
        return BindingBuilder.bind(auditQueue).to(identityExchange).with(AUDIT_ROUTING_KEY);
    }

    @Bean
    public Binding userEventsBinding(Queue userEventsQueue, TopicExchange identityExchange) {
        return BindingBuilder.bind(userEventsQueue).to(identityExchange).with(USER_EVENTS_ROUTING_KEY);
    }
}
