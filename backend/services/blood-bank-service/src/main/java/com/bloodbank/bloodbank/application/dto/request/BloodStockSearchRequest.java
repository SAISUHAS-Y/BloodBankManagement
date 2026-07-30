package com.bloodbank.bloodbank.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodStockSearchRequest {

    @NotBlank(message = "Blood group code is required")
    private String bloodGroupCode;

    private Long stateId;
    private Long districtId;
    
    @Builder.Default
    private double minUnits = 0.0;
}
