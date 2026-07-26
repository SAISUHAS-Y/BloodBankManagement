package com.bloodbank.donation.application.dto;

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
public class DonationRecordResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long donorProfileId;
    private String donorName;
    private Long bloodBankId;
    private String bloodBankName;
    private Long eligibilityCheckId;
    private Long bloodGroupId;
    private String bloodGroupLabel;
    private String componentTypeCode;
    private String componentTypeLabel;
    private double unitsCollected;
    private String donationStatus;
    private Long collectedBy;
    private LocalDate donationDate;
    private String notes;
}
