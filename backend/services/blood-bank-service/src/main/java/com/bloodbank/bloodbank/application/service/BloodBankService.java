package com.bloodbank.bloodbank.application.service;

import com.bloodbank.bloodbank.application.dto.request.BloodBankRequest;
import com.bloodbank.bloodbank.application.dto.response.BloodBankResponse;
import org.springframework.data.domain.Page;

public interface BloodBankService {
    BloodBankResponse createBloodBank(BloodBankRequest request, String createdBy);
    BloodBankResponse updateBloodBank(Long id, BloodBankRequest request, String updatedBy);
    BloodBankResponse getBloodBankById(Long id);
    Page<BloodBankResponse> searchBloodBanks(Long stateId, Long districtId, Long cityId, Boolean isActive, int page, int size);
}
