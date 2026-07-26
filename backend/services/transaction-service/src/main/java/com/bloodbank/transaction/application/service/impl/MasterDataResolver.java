package com.bloodbank.transaction.application.service.impl;

import com.bloodbank.common.contracts.client.BloodBankServiceClient;
import com.bloodbank.common.contracts.client.HospitalServiceClient;
import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.dto.BloodBankSummaryResponse;
import com.bloodbank.common.contracts.dto.HospitalContractResponse;
import com.bloodbank.common.contracts.dto.LookupItemContractResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterDataResolver {

    private final MasterServiceClient masterServiceClient;
    private final HospitalServiceClient hospitalServiceClient;
    private final BloodBankServiceClient bloodBankServiceClient;

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
            LookupItemContractResponse item = masterServiceClient.getLookupItem("COMPONENT_TYPE", componentCode).getData();
            return item != null ? item.getLabel() : "Unknown";
        } catch (Exception e) {
            log.error("Failed to resolve component type label for code: {}", componentCode, e);
            return "Unknown";
        }
    }

    @Cacheable(value = "hospital_names", key = "#hospitalId")
    public String getHospitalName(Long hospitalId) {
        log.info("Resolving hospital name for ID: {}", hospitalId);
        try {
            HospitalContractResponse hospital = hospitalServiceClient.getHospitalById(hospitalId).getData();
            return hospital != null ? hospital.getName() : "Unknown Hospital";
        } catch (Exception e) {
            log.error("Failed to resolve hospital name for ID: {}", hospitalId, e);
            return "Unknown Hospital";
        }
    }

    @Cacheable(value = "blood_bank_names", key = "#bloodBankId")
    public String getBloodBankName(Long bloodBankId) {
        log.info("Resolving blood bank name for ID: {}", bloodBankId);
        try {
            BloodBankSummaryResponse bb = bloodBankServiceClient.getBloodBankById(bloodBankId).getData();
            return bb != null ? bb.getName() : "Unknown Blood Bank";
        } catch (Exception e) {
            log.error("Failed to resolve blood bank name for ID: {}", bloodBankId, e);
            return "Unknown Blood Bank";
        }
    }
}
