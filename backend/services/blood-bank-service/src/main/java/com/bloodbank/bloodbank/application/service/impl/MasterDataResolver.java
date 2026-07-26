package com.bloodbank.bloodbank.application.service.impl;

import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.dto.LookupItemContractResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MasterDataResolver {

    private final MasterServiceClient masterServiceClient;

    public MasterDataResolver(MasterServiceClient masterServiceClient) {
        this.masterServiceClient = masterServiceClient;
    }

    @Cacheable(value = "blood_bank_type_labels", key = "#typeCode")
    public String getBloodBankTypeLabel(String typeCode) {
        log.info("Resolving blood bank type label for code: {}", typeCode);
        try {
            LookupItemContractResponse item = masterServiceClient.getLookupItem("BLOOD_BANK_TYPE", typeCode).getData();
            return item != null ? item.getLabel() : "Unknown";
        } catch (Exception e) {
            log.error("Failed to resolve blood bank type label for code: {}", typeCode, e);
            return "Unknown";
        }
    }

    @Cacheable(value = "blood_group_labels", key = "#bloodGroupId")
    public String getBloodGroupLabel(Long bloodGroupId) {
        log.info("Resolving blood group label for ID: {}", bloodGroupId);
        try {
            return masterServiceClient.getBloodGroups().getData().stream()
                    .filter(g -> g.getId().equals(bloodGroupId))
                    .map(g -> g.getDisplayName())
                    .findFirst()
                    .orElse("Unknown");
        } catch (Exception e) {
            log.error("Failed to resolve blood group label for ID: {}", bloodGroupId, e);
            return "Unknown";
        }
    }

    @Cacheable(value = "component_type_labels", key = "#componentCode")

    public String getComponentTypeLabel(String componentCode) {
        log.info("Resolving component type label for code: {}", componentCode);
        try {
            LookupItemContractResponse item = masterServiceClient.getLookupItem("COMPONENT_TYPE", componentCode)
                    .getData();
            return item != null ? item.getLabel() : "Unknown";
        } catch (Exception e) {
            log.error("Failed to resolve component type label for code: {}", componentCode, e);
            return "Unknown";
        }
    }

    @Cacheable(value = "state_names", key = "#stateId")
    public String getStateName(Long stateId) {
        log.info("Resolving state name for ID: {}", stateId);
        try {
            return masterServiceClient.getStates().getData().stream()
                    .filter(s -> s.getId().equals(stateId))
                    .map(s -> s.getName())
                    .findFirst()
                    .orElse("Unknown");
        } catch (Exception e) {
            log.error("Failed to resolve state name for ID: {}", stateId, e);
            return "Unknown";
        }
    }

    @Cacheable(value = "district_names", key = "#stateId + ':' + #districtId")
    public String getDistrictName(Long stateId, Long districtId) {
        log.info("Resolving district name for stateId={} districtId={}", stateId, districtId);
        try {
            return masterServiceClient.getDistrictsByState(stateId).getData().stream()
                    .filter(d -> d.getId().equals(districtId))
                    .map(d -> d.getName())
                    .findFirst()
                    .orElse("Unknown");
        } catch (Exception e) {
            log.error("Failed to resolve district name for ID: {}", districtId, e);
            return "Unknown";
        }
    }

    @Cacheable(value = "city_names", key = "#districtId + ':' + #cityId")
    public String getCityName(Long districtId, Long cityId) {
        log.info("Resolving city name for districtId={} cityId={}", districtId, cityId);
        try {
            return masterServiceClient.getCitiesByDistrict(districtId).getData().stream()
                    .filter(c -> c.getId().equals(cityId))
                    .map(c -> c.getName())
                    .findFirst()
                    .orElse("Unknown");
        } catch (Exception e) {
            log.error("Failed to resolve city name for ID: {}", cityId, e);
            return "Unknown";
        }
    }
}
