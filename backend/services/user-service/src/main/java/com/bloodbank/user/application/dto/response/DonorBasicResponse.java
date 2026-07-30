package com.bloodbank.user.application.dto.response;

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
public class DonorBasicResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String fullName;
    private String genderCode;
    private String genderLabel;
    private Long bloodGroupId;
    private String bloodGroupLabel;
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private Long cityId;
    private String cityName;
    private String donorStatus;
    private LocalDate lastDonationDate;
    private int totalDonations;
}
