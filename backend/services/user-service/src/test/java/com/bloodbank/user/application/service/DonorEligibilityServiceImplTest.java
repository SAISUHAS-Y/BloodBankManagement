package com.bloodbank.user.application.service;

import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.user.application.dto.response.DonorEligibilityResponse;
import com.bloodbank.user.application.service.impl.DonorEligibilityServiceImpl;
import com.bloodbank.user.domain.entity.DonorProfile;
import com.bloodbank.user.domain.enums.DonorStatus;
import com.bloodbank.user.domain.repository.DonorProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DonorEligibilityServiceImplTest {

    @Mock
    private DonorProfileRepository donorProfileRepository;

    @InjectMocks
    private DonorEligibilityServiceImpl donorEligibilityService;

    private DonorProfile donor;

    @BeforeEach
    void setUp() {
        donor = new DonorProfile();
        donor.setId(1L);
        donor.setFullName("John Doe");
        donor.setGenderCode("MALE");
        donor.setDonorStatus(DonorStatus.ACTIVE);
    }

    @Test
    void calculateEligibility_DonorNotFound_ThrowsResourceNotFoundException() {
        when(donorProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> donorEligibilityService.calculateEligibility(99L));
    }

    @Test
    void calculateEligibility_BlacklistedDonor_ReturnsNotEligible() {
        donor.setDonorStatus(DonorStatus.BLACKLISTED);
        when(donorProfileRepository.findById(1L)).thenReturn(Optional.of(donor));

        DonorEligibilityResponse response = donorEligibilityService.calculateEligibility(1L);

        assertFalse(response.isEligible());
        assertEquals("BLACKLISTED", response.getDonorStatus());
        assertNull(response.getNextEligibleDate());
    }

    @Test
    void calculateEligibility_DeferredDonor_ReturnsNotEligible() {
        donor.setDonorStatus(DonorStatus.DEFERRED);
        donor.setDeferredUntilDate(LocalDate.now().plusDays(10));
        when(donorProfileRepository.findById(1L)).thenReturn(Optional.of(donor));

        DonorEligibilityResponse response = donorEligibilityService.calculateEligibility(1L);

        assertFalse(response.isEligible());
        assertEquals("DEFERRED", response.getDonorStatus());
        assertEquals(LocalDate.now().plusDays(10), response.getNextEligibleDate());
        assertEquals(10, response.getDaysRemaining());
    }

    @Test
    void calculateEligibility_NoPriorDonations_ReturnsEligible() {
        when(donorProfileRepository.findById(1L)).thenReturn(Optional.of(donor));

        DonorEligibilityResponse response = donorEligibilityService.calculateEligibility(1L);

        assertTrue(response.isEligible());
        assertEquals(0, response.getDaysRemaining());
    }

    @Test
    void calculateEligibility_RecentMaleDonation_ReturnsNotEligible() {
        donor.setLastDonationDate(LocalDate.now().minusDays(30));
        when(donorProfileRepository.findById(1L)).thenReturn(Optional.of(donor));

        DonorEligibilityResponse response = donorEligibilityService.calculateEligibility(1L);

        assertFalse(response.isEligible());
        assertEquals(60, response.getDaysRemaining());
    }

    @Test
    void calculateEligibility_PastDonationEligible_ReturnsEligible() {
        donor.setLastDonationDate(LocalDate.now().minusDays(100));
        when(donorProfileRepository.findById(1L)).thenReturn(Optional.of(donor));

        DonorEligibilityResponse response = donorEligibilityService.calculateEligibility(1L);

        assertTrue(response.isEligible());
        assertEquals(0, response.getDaysRemaining());
    }
}
