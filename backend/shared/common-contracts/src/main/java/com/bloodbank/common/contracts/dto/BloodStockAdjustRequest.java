package com.bloodbank.common.contracts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodStockAdjustRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long bloodBankId;
    private Long bloodGroupId;
    private String componentTypeCode;
    private double units;
    private String reason;
    private Long referenceId; // donationId or issuanceId
    private String idempotencyKey;
}
