package com.bloodbank.identity.application.config;

import com.bloodbank.common.events.config.SharedRabbitConfig;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharedRabbitConfig.class)
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "bloodbank.user.exchange";
    public static final String QUEUE_NAME = "bloodbank.user.registered.queue";
    public static final String ROUTING_KEY = "user.registered";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userRegisteredQueue).to(userExchange).with(ROUTING_KEY);
    }
}
