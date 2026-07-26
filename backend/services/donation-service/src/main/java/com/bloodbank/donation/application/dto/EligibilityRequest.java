package com.bloodbank.donation.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityRequest {

    @NotNull(message = "Donor profile ID is required")
    private Long donorProfileId;

    private double hemoglobinLevel;
    private double weightKg;
    private double systolicBp;
    private double diastolicBp;

    @NotNull(message = "Staff ID is required")
    private Long checkedBy;
}
