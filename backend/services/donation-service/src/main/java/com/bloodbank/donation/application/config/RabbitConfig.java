package com.bloodbank.donation.application.config;

import com.bloodbank.common.events.config.SharedRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharedRabbitConfig.class)
public class RabbitConfig {

    public static final String DONATION_EXCHANGE = "bloodbank.donation.exchange";

    @Bean
    public TopicExchange donationExchange() {
        return new TopicExchange(DONATION_EXCHANGE, true, false);
    }
}
