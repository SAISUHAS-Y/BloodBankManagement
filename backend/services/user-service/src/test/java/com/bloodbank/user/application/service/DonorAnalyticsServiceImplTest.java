package com.bloodbank.user.application.service;

import com.bloodbank.user.application.dto.response.DonorAnalyticsSummaryResponse;
import com.bloodbank.user.application.service.impl.DonorAnalyticsServiceImpl;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.enums.DonorStatus;
import com.bloodbank.user.domain.repository.DonorProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonorAnalyticsServiceImplTest {

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @InjectMocks
    private DonorAnalyticsServiceImpl donorAnalyticsService;

    @Test
    void getDonorAnalyticsSummary_EmptyList_ReturnsZeros() {
        when(donorProfileRepository.findAll()).thenReturn(List.of());

        DonorAnalyticsSummaryResponse response = donorAnalyticsService.getDonorAnalyticsSummary();

        assertEquals(0, response.getTotalDonors());
        assertEquals(0, response.getActiveDonorsCount());
    }

    @Test
    void getDonorAnalyticsSummary_PopulatedList_CalculatesMetricsCorrectly() {
        DonorProfile d1 = new DonorProfile();
        d1.setDonorStatus(DonorStatus.ACTIVE);
        d1.setBloodGroupId(1L);
        d1.setStateId(10L);
        d1.setTotalDonations(3);

        DonorProfile d2 = new DonorProfile();
        d2.setDonorStatus(DonorStatus.DEFERRED);
        d2.setBloodGroupId(1L);
        d2.setStateId(10L);
        d2.setTotalDonations(1);

        when(donorProfileRepository.findAll()).thenReturn(List.of(d1, d2));

        DonorAnalyticsSummaryResponse response = donorAnalyticsService.getDonorAnalyticsSummary();

        assertEquals(2, response.getTotalDonors());
        assertEquals(1, response.getActiveDonorsCount());
        assertEquals(1, response.getDeferredDonorsCount());
        assertEquals(4, response.getTotalDonationsRecorded());
        assertEquals(2.0, response.getAverageDonationsPerDonor());
        assertEquals(50.0, response.getRetentionRatePercentage());
    }
}
