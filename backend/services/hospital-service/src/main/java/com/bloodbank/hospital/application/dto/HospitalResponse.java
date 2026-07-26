package com.bloodbank.hospital.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String registrationNumber;
    
    private String hospitalTypeCode;
    private String hospitalTypeLabel;
    
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private Long cityId;
    private String cityName;
    
    private String addressLine;
    private String phone;
    private String email;
    private boolean active;
    private List<HospitalContactResponse> contacts;
}
