package com.bloodbank.user.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.common.contracts.dto.RecordDonationRequest;
import com.bloodbank.user.application.dto.request.DonorNoteRequest;
import com.bloodbank.user.application.dto.request.DonorProfileRequest;
import com.bloodbank.user.application.dto.request.DonorSearchRequest;
import com.bloodbank.user.application.dto.response.DonorAnalyticsSummaryResponse;
import com.bloodbank.user.application.dto.response.DonorBasicResponse;
import com.bloodbank.user.application.dto.response.DonorBulkImportResultResponse;
import com.bloodbank.user.application.dto.response.DonorEligibilityResponse;
import com.bloodbank.user.application.dto.response.DonorNoteResponse;
import com.bloodbank.user.application.dto.response.DonorProfileResponse;
import com.bloodbank.user.application.dto.response.ProfileAuditHistoryResponse;
import com.bloodbank.user.application.service.DonorAnalyticsService;
import com.bloodbank.user.application.service.DonorEligibilityService;
import com.bloodbank.user.application.service.DonorProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/donors")
@RequiredArgsConstructor
@Slf4j
public class DonorController {

    private final DonorProfileService donorProfileService;
    private final DonorEligibilityService donorEligibilityService;
    private final DonorAnalyticsService donorAnalyticsService;

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
            @Valid DonorSearchRequest request) {
        Page<DonorBasicResponse> response = donorProfileService.searchDonorsBasic(request);
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
            @Valid DonorSearchRequest request) {
        Page<DonorProfileResponse> response = donorProfileService.searchDonors(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // ELIGIBILITY & HISTORY
    // ==========================================

    @GetMapping("/{id}/eligibility")
    @HasPermission("DONOR_VIEW")
    public ResponseEntity<ApiResponse<DonorEligibilityResponse>> getDonorEligibility(@PathVariable Long id) {
        DonorEligibilityResponse response = donorEligibilityService.calculateEligibility(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/history")
    @HasPermission("DONOR_VIEW_SENSITIVE")
    public ResponseEntity<ApiResponse<List<ProfileAuditHistoryResponse>>> getDonorHistory(@PathVariable Long id) {
        List<ProfileAuditHistoryResponse> response = donorProfileService.getDonorHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // BULK IMPORT & EXPORT
    // ==========================================

    @PostMapping("/bulk-import")
    @HasPermission("DONOR_MANAGE")
    public ResponseEntity<ApiResponse<DonorBulkImportResultResponse>> bulkImportDonors(
            @RequestParam("file") MultipartFile file,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        DonorBulkImportResultResponse response = donorProfileService.bulkImportDonors(file, username);
        return ResponseEntity.ok(ApiResponse.success("Bulk donor import completed", response));
    }

    @GetMapping("/export")
    @HasPermission("DONOR_VIEW_SENSITIVE")
    public ResponseEntity<byte[]> exportDonors(@Valid DonorSearchRequest request) {
        byte[] csvData = donorProfileService.exportDonorsCsv(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=donors_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvData);
    }

    // ==========================================
    // DONOR NOTES
    // ==========================================

    @PostMapping("/{id}/notes")
    @HasPermission("DONOR_MANAGE")
    public ResponseEntity<ApiResponse<DonorNoteResponse>> addDonorNote(
            @PathVariable Long id,
            @Valid @RequestBody DonorNoteRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        DonorNoteResponse response = donorProfileService.addDonorNote(id, request, username);
        return new ResponseEntity<>(ApiResponse.success("Case note added successfully", response), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/notes")
    @HasPermission("DONOR_VIEW")
    public ResponseEntity<ApiResponse<List<DonorNoteResponse>>> getDonorNotes(@PathVariable Long id) {
        List<DonorNoteResponse> response = donorProfileService.getDonorNotes(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // ANALYTICS
    // ==========================================

    @GetMapping("/analytics/summary")
    @HasPermission("DONOR_MANAGE")
    public ResponseEntity<ApiResponse<DonorAnalyticsSummaryResponse>> getDonorAnalyticsSummary() {
        DonorAnalyticsSummaryResponse response = donorAnalyticsService.getDonorAnalyticsSummary();
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
