package com.bloodbank.user.application.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Staff headcount breakdown by status, designation, and facility")
public class StaffHeadcountAnalyticsResponse {

    private long totalStaff;
    private Map<String, Long> headcountByStatus;
    private Map<String, Long> headcountByDesignation;
    private Map<Long, Long> headcountByBloodBank;
    private Map<Long, Long> headcountByHospital;
}
