package com.bloodbank.bloodbank.application.dto;

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
public class BloodBankRequest {

    @NotBlank(message = "Blood bank name is required")
    private String name;

    @NotBlank(message = "License number is required")
    private String licenseNumber;

    @NotBlank(message = "Blood bank type code is required")
    private String bloodBankTypeCode;

    @NotNull(message = "State ID is required")
    private Long stateId;

    @NotNull(message = "District ID is required")
    private Long districtId;

    @NotNull(message = "City ID is required")
    private Long cityId;

    private String addressLine;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private String operatingHoursNote;
}
