package com.bloodbank.common.contracts.client;

import com.bloodbank.common.contracts.config.FeignClientConfig;
import com.bloodbank.common.contracts.dto.DonorProfileContractResponse;
import com.bloodbank.common.contracts.dto.RecordDonationRequest;
import com.bloodbank.common.core.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "user-service",
        contextId = "userServiceClient",
        path = "/api/v1/donors",
        configuration = FeignClientConfig.class
)
public interface UserServiceClient {

    @GetMapping("/{id}")
    @CircuitBreaker(name = "userService", fallbackMethod = "getDonorByIdFallback")
    @Retry(name = "userService")
    ApiResponse<DonorProfileContractResponse> getDonorById(@PathVariable("id") Long id);

    @PatchMapping("/{id}/record-donation")
    @CircuitBreaker(name = "userService", fallbackMethod = "recordDonationFallback")
    @Retry(name = "userService")
    ApiResponse<DonorProfileContractResponse> recordDonation(
            @PathVariable("id") Long id,
            @RequestBody RecordDonationRequest request);

    default ApiResponse<DonorProfileContractResponse> getDonorByIdFallback(Long id, Throwable t) {
        DonorProfileContractResponse fallback = DonorProfileContractResponse.builder()
                .id(id)
                .fullName("Unknown Donor")
                .donorStatus("INACTIVE")
                .build();
        return ApiResponse.success("Degraded response: User Service unreachable", fallback);
    }

    default ApiResponse<DonorProfileContractResponse> recordDonationFallback(Long id, RecordDonationRequest request, Throwable t) {
        DonorProfileContractResponse fallback = DonorProfileContractResponse.builder()
                .id(id)
                .fullName("Unknown Donor")
                .donorStatus("INACTIVE")
                .build();
        return ApiResponse.failure(503, "User Service currently unreachable. Donation record update queued.", fallback, null);
    }
}
