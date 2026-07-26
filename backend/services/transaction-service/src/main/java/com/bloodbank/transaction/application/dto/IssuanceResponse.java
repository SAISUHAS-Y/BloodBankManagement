package com.bloodbank.transaction.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssuanceResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bloodRequestId;
    private Long bloodBankId;
    private String bloodBankName;
    private double unitsIssued;
    private Long issuedBy;
    private Instant issuedAt;
    private String crossMatchReference;
}
