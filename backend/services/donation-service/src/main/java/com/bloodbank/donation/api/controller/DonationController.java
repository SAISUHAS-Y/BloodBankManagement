package com.bloodbank.donation.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.donation.application.dto.DonationRecordRequest;
import com.bloodbank.donation.application.dto.DonationRecordResponse;
import com.bloodbank.donation.application.dto.EligibilityRequest;
import com.bloodbank.donation.application.dto.EligibilityResponse;
import com.bloodbank.donation.application.service.DonationService;
import com.bloodbank.donation.application.service.EligibilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/donations")
@RequiredArgsConstructor
@Slf4j
public class DonationController {

    private final EligibilityService eligibilityService;
    private final DonationService donationService;

    @PostMapping("/eligibility")
    @HasPermission("DONATION_CREATE")
    public ResponseEntity<ApiResponse<EligibilityResponse>> conductScreening(
            @Valid @RequestBody EligibilityRequest request,
            @RequestParam String componentTypeCode) {
        
        EligibilityResponse response = eligibilityService.conductScreening(request, componentTypeCode);
        return new ResponseEntity<>(
                ApiResponse.success("Pre-donation screening conducted successfully", response),
                HttpStatus.CREATED
        );
    }

    @PostMapping
    @HasPermission("DONATION_CREATE")
    public ResponseEntity<ApiResponse<DonationRecordResponse>> createDonation(
            @Valid @RequestBody DonationRecordRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        DonationRecordResponse response = donationService.createDonation(request, username);
        return new ResponseEntity<>(
                ApiResponse.success("Donation record created successfully in IN_PROGRESS state", response),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/{id}/complete")
    @HasPermission("DONATION_APPROVE")
    public ResponseEntity<ApiResponse<DonationRecordResponse>> completeDonation(
            @PathVariable Long id,
            Principal principal) {
        
        Long staffUserId = 0L;
        if (principal != null) {
            try {
                staffUserId = Long.parseLong(principal.getName());
            } catch (NumberFormatException e) {
                log.warn("Principal name is not a numeric user ID: {}", principal.getName());
            }
        }

        DonationRecordResponse response = donationService.completeDonation(id, staffUserId);
        return ResponseEntity.ok(ApiResponse.success("Donation completed and stock incremented successfully", response));
    }

    @GetMapping("/{id}")
    @HasPermission("DONATION_VIEW")
    public ResponseEntity<ApiResponse<DonationRecordResponse>> getDonationById(@PathVariable Long id) {
        DonationRecordResponse response = donationService.getDonationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @HasPermission("DONATION_VIEW")
    public ResponseEntity<ApiResponse<Page<DonationRecordResponse>>> searchDonations(
            @RequestParam(required = false) Long donorProfileId,
            @RequestParam(required = false) Long bloodBankId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DonationRecordResponse> response = donationService.searchDonations(
                donorProfileId, bloodBankId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/admin/pending-stats-sync")
    @HasPermission("DONATION_APPROVE")
    public ResponseEntity<ApiResponse<java.util.List<DonationRecordResponse>>> getPendingStatsSyncDonations() {
        log.info("Operator audit request for pending donor stats sync donations");
        java.util.List<DonationRecordResponse> response = donationService.getPendingStatsSyncDonations();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
