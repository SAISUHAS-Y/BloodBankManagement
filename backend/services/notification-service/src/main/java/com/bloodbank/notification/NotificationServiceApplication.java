package com.bloodbank.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.bloodbank.notification", "com.bloodbank.common"})
@EnableJpaRepositories(basePackages = {"com.bloodbank.notification", "com.bloodbank.common"})
@AutoConfigurationPackage(basePackages = {"com.bloodbank.notification", "com.bloodbank.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}
