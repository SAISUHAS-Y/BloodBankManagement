package com.bloodbank.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Boots the Spring Cloud API Gateway (Reactive Netty).
 * 
 * DESIGN DECISION: Scan only the gateway-specific package to prevent Spring from 
 * automatically scanning servlet-specific filters in the shared security module.
 */
@SpringBootApplication(
    scanBasePackages = "com.bloodbank.gateway",
    excludeName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration"
    }
)
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
