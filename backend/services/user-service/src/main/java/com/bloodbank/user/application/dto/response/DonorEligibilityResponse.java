package com.bloodbank.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Donor eligibility calculation details")
public class DonorEligibilityResponse {

    @Schema(example = "true", description = "Whether the donor is currently eligible to donate blood")
    private boolean isEligible;

    @Schema(example = "2026-08-01", description = "The date when donor becomes next eligible to donate")
    private LocalDate nextEligibleDate;

    @Schema(example = "15", description = "Number of days remaining until next eligible date")
    private long daysRemaining;

    @Schema(example = "ACTIVE", description = "Current donor status")
    private String donorStatus;

    @Schema(description = "Last recorded donation date")
    private LocalDate lastDonationDate;

    @Schema(example = "Donor is eligible to donate whole blood", description = "Human readable explanation or deferral reason")
    private String reason;
}
