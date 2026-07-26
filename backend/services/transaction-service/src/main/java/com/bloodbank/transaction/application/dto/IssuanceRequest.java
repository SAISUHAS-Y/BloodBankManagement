package com.bloodbank.transaction.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuanceRequest {

    @NotNull(message = "Blood request ID is required")
    private Long bloodRequestId;

    @NotNull(message = "Blood bank ID is required")
    private Long bloodBankId;

    private double unitsIssued;

    @NotNull(message = "Issuer staff ID is required")
    private Long issuedBy;

    private String crossMatchReference;
}
