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
public class BloodRequestResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long hospitalId;
    private String hospitalName;
    private Long requestedBy;
    private Long bloodGroupId;
    private String bloodGroupLabel;
    private String componentTypeCode;
    private String componentTypeLabel;
    private double unitsRequested;
    private String urgency;
    private String requestStatus;
    private String patientName;
    private int patientAge;
    private String clinicalReason;
    private Instant requestedAt;
}
