package com.bloodbank.common.contracts.client.fallback;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.common.contracts.client.IdentityServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class IdentityServiceClientFallback implements IdentityServiceClient {

    @Override
    public ApiResponse<UserSummaryResponse> getUserSummary(Long id) {
        log.warn("Feign fallback triggered for user ID: {}", id);
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
        return ApiResponse.success("Feign fallback: Identity Service is down", fallbackUser);
    }
}
