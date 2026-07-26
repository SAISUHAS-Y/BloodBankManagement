package com.bloodbank.user.application.service;

import com.bloodbank.user.application.dto.DonorBasicResponse;
import com.bloodbank.user.application.dto.DonorProfileRequest;
import com.bloodbank.user.application.dto.DonorProfileResponse;
import org.springframework.data.domain.Page;

public interface DonorProfileService {
    DonorProfileResponse createDonor(DonorProfileRequest request, String createdBy);
    DonorProfileResponse updateDonor(Long id, DonorProfileRequest request, String updatedBy);
    DonorProfileResponse getDonorById(Long id);
    DonorBasicResponse getDonorBasicById(Long id);
    Page<DonorProfileResponse> searchDonors(Long bloodGroupId, Long stateId, Long districtId, Long cityId, String status, int page, int size);
    Page<DonorBasicResponse> searchDonorsBasic(Long bloodGroupId, Long stateId, Long districtId, Long cityId, String status, int page, int size);
    void deferOrBlacklistDonor(Long id, String status, String reasonCode, String updatedBy);
    DonorProfileResponse recordDonation(Long id, com.bloodbank.common.contracts.dto.RecordDonationRequest request);
}
