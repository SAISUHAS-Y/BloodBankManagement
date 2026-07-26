package com.bloodbank.bloodbank.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BloodBankResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String licenseNumber;
    
    private String bloodBankTypeCode;
    private String bloodBankTypeLabel;
    
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private Long cityId;
    private String cityName;
    
    private String addressLine;
    private String phone;
    private String email;
    private String operatingHoursNote;
    private boolean active;
}
