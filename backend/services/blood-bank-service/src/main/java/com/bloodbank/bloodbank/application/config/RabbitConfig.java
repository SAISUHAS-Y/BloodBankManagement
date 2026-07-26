package com.bloodbank.bloodbank.application.config;

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

    public static final String DONATION_EXCHANGE = "bloodbank.donation.exchange";
    public static final String DONATION_COMPLETED_QUEUE = "bloodbank.donation.completed.queue";
    public static final String DONATION_COMPLETED_ROUTING_KEY = "donation.completed";

    @Bean
    public TopicExchange donationExchange() {
        return new TopicExchange(DONATION_EXCHANGE, true, false);
    }

    @Bean
    public Queue donationCompletedQueue() {
        return new Queue(DONATION_COMPLETED_QUEUE, true);
    }

    @Bean
    public Binding donationCompletedBinding() {
        return BindingBuilder.bind(donationCompletedQueue())
                .to(donationExchange())
                .with(DONATION_COMPLETED_ROUTING_KEY);
    }
}
