package com.bloodbank.transaction.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestDto {

    @NotNull(message = "Hospital ID is required")
    private Long hospitalId;

    @NotNull(message = "Requester staff ID is required")
    private Long requestedBy;

    @NotNull(message = "Blood group ID is required")
    private Long bloodGroupId;

    @NotBlank(message = "Component type code is required")
    private String componentTypeCode;

    private double unitsRequested;

    @NotBlank(message = "Urgency level is required")
    private String urgency;

    @NotBlank(message = "Patient name is required")
    private String patientName;

    private int patientAge;
    private String clinicalReason;
}
