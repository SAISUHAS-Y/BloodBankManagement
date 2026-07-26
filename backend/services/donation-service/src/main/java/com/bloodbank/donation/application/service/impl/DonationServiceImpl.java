package com.bloodbank.donation.application.service.impl;

import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.contracts.client.BloodBankServiceClient;
import com.bloodbank.common.contracts.dto.BloodStockAdjustRequest;
import com.bloodbank.donation.application.dto.DonationRecordRequest;
import com.bloodbank.donation.application.dto.DonationRecordResponse;
import com.bloodbank.donation.application.event.DonationEventPublisher;
import com.bloodbank.donation.application.service.DonationService;
import com.bloodbank.donation.domain.entity.DonationRecord;
import com.bloodbank.donation.domain.entity.EligibilityCheck;
import com.bloodbank.donation.domain.enums.DonationStatus;
import com.bloodbank.donation.domain.repository.DonationRecordRepository;
import com.bloodbank.donation.domain.repository.EligibilityCheckRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DonationServiceImpl implements DonationService {

    private final DonationRecordRepository donationRecordRepository;
    private final EligibilityCheckRepository eligibilityCheckRepository;
    private final BloodBankServiceClient bloodBankServiceClient;
    private final MasterDataResolver dataResolver;
    private final DonationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DonationRecordResponse createDonation(DonationRecordRequest request, String createdBy) {
        log.info("Creating donation record for donor ID: {}", request.getDonorProfileId());

        EligibilityCheck check = eligibilityCheckRepository.findById(request.getEligibilityCheckId())
                .orElseThrow(() -> new ResourceNotFoundException("Eligibility check not found with ID: " 
                        + request.getEligibilityCheckId()));

        if (!check.isEligible()) {
            throw new InvalidInputException("Cannot register donation: Donor is ineligible due to screening failure.");
        }

        if (!check.getDonorProfileId().equals(request.getDonorProfileId())) {
            throw new InvalidInputException("Eligibility check ID does not belong to the requesting donor profile.");
        }

        DonationRecord record = new DonationRecord();
        record.setDonorProfileId(request.getDonorProfileId());
        record.setBloodBankId(request.getBloodBankId());
        record.setEligibilityCheck(check);
        record.setBloodGroupId(request.getBloodGroupId());
        record.setComponentTypeCode(request.getComponentTypeCode());
        record.setUnitsCollected(request.getUnitsCollected());
        record.setDonationStatus(DonationStatus.IN_PROGRESS);
        record.setDonationDate(request.getDonationDate());
        record.setNotes(request.getNotes());
        record.setCreatedBy(createdBy);
        record.setUpdatedBy(createdBy);

        DonationRecord saved = donationRecordRepository.save(record);
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DonationRecordResponse completeDonation(Long id, Long collectedByUserId) {
        log.info("Completing donation record ID: {} by staff ID: {}", id, collectedByUserId);

        DonationRecord donation = donationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation record not found with ID: " + id));

        if (donation.getDonationStatus() == DonationStatus.COMPLETED) {
            log.info("Donation ID: {} is already in COMPLETED status. Returning response.", id);
            return mapToResponse(donation);
        }

        if (donation.getDonationStatus() != DonationStatus.IN_PROGRESS && donation.getDonationStatus() != DonationStatus.SCHEDULED) {
            throw new InvalidInputException("Donation cannot be completed from current state: " + donation.getDonationStatus());
        }

        // STEP 1: MUST-SUCCEED Synchronous Feign Call for Immediate Stock Inventory Increment
        String stockIdempotencyKey = "DONATION-INC-" + donation.getId();
        BloodStockAdjustRequest stockReq = BloodStockAdjustRequest.builder()
                .bloodBankId(donation.getBloodBankId())
                .bloodGroupId(donation.getBloodGroupId())
                .componentTypeCode(donation.getComponentTypeCode())
                .units(donation.getUnitsCollected())
                .reason("Intake from Donation ID " + donation.getId())
                .referenceId(donation.getId())
                .idempotencyKey(stockIdempotencyKey)
                .build();
        
        try {
            bloodBankServiceClient.incrementStock(stockReq);
        } catch (Exception e) {
            log.error("Failed S2S stock increment call for Donation ID: {}", donation.getId(), e);
            throw new RuntimeException("Could not update blood stock inventory. Donation collection aborted.", e);
        }

        // STEP 2: Mark Donation COMPLETED and stage ASYNCHRONOUS OUTBOX EVENT for Donor Stats
        donation.setDonationStatus(DonationStatus.COMPLETED);
        donation.setCollectedBy(collectedByUserId);
        donation.setUpdatedBy("STAFF_" + collectedByUserId);
        DonationRecord saved = donationRecordRepository.save(donation);

        String traceId = UUID.randomUUID().toString();

        // Stage Outbox Event for User Service (Asynchronous Donor Stats Update)
        eventPublisher.publishDonationStatsUpdateRequired(
                saved.getId(),
                saved.getDonorProfileId(),
                saved.getDonationDate(),
                saved.getUnitsCollected(),
                traceId
        );

        // Stage Outbox Event for Notification/Broader System Events
        String bloodGroupCode = dataResolver.getBloodGroupCode(saved.getBloodGroupId());
        eventPublisher.publishDonationCompleted(
                saved.getId(),
                saved.getDonorProfileId(),
                saved.getBloodBankId(),
                bloodGroupCode,
                saved.getComponentTypeCode(),
                saved.getUnitsCollected(),
                traceId
        );

        log.info("Donation ID {} completed cleanly. Outbox events staged for async donor stats update.", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DonationRecordResponse getDonationById(Long id) {
        log.info("Fetching donation record ID: {}", id);
        DonationRecord donation = donationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation record not found with ID: " + id));
        return mapToResponse(donation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DonationRecordResponse> searchDonations(Long donorProfileId, Long bloodBankId, String status, int page, int size) {
        log.info("Searching donations: donorProfileId={} bloodBankId={} status={}", donorProfileId, bloodBankId, status);

        DonationStatus donationStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                donationStatus = DonationStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new InvalidInputException("Invalid donation status filter: " + status);
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("donationDate").descending());
        Page<DonationRecord> donationPage = donationRecordRepository.searchDonations(donorProfileId, bloodBankId, donationStatus, pageable);

        return donationPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationRecordResponse> getPendingStatsSyncDonations() {
        log.info("Operator audit query: checking for stuck completed donations (> 5 minutes old)");
        Instant threshold = Instant.now().minusSeconds(300); // 5 minutes ago
        List<DonationRecord> stuckDonations = donationRecordRepository.findByDonationStatusAndUpdatedAtBefore(DonationStatus.COMPLETED, threshold);
        return stuckDonations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DonationRecordResponse mapToResponse(DonationRecord donation) {
        return DonationRecordResponse.builder()
                .id(donation.getId())
                .donorProfileId(donation.getDonorProfileId())
                .donorName(dataResolver.getDonorName(donation.getDonorProfileId()))
                .bloodBankId(donation.getBloodBankId())
                .bloodBankName(dataResolver.getBloodBankName(donation.getBloodBankId()))
                .eligibilityCheckId(donation.getEligibilityCheck().getId())
                .bloodGroupId(donation.getBloodGroupId())
                .bloodGroupLabel(dataResolver.getBloodGroupLabel(donation.getBloodGroupId()))
                .componentTypeCode(donation.getComponentTypeCode())
                .componentTypeLabel(dataResolver.getComponentTypeLabel(donation.getComponentTypeCode()))
                .unitsCollected(donation.getUnitsCollected())
                .donationStatus(donation.getDonationStatus().name())
                .collectedBy(donation.getCollectedBy())
                .donationDate(donation.getDonationDate())
                .notes(donation.getNotes())
                .build();
    }
}
