package com.bloodbank.donation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationRecordRequest {

    @NotNull(message = "Donor profile ID is required")
    private Long donorProfileId;

    @NotNull(message = "Blood bank ID is required")
    private Long bloodBankId;

    @NotNull(message = "Eligibility check ID is required")
    private Long eligibilityCheckId;

    @NotNull(message = "Blood group ID is required")
    private Long bloodGroupId;

    @NotBlank(message = "Component type code is required")
    private String componentTypeCode;

    private double unitsCollected;

    @NotNull(message = "Donation date is required")
    private LocalDate donationDate;

    private String notes;
}
