package com.bloodbank.common.contracts.client;

import com.bloodbank.common.contracts.config.FeignClientConfig;
import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.common.core.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;

@FeignClient(
        name = "identity-service",
        contextId = "identityServiceClient",
        path = "/api/v1/users",
        configuration = FeignClientConfig.class
)
public interface IdentityServiceClient {

    @GetMapping("/{id}/summary")
    @CircuitBreaker(name = "identityService", fallbackMethod = "getUserSummaryFallback")
    @Retry(name = "identityService")
    ApiResponse<UserSummaryResponse> getUserSummary(@PathVariable("id") Long id);

    default ApiResponse<UserSummaryResponse> getUserSummaryFallback(Long id, Throwable t) {
        UserSummaryResponse fallbackUser = UserSummaryResponse.builder()
                .id(id)
                .username("unknown_user")
                .email("unknown@bloodbank.org")
                .firstName("Unknown")
                .lastName("User")
                .roles(Collections.emptySet())
                .permissions(Collections.emptySet())
                .active(false)
                .build();
        return ApiResponse.success("Degraded response: Identity Service unreachable", fallbackUser);
    }
}
