package com.bloodbank.common.contracts.client;

import com.bloodbank.common.contracts.config.FeignClientConfig;
import com.bloodbank.common.contracts.dto.HospitalContractResponse;
import com.bloodbank.common.core.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "hospital-service",
        contextId = "hospitalServiceClient",
        path = "/api/v1/hospitals",
        configuration = FeignClientConfig.class
)
public interface HospitalServiceClient {

    @GetMapping("/{id}")
    @CircuitBreaker(name = "hospitalService", fallbackMethod = "getHospitalByIdFallback")
    @Retry(name = "hospitalService")
    ApiResponse<HospitalContractResponse> getHospitalById(@PathVariable("id") Long id);

    default ApiResponse<HospitalContractResponse> getHospitalByIdFallback(Long id, Throwable t) {
        HospitalContractResponse fallback = HospitalContractResponse.builder()
                .id(id)
                .name("Unknown Hospital")
                .active(false)
                .build();
        return ApiResponse.success("Degraded response: Hospital Service unreachable", fallback);
    }
}
