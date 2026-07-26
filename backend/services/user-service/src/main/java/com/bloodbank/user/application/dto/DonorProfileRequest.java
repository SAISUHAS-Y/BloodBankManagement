package com.bloodbank.user.application.dto;

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
public class DonorProfileRequest {

    private Long identityUserId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    private LocalDate dob;

    @NotBlank(message = "Gender code is required")
    private String genderCode;

    @NotNull(message = "Blood group ID is required")
    private Long bloodGroupId;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
    private String addressLine;

    @NotNull(message = "State ID is required")
    private Long stateId;

    @NotNull(message = "District ID is required")
    private Long districtId;

    @NotNull(message = "City ID is required")
    private Long cityId;

    private String idType;
    private String idNumber;
}
