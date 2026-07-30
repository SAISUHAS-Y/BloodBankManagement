package com.bloodbank.user.application.service;

import com.bloodbank.user.application.dto.request.DonorNoteRequest;
import com.bloodbank.user.application.dto.request.DonorProfileRequest;
import com.bloodbank.user.application.dto.request.DonorSearchRequest;
import com.bloodbank.user.application.dto.response.DonorBasicResponse;
import com.bloodbank.user.application.dto.response.DonorBulkImportResultResponse;
import com.bloodbank.user.application.dto.response.DonorNoteResponse;
import com.bloodbank.user.application.dto.response.DonorProfileResponse;
import com.bloodbank.user.application.dto.response.ProfileAuditHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DonorProfileService {
    DonorProfileResponse createDonor(DonorProfileRequest request, String createdBy);
    DonorProfileResponse updateDonor(Long id, DonorProfileRequest request, String updatedBy);
    DonorProfileResponse getDonorById(Long id);
    DonorBasicResponse getDonorBasicById(Long id);
    Page<DonorProfileResponse> searchDonors(Long bloodGroupId, Long stateId, Long districtId, Long cityId, String status, int page, int size);
    Page<DonorBasicResponse> searchDonorsBasic(Long bloodGroupId, Long stateId, Long districtId, Long cityId, String status, int page, int size);
    Page<DonorProfileResponse> searchDonors(DonorSearchRequest request);
    Page<DonorBasicResponse> searchDonorsBasic(DonorSearchRequest request);
    void deferOrBlacklistDonor(Long id, String status, String reasonCode, String updatedBy);
    DonorProfileResponse recordDonation(Long id, com.bloodbank.common.contracts.dto.RecordDonationRequest request);

    List<ProfileAuditHistoryResponse> getDonorHistory(Long donorId);
    DonorNoteResponse addDonorNote(Long donorId, DonorNoteRequest request, String authorUsername);
    List<DonorNoteResponse> getDonorNotes(Long donorId);
    DonorBulkImportResultResponse bulkImportDonors(MultipartFile file, String createdBy);
    byte[] exportDonorsCsv(DonorSearchRequest searchRequest);
}
