package com.bloodbank.common.contracts.client;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.contracts.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "transaction-service", configuration = FeignClientConfig.class)
public interface TransactionServiceClient {

    @GetMapping("/api/v1/transactions/internal/hospitals/{hospitalId}/has-requests")
    ApiResponse<Boolean> hasHospitalRequests(@PathVariable("hospitalId") Long hospitalId);
}
