package com.bloodbank.user.application.service;

import com.bloodbank.user.application.dto.response.DonorEligibilityResponse;

public interface DonorEligibilityService {
    DonorEligibilityResponse calculateEligibility(Long donorId);
}
