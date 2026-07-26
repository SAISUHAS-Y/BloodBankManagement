package com.bloodbank.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.bloodbank.hospital", "com.bloodbank.common"})
@EnableJpaRepositories(basePackages = {"com.bloodbank.hospital", "com.bloodbank.common"})
@AutoConfigurationPackage(basePackages = {"com.bloodbank.hospital", "com.bloodbank.common"})
@EnableDiscoveryClient
@EnableJpaAuditing
@EnableScheduling
@EnableCaching
@EnableFeignClients(basePackages = "com.bloodbank.common.contracts.client")
public class HospitalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospitalServiceApplication.class, args);
    }
}
