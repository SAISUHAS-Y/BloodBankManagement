package com.bloodbank.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.bloodbank.identity", "com.bloodbank.common"})
@EnableJpaRepositories(basePackages = {"com.bloodbank.identity", "com.bloodbank.common"})
@AutoConfigurationPackage(basePackages = {"com.bloodbank.identity", "com.bloodbank.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
public class IdentityServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
