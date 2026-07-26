package com.bloodbank.donation.application.service;

import com.bloodbank.donation.application.dto.EligibilityRequest;
import com.bloodbank.donation.application.dto.EligibilityResponse;

public interface EligibilityService {
    EligibilityResponse conductScreening(EligibilityRequest request, String componentTypeCode);
}
