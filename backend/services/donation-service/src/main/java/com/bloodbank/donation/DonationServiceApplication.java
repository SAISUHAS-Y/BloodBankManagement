package com.bloodbank.donation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.bloodbank.donation", "com.bloodbank.common"})
@EnableJpaRepositories(basePackages = {"com.bloodbank.donation", "com.bloodbank.common"})
@AutoConfigurationPackage(basePackages = {"com.bloodbank.donation", "com.bloodbank.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
@EnableFeignClients(basePackages = "com.bloodbank.common.contracts.client")
public class DonationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DonationServiceApplication.class, args);
    }
}
