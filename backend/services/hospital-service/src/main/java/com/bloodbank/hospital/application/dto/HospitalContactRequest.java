package com.bloodbank.hospital.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalContactRequest {

    @NotBlank(message = "Contact name is required")
    private String name;

    @NotBlank(message = "Designation is required")
    private String designation;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String email;
}
