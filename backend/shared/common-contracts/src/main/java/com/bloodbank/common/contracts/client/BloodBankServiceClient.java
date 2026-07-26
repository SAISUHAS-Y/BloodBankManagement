package com.bloodbank.common.contracts.client;

import com.bloodbank.common.contracts.config.FeignClientConfig;
import com.bloodbank.common.contracts.dto.BloodBankSummaryResponse;
import com.bloodbank.common.contracts.dto.BloodStockAdjustRequest;
import com.bloodbank.common.core.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "blood-bank-service",
        contextId = "bloodBankServiceClient",
        path = "/api/v1/blood-banks",
        configuration = FeignClientConfig.class
)
public interface BloodBankServiceClient {

    @GetMapping("/{id}")
    @CircuitBreaker(name = "bloodBankService", fallbackMethod = "getBloodBankByIdFallback")
    @Retry(name = "bloodBankService")
    ApiResponse<BloodBankSummaryResponse> getBloodBankById(@PathVariable("id") Long id);

    @PostMapping("/stock/increment")
    @CircuitBreaker(name = "bloodBankService", fallbackMethod = "incrementStockFallback")
    @Retry(name = "bloodBankService")
    ApiResponse<Void> incrementStock(@RequestBody BloodStockAdjustRequest request);

    @PostMapping("/stock/decrement")
    @CircuitBreaker(name = "bloodBankService", fallbackMethod = "decrementStockFallback")
    @Retry(name = "bloodBankService")
    ApiResponse<Void> decrementStock(@RequestBody BloodStockAdjustRequest request);

    default ApiResponse<BloodBankSummaryResponse> getBloodBankByIdFallback(Long id, Throwable t) {
        BloodBankSummaryResponse fallback = BloodBankSummaryResponse.builder()
                .id(id)
                .name("Unknown Blood Bank")
                .active(false)
                .build();
        return ApiResponse.success("Degraded response: Blood Bank Service unreachable", fallback);
    }

    default ApiResponse<Void> incrementStockFallback(BloodStockAdjustRequest request, Throwable t) {
        return ApiResponse.failure(503, "Blood Bank Service unreachable. Stock increment failed.", null);
    }

    default ApiResponse<Void> decrementStockFallback(BloodStockAdjustRequest request, Throwable t) {
        return ApiResponse.failure(503, "Blood Bank Service unreachable. Stock decrement failed.", null);
    }
}
