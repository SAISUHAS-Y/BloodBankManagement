package com.bloodbank.common.contracts.client;

import com.bloodbank.common.contracts.config.FeignClientConfig;
import com.bloodbank.common.contracts.dto.BloodGroupContractResponse;
import com.bloodbank.common.contracts.dto.CityContractResponse;
import com.bloodbank.common.contracts.dto.DistrictContractResponse;
import com.bloodbank.common.contracts.dto.LookupItemContractResponse;
import com.bloodbank.common.contracts.dto.StateContractResponse;
import com.bloodbank.common.core.dto.ApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Collections;
import java.util.List;

@FeignClient(
        name = "master-service",
        contextId = "masterServiceClient",
        path = "/api/v1/master",
        configuration = FeignClientConfig.class
)
public interface MasterServiceClient {

    @GetMapping("/blood-groups")
    @CircuitBreaker(name = "masterService", fallbackMethod = "getBloodGroupsFallback")
    @Retry(name = "masterService")
    ApiResponse<List<BloodGroupContractResponse>> getBloodGroups();

    @GetMapping("/blood-groups/{code}")
    @CircuitBreaker(name = "masterService", fallbackMethod = "getBloodGroupByCodeFallback")
    @Retry(name = "masterService")
    ApiResponse<BloodGroupContractResponse> getBloodGroupByCode(@PathVariable("code") String code);

    @GetMapping("/lookups/{categoryCode}/{itemCode}")
    @CircuitBreaker(name = "masterService", fallbackMethod = "getLookupItemFallback")
    @Retry(name = "masterService")
    ApiResponse<LookupItemContractResponse> getLookupItem(
            @PathVariable("categoryCode") String categoryCode,
            @PathVariable("itemCode") String itemCode);

    @GetMapping("/lookups/{categoryCode}/{itemCode}/validate")
    @CircuitBreaker(name = "masterService", fallbackMethod = "validateLookupItemActiveFallback")
    @Retry(name = "masterService")
    ApiResponse<Boolean> validateLookupItemActive(
            @PathVariable("categoryCode") String categoryCode,
            @PathVariable("itemCode") String itemCode);

    @GetMapping("/locations/states")
    @CircuitBreaker(name = "masterService", fallbackMethod = "getStatesFallback")
    @Retry(name = "masterService")
    ApiResponse<List<StateContractResponse>> getStates();

    @GetMapping("/locations/states/{stateId}/districts")
    @CircuitBreaker(name = "masterService", fallbackMethod = "getDistrictsByStateFallback")
    @Retry(name = "masterService")
    ApiResponse<List<DistrictContractResponse>> getDistrictsByState(@PathVariable("stateId") Long stateId);

    @GetMapping("/locations/districts/{districtId}/cities")
    @CircuitBreaker(name = "masterService", fallbackMethod = "getCitiesByDistrictFallback")
    @Retry(name = "masterService")
    ApiResponse<List<CityContractResponse>> getCitiesByDistrict(@PathVariable("districtId") Long districtId);

    default ApiResponse<List<BloodGroupContractResponse>> getBloodGroupsFallback(Throwable t) {
        return ApiResponse.success("Degraded response: Master Service unreachable", Collections.emptyList());
    }

    default ApiResponse<BloodGroupContractResponse> getBloodGroupByCodeFallback(String code, Throwable t) {
        BloodGroupContractResponse fallback = BloodGroupContractResponse.builder()
                .code(code)
                .displayName("Unknown Blood Group")
                .universalDonor(false)
                .universalRecipient(false)
                .build();
        return ApiResponse.success("Degraded response: Master Service unreachable", fallback);
    }

    default ApiResponse<LookupItemContractResponse> getLookupItemFallback(String categoryCode, String itemCode, Throwable t) {
        LookupItemContractResponse fallback = LookupItemContractResponse.builder()
                .code(itemCode)
                .label("Unknown Lookup Item")
                .active(false)
                .build();
        return ApiResponse.success("Degraded response: Master Service unreachable", fallback);
    }

    default ApiResponse<Boolean> validateLookupItemActiveFallback(String categoryCode, String itemCode, Throwable t) {
        return ApiResponse.success("Degraded response: Master Service unreachable", false);
    }

    default ApiResponse<List<StateContractResponse>> getStatesFallback(Throwable t) {
        return ApiResponse.success("Degraded response: Master Service unreachable", Collections.emptyList());
    }

    default ApiResponse<List<DistrictContractResponse>> getDistrictsByStateFallback(Long stateId, Throwable t) {
        return ApiResponse.success("Degraded response: Master Service unreachable", Collections.emptyList());
    }

    default ApiResponse<List<CityContractResponse>> getCitiesByDistrictFallback(Long districtId, Throwable t) {
        return ApiResponse.success("Degraded response: Master Service unreachable", Collections.emptyList());
    }
}
