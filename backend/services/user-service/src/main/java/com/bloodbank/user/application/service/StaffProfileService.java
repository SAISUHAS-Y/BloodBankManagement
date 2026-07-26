package com.bloodbank.user.application.service;

import com.bloodbank.user.application.dto.StaffProfileRequest;
import com.bloodbank.user.application.dto.StaffProfileResponse;

public interface StaffProfileService {
    StaffProfileResponse createStaff(StaffProfileRequest request, String createdBy);
    StaffProfileResponse updateStaff(Long id, StaffProfileRequest request, String updatedBy);
    StaffProfileResponse getStaffById(Long id);
    StaffProfileResponse getStaffByIdentityUserId(Long identityUserId);
}
