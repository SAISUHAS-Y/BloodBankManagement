package com.bloodbank.donation.application.service.impl;

import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.client.UserServiceClient;
import com.bloodbank.common.contracts.dto.DonorProfileContractResponse;
import com.bloodbank.common.contracts.dto.LookupItemContractResponse;
import com.bloodbank.common.exception.BusinessRuleViolationException;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.enums.ErrorCode;
import com.bloodbank.donation.application.dto.EligibilityRequest;
import com.bloodbank.donation.application.dto.EligibilityResponse;
import com.bloodbank.donation.application.service.EligibilityService;
import com.bloodbank.donation.domain.entity.EligibilityCheck;
import com.bloodbank.donation.domain.repository.EligibilityCheckRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityServiceImpl implements EligibilityService {

    private final EligibilityCheckRepository eligibilityCheckRepository;
    private final MasterServiceClient masterServiceClient;
    private final UserServiceClient userServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public EligibilityResponse conductScreening(EligibilityRequest request, String componentTypeCode) {
        log.info("Conducting pre-donation eligibility screening for donor profile ID: {}", request.getDonorProfileId());

        DonorProfileContractResponse donor = userServiceClient.getDonorById(request.getDonorProfileId()).getData();
        if (donor == null || donor.getId() == null) {
            throw new InvalidInputException("Linked donor profile does not exist: " + request.getDonorProfileId());
        }

        if ("BLACKLISTED".equalsIgnoreCase(donor.getDonorStatus())) {
            throw new InvalidInputException("Donor is blacklisted and permanently ineligible to donate.");
        }

        // DEFERRAL DATE PRE-CHECK FIRST: Must be evaluated BEFORE vitals screening
        if (donor.getDeferredUntilDate() != null && donor.getDeferredUntilDate().isAfter(LocalDate.now())) {
            log.warn("Donor ID {} is currently deferred until {}", donor.getId(), donor.getDeferredUntilDate());
            throw new BusinessRuleViolationException(
                    "Donor is currently deferred until " + donor.getDeferredUntilDate(),
                    ErrorCode.DONOR_CURRENTLY_DEFERRED
            );
        }

        boolean eligible = true;
        String reasonCode = null;
        LocalDate deferredUntil = null;

        if (donor.getLastDonationDate() != null) {
            int minIntervalDays = 90;
            try {
                LookupItemContractResponse donationTypeLookup = masterServiceClient.getLookupItem("DONATION_TYPE", componentTypeCode).getData();
                if (donationTypeLookup != null && donationTypeLookup.getMetadata() != null) {
                    JsonNode rootNode = objectMapper.readTree(donationTypeLookup.getMetadata());
                    minIntervalDays = rootNode.path("minIntervalDays").asInt(90);
                }
            } catch (Exception e) {
                log.error("Failed to parse interval metadata from Master Service for donation type: {}", componentTypeCode, e);
            }

            LocalDate nextEligibleDate = donor.getLastDonationDate().plusDays(minIntervalDays);
            if (nextEligibleDate.isAfter(LocalDate.now())) {
                eligible = false;
                reasonCode = "INTERVAL_NOT_MET";
                deferredUntil = nextEligibleDate;
                log.warn("Donor ID {} fails eligibility check due to minimum interval limit: deferred until {}",
                        request.getDonorProfileId(), deferredUntil);
            }
        }

        if (eligible) {
            if (request.getWeightKg() < 50.0) {
                eligible = false;
                reasonCode = "UNDERWEIGHT";
                deferredUntil = LocalDate.now().plusDays(30);
            } else if (request.getHemoglobinLevel() < 12.5) {
                eligible = false;
                reasonCode = "LOW_HEMOGLOBIN";
                deferredUntil = LocalDate.now().plusDays(28);
            } else if (request.getSystolicBp() < 90.0 || request.getSystolicBp() > 140.0 ||
                       request.getDiastolicBp() < 60.0 || request.getDiastolicBp() > 90.0) {
                eligible = false;
                reasonCode = "ABNORMAL_BP";
                deferredUntil = LocalDate.now().plusDays(7);
            }
        }

        if (!eligible && reasonCode != null && !"INTERVAL_NOT_MET".equals(reasonCode)) {
            Boolean active = masterServiceClient.validateLookupItemActive("DEFERRAL_REASON", reasonCode).getData();
            if (active == null || !active) {
                log.warn("Screening generated deferral reason code {} which is inactive/invalid in Master Service.", reasonCode);
            }
        }

        EligibilityCheck check = new EligibilityCheck();
        check.setDonorProfileId(request.getDonorProfileId());
        check.setCheckedAt(Instant.now());
        check.setHemoglobinLevel(request.getHemoglobinLevel());
        check.setWeightKg(request.getWeightKg());
        check.setSystolicBp(request.getSystolicBp());
        check.setDiastolicBp(request.getDiastolicBp());
        check.setEligible(eligible);
        check.setDeferralReasonCode(reasonCode);
        check.setDeferredUntilDate(deferredUntil);
        check.setCheckedBy(request.getCheckedBy());
        check.setCreatedBy("STAFF_" + request.getCheckedBy());
        check.setUpdatedBy("STAFF_" + request.getCheckedBy());

        EligibilityCheck saved = eligibilityCheckRepository.save(check);
        return mapToResponse(saved);
    }

    private EligibilityResponse mapToResponse(EligibilityCheck check) {
        return EligibilityResponse.builder()
                .id(check.getId())
                .donorProfileId(check.getDonorProfileId())
                .checkedAt(check.getCheckedAt())
                .hemoglobinLevel(check.getHemoglobinLevel())
                .weightKg(check.getWeightKg())
                .systolicBp(check.getSystolicBp())
                .diastolicBp(check.getDiastolicBp())
                .eligible(check.isEligible())
                .deferralReasonCode(check.getDeferralReasonCode())
                .deferredUntilDate(check.getDeferredUntilDate())
                .checkedBy(check.getCheckedBy())
                .build();
    }
}
