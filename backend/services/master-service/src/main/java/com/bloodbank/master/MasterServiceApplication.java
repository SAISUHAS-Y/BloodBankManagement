package com.bloodbank.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.bloodbank.master", "com.bloodbank.common"})
@EnableJpaRepositories(basePackages = {"com.bloodbank.master", "com.bloodbank.common"})
@AutoConfigurationPackage(basePackages = {"com.bloodbank.master", "com.bloodbank.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
public class MasterServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MasterServiceApplication.class, args);
    }
}
