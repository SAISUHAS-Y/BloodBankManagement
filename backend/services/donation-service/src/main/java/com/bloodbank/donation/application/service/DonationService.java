package com.bloodbank.donation.application.service;

import com.bloodbank.donation.application.dto.DonationRecordRequest;
import com.bloodbank.donation.application.dto.DonationRecordResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface DonationService {
    DonationRecordResponse createDonation(DonationRecordRequest request, String createdBy);
    DonationRecordResponse completeDonation(Long id, Long collectedByStaffUserId);
    DonationRecordResponse getDonationById(Long id);
    Page<DonationRecordResponse> searchDonations(Long donorProfileId, Long bloodBankId, String status, int page, int size);
    List<DonationRecordResponse> getPendingStatsSyncDonations();
}
