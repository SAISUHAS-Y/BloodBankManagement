package com.bloodbank.user.application.service.impl;

import com.bloodbank.user.application.dto.response.DonorAnalyticsSummaryResponse;
import com.bloodbank.user.application.service.DonorAnalyticsService;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.enums.DonorStatus;
import com.bloodbank.user.domain.repository.DonorProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonorAnalyticsServiceImpl implements DonorAnalyticsService {

    private final DonorProfileRepository donorProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public DonorAnalyticsSummaryResponse getDonorAnalyticsSummary() {
        log.info("Computing donor analytics summary");
        List<DonorProfile> donors = donorProfileRepository.findAll();

        long totalDonors = donors.size();
        if (totalDonors == 0) {
            return DonorAnalyticsSummaryResponse.builder()
                    .totalDonors(0)
                    .activeDonorsCount(0)
                    .deferredDonorsCount(0)
                    .blacklistedDonorsCount(0)
                    .activeToDeferredRatio(0.0)
                    .totalDonationsRecorded(0)
                    .averageDonationsPerDonor(0.0)
                    .retentionRatePercentage(0.0)
                    .donorsByBloodGroup(Map.of())
                    .donorsByState(Map.of())
                    .build();
        }

        long activeCount = donors.stream().filter(d -> d.getDonorStatus() == DonorStatus.ACTIVE).count();
        long deferredCount = donors.stream().filter(d -> d.getDonorStatus() == DonorStatus.DEFERRED).count();
        long blacklistedCount = donors.stream().filter(d -> d.getDonorStatus() == DonorStatus.BLACKLISTED).count();

        double activeToDeferredRatio = deferredCount > 0 ? (double) activeCount / deferredCount : (double) activeCount;
        long totalDonations = donors.stream().mapToLong(DonorProfile::getTotalDonations).sum();
        double avgDonations = (double) totalDonations / totalDonors;

        long repeatDonors = donors.stream().filter(d -> d.getTotalDonations() > 1).count();
        double retentionRate = ((double) repeatDonors / totalDonors) * 100.0;

        Map<Long, Long> byBloodGroup = donors.stream()
                .filter(d -> d.getBloodGroupId() != null)
                .collect(Collectors.groupingBy(DonorProfile::getBloodGroupId, Collectors.counting()));

        Map<Long, Long> byState = donors.stream()
                .filter(d -> d.getStateId() != null)
                .collect(Collectors.groupingBy(DonorProfile::getStateId, Collectors.counting()));

        return DonorAnalyticsSummaryResponse.builder()
                .totalDonors(totalDonors)
                .activeDonorsCount(activeCount)
                .deferredDonorsCount(deferredCount)
                .blacklistedDonorsCount(blacklistedCount)
                .activeToDeferredRatio(Math.round(activeToDeferredRatio * 100.0) / 100.0)
                .totalDonationsRecorded(totalDonations)
                .averageDonationsPerDonor(Math.round(avgDonations * 100.0) / 100.0)
                .retentionRatePercentage(Math.round(retentionRate * 100.0) / 100.0)
                .donorsByBloodGroup(byBloodGroup)
                .donorsByState(byState)
                .build();
    }
}
