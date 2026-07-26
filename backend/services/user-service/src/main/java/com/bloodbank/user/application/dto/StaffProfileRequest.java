package com.bloodbank.user.application.dto;

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
public class StaffProfileRequest {

    @NotNull(message = "Identity user ID is required")
    private Long identityUserId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Designation is required")
    private String designation;

    private Long bloodBankId;
    private Long hospitalId;

    @NotBlank(message = "Phone number is required")
    private String phone;
}
