package com.bloodbank.common.contracts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordDonationRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private LocalDate donationDate;
    private double unitsCollected;
    private Long donationId; // Used as the S2S idempotency key
}
