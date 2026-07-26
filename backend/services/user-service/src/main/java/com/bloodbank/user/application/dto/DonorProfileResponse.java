package com.bloodbank.user.application.dto;

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
public class DonorProfileResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long identityUserId;
    private String fullName;
    private LocalDate dob;
    private String genderCode;
    private String genderLabel;
    
    private Long bloodGroupId;
    private String bloodGroupLabel;
    
    private String phone;
    private String email;
    private String addressLine;
    
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private Long cityId;
    private String cityName;
    
    private String idType;
    private String idNumber;
    private String donorStatus;
    private LocalDate lastDonationDate;
    private LocalDate deferredUntilDate;
    private int totalDonations;
}
