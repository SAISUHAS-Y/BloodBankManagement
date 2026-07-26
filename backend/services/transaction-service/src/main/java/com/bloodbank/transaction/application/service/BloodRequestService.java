package com.bloodbank.transaction.application.service;

import com.bloodbank.transaction.application.dto.BloodRequestDto;
import com.bloodbank.transaction.application.dto.BloodRequestResponse;
import com.bloodbank.transaction.application.dto.IssuanceRequest;
import com.bloodbank.transaction.application.dto.IssuanceResponse;
import org.springframework.data.domain.Page;

public interface BloodRequestService {
    BloodRequestResponse createRequest(BloodRequestDto requestDto, String username);
    BloodRequestResponse approveRequest(Long id, String username);
    BloodRequestResponse rejectRequest(Long id, String username);
    BloodRequestResponse cancelRequest(Long id, Long userId, boolean isManager);
    IssuanceResponse issueBlood(IssuanceRequest request);
    BloodRequestResponse getRequestById(Long id);
    Page<BloodRequestResponse> searchRequests(Long hospitalId, Long bloodGroupId, String status, int page, int size);
    boolean hasHospitalRequests(Long hospitalId);
}
