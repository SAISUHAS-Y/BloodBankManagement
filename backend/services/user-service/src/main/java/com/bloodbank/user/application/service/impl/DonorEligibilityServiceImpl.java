package com.bloodbank.user.application.service.impl;

import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.user.application.dto.response.DonorEligibilityResponse;
import com.bloodbank.user.application.service.DonorEligibilityService;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.enums.DonorStatus;
import com.bloodbank.user.domain.repository.DonorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorEligibilityServiceImpl implements DonorEligibilityService {

    private final DonorProfileRepository donorProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public DonorEligibilityResponse calculateEligibility(Long donorId) {
        log.info("Calculating donation eligibility for donor ID: {}", donorId);

        DonorProfile donor = donorProfileRepository.findById(donorId)
                .orElseThrow(() -> new ResourceNotFoundException("Donor profile not found with ID: " + donorId));

        LocalDate today = LocalDate.now();

        if (donor.getDonorStatus() == DonorStatus.BLACKLISTED) {
            return DonorEligibilityResponse.builder()
                    .isEligible(false)
                    .nextEligibleDate(null)
                    .daysRemaining(-1)
                    .donorStatus(DonorStatus.BLACKLISTED.name())
                    .lastDonationDate(donor.getLastDonationDate())
                    .reason("Donor is permanently blacklisted.")
                    .build();
        }

        if (donor.getDonorStatus() == DonorStatus.DEFERRED) {
            LocalDate deferredUntil = donor.getDeferredUntilDate();
            if (deferredUntil != null && today.isBefore(deferredUntil)) {
                long days = ChronoUnit.DAYS.between(today, deferredUntil);
                return DonorEligibilityResponse.builder()
                        .isEligible(false)
                        .nextEligibleDate(deferredUntil)
                        .daysRemaining(days)
                        .donorStatus(DonorStatus.DEFERRED.name())
                        .lastDonationDate(donor.getLastDonationDate())
                        .reason("Donor is under active deferral until " + deferredUntil)
                        .build();
            }
        }

        if (donor.getLastDonationDate() == null) {
            return DonorEligibilityResponse.builder()
                    .isEligible(true)
                    .nextEligibleDate(today)
                    .daysRemaining(0)
                    .donorStatus(donor.getDonorStatus().name())
                    .lastDonationDate(null)
                    .reason("No prior donation record. Donor is eligible.")
                    .build();
        }

        int intervalDays = "MALE".equalsIgnoreCase(donor.getGenderCode()) ? 90 : 120;
        LocalDate nextEligibleDate = donor.getLastDonationDate().plusDays(intervalDays);

        if (today.isBefore(nextEligibleDate)) {
            long days = ChronoUnit.DAYS.between(today, nextEligibleDate);
            return DonorEligibilityResponse.builder()
                    .isEligible(false)
                    .nextEligibleDate(nextEligibleDate)
                    .daysRemaining(days)
                    .donorStatus(donor.getDonorStatus().name())
                    .lastDonationDate(donor.getLastDonationDate())
                    .reason("Minimum donation interval of " + intervalDays + " days not met. Next eligible on " + nextEligibleDate)
                    .build();
        }

        return DonorEligibilityResponse.builder()
                .isEligible(true)
                .nextEligibleDate(today)
                .daysRemaining(0)
                .donorStatus(donor.getDonorStatus().name())
                .lastDonationDate(donor.getLastDonationDate())
                .reason("Donor meets all eligibility criteria.")
                .build();
    }
}
