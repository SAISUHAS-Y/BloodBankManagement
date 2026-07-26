package com.bloodbank.donation.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long donorProfileId;
    private Instant checkedAt;
    private double hemoglobinLevel;
    private double weightKg;
    private double systolicBp;
    private double diastolicBp;
    private boolean eligible;
    private String deferralReasonCode;
    private LocalDate deferredUntilDate;
    private Long checkedBy;
}
