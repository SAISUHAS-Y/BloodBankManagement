package com.bloodbank.bloodbank.application.dto.response;

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
public class BloodStockResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long bloodBankId;
    private String bloodBankName;
    
    private Long bloodGroupId;
    private String bloodGroupLabel;
    
    private String componentTypeCode;
    private String componentTypeLabel;
    
    private double unitsAvailable;
    private Instant lastUpdatedAt;
}
