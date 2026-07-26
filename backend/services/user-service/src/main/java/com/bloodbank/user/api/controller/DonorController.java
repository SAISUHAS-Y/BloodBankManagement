package com.bloodbank.user.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.common.contracts.dto.RecordDonationRequest;
import com.bloodbank.user.application.dto.DonorBasicResponse;
import com.bloodbank.user.application.dto.DonorProfileRequest;
import com.bloodbank.user.application.dto.DonorProfileResponse;
import com.bloodbank.user.application.service.DonorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
@Slf4j
public class DonorController {

    private final DonorProfileService donorProfileService;

    @PostMapping
    @HasPermission("DONOR_CREATE")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> createDonor(
            @Valid @RequestBody DonorProfileRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        DonorProfileResponse response = donorProfileService.createDonor(request, username);
        return new ResponseEntity<>(
                ApiResponse.success("Donor profile created successfully", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @HasPermission("DONOR_MANAGE")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> updateDonor(
            @PathVariable Long id,
            @Valid @RequestBody DonorProfileRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        DonorProfileResponse response = donorProfileService.updateDonor(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Donor profile updated successfully", response));
    }

    // ==========================================
    // TWO-TIER ACCESS MODEL: NON-SENSITIVE READS
    // ==========================================

    @GetMapping("/{id}")
    @HasPermission("DONOR_VIEW")
    public ResponseEntity<ApiResponse<DonorBasicResponse>> getDonorBasicById(@PathVariable Long id) {
        DonorBasicResponse response = donorProfileService.getDonorBasicById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @HasPermission("DONOR_VIEW")
    public ResponseEntity<ApiResponse<Page<DonorBasicResponse>>> searchDonorsBasic(
            @RequestParam(required = false) Long bloodGroupId,
            @RequestParam(required = false) Long stateId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DonorBasicResponse> response = donorProfileService.searchDonorsBasic(
                bloodGroupId, stateId, districtId, cityId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // TWO-TIER ACCESS MODEL: SENSITIVE FULL READS
    // ==========================================

    @GetMapping("/{id}/sensitive")
    @HasPermission("DONOR_VIEW_SENSITIVE")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> getDonorSensitiveById(@PathVariable Long id) {
        DonorProfileResponse response = donorProfileService.getDonorById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/sensitive")
    @HasPermission("DONOR_VIEW_SENSITIVE")
    public ResponseEntity<ApiResponse<Page<DonorProfileResponse>>> searchDonorsSensitive(
            @RequestParam(required = false) Long bloodGroupId,
            @RequestParam(required = false) Long stateId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<DonorProfileResponse> response = donorProfileService.searchDonors(
                bloodGroupId, stateId, districtId, cityId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // DEFERRALS AND RECORD DONATION
    // ==========================================

    @PatchMapping("/{id}/defer")
    @HasPermission("DONOR_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deferOrBlacklistDonor(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam String reasonCode,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        donorProfileService.deferOrBlacklistDonor(id, status, reasonCode, username);
        return ResponseEntity.ok(ApiResponse.success("Donor status deferred/blacklisted successfully", null));
    }

    @PatchMapping("/{id}/record-donation")
    @HasPermission("DONOR_MANAGE")
    public ResponseEntity<ApiResponse<DonorProfileResponse>> recordDonation(
            @PathVariable Long id,
            @Valid @RequestBody RecordDonationRequest request) {
        
        log.info("REST S2S request to record donation for donor ID: {}", id);
        DonorProfileResponse response = donorProfileService.recordDonation(id, request);
        return ResponseEntity.ok(ApiResponse.success("Donation recorded on profile successfully", response));
    }
}
