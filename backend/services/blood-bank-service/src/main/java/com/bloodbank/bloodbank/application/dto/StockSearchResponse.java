package com.bloodbank.bloodbank.application.dto;

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
public class StockSearchResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long bloodBankId;
    private String bloodBankName;
    private String stateName;
    private String districtName;
    private String cityName;
    private String addressLine;
    private String phone;
    private double unitsAvailable;
    private Instant lastUpdatedAt;
}
