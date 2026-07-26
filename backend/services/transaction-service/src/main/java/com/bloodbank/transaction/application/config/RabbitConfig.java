package com.bloodbank.transaction.application.config;

import com.bloodbank.common.events.config.SharedRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharedRabbitConfig.class)
public class RabbitConfig {

    public static final String TRANSACTION_EXCHANGE = "bloodbank.transaction.exchange";

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(TRANSACTION_EXCHANGE, true, false);
    }
}
