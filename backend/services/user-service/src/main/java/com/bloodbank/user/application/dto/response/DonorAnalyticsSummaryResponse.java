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
@Schema(description = "Donor analytics metrics and distribution summary")
public class DonorAnalyticsSummaryResponse {

    private long totalDonors;
    private long activeDonorsCount;
    private long deferredDonorsCount;
    private long blacklistedDonorsCount;
    private double activeToDeferredRatio;

    private long totalDonationsRecorded;
    private double averageDonationsPerDonor;
    private double retentionRatePercentage;

    private Map<Long, Long> donorsByBloodGroup;
    private Map<Long, Long> donorsByState;
}
