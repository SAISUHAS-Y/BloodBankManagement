package com.bloodbank.master.application.config;

import com.bloodbank.common.events.config.SharedRabbitConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(SharedRabbitConfig.class)
public class RabbitConfig {

    public static final String MASTER_EXCHANGE = "bloodbank.master.exchange";

    @Bean
    public TopicExchange masterExchange() {
        return new TopicExchange(MASTER_EXCHANGE, true, false);
    }
}
