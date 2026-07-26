package com.bloodbank.hospital.application.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRequest {

    @NotBlank(message = "Hospital name is required")
    private String name;

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotBlank(message = "Hospital type code is required")
    private String hospitalTypeCode;

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

    @Valid
    private List<HospitalContactRequest> contacts;
}
