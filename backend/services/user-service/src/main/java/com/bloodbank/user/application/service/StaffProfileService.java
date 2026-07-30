package com.bloodbank.user.application.service;

import com.bloodbank.user.application.dto.request.StaffProfileRequest;
import com.bloodbank.user.application.dto.request.StaffSearchRequest;
import com.bloodbank.user.application.dto.request.StaffStatusUpdateRequest;
import com.bloodbank.user.application.dto.request.StaffTransferRequest;
import com.bloodbank.user.application.dto.response.ProfileAuditHistoryResponse;
import com.bloodbank.user.application.dto.response.StaffHeadcountAnalyticsResponse;
import com.bloodbank.user.application.dto.response.StaffProfileResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface StaffProfileService {
    StaffProfileResponse createStaff(StaffProfileRequest request, String createdBy);
    StaffProfileResponse updateStaff(Long id, StaffProfileRequest request, String updatedBy);
    StaffProfileResponse getStaffById(Long id);
    StaffProfileResponse getStaffByIdentityUserId(Long identityUserId);

    StaffProfileResponse updateStaffStatus(Long id, StaffStatusUpdateRequest request, String updatedBy);
    StaffProfileResponse transferStaff(Long id, StaffTransferRequest request, String updatedBy);
    Page<StaffProfileResponse> searchStaff(StaffSearchRequest request);
    List<StaffProfileResponse> getDirectReports(Long staffId);
    List<ProfileAuditHistoryResponse> getStaffHistory(Long staffId);
    StaffHeadcountAnalyticsResponse getStaffHeadcountAnalytics();
}
