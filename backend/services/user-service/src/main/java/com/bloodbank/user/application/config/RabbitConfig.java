package com.bloodbank.user.application.config;

import com.bloodbank.common.events.config.SharedRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharedRabbitConfig.class)
public class RabbitConfig {

    public static final String DONOR_EXCHANGE = "bloodbank.donor.exchange";
    public static final String STAFF_EXCHANGE = "bloodbank.staff.exchange";

    @Bean
    public TopicExchange donorExchange() {
        return new TopicExchange(DONOR_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange staffExchange() {
        return new TopicExchange(STAFF_EXCHANGE, true, false);
    }
}
