package com.bloodbank.user.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.user.application.dto.request.StaffProfileRequest;
import com.bloodbank.user.application.dto.request.StaffSearchRequest;
import com.bloodbank.user.application.dto.request.StaffStatusUpdateRequest;
import com.bloodbank.user.application.dto.request.StaffTransferRequest;
import com.bloodbank.user.application.dto.response.ProfileAuditHistoryResponse;
import com.bloodbank.user.application.dto.response.StaffHeadcountAnalyticsResponse;
import com.bloodbank.user.application.dto.response.StaffProfileResponse;
import com.bloodbank.user.application.service.StaffProfileService;
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
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
@Slf4j
public class StaffController {

    private final StaffProfileService staffProfileService;

    @PostMapping
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> createStaff(
            @Valid @RequestBody StaffProfileRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        StaffProfileResponse response = staffProfileService.createStaff(request, username);
        return new ResponseEntity<>(
                ApiResponse.success("Staff profile created successfully", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> updateStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffProfileRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        StaffProfileResponse response = staffProfileService.updateStaff(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Staff profile updated successfully", response));
    }

    @GetMapping("/{id}")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> getStaffById(@PathVariable Long id) {
        StaffProfileResponse response = staffProfileService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{identityUserId}")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> getStaffByIdentityUserId(@PathVariable Long identityUserId) {
        StaffProfileResponse response = staffProfileService.getStaffByIdentityUserId(identityUserId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // SPECIFICATION SEARCH, STATUS & TRANSFER
    // ==========================================

    @GetMapping
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<Page<StaffProfileResponse>>> searchStaff(
            @Valid StaffSearchRequest request) {
        Page<StaffProfileResponse> response = staffProfileService.searchStaff(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/status")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> updateStaffStatus(
            @PathVariable Long id,
            @Valid @RequestBody StaffStatusUpdateRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        StaffProfileResponse response = staffProfileService.updateStaffStatus(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Staff status updated successfully", response));
    }

    @PatchMapping("/{id}/transfer")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<StaffProfileResponse>> transferStaff(
            @PathVariable Long id,
            @Valid @RequestBody StaffTransferRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        StaffProfileResponse response = staffProfileService.transferStaff(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Staff transferred successfully", response));
    }

    @GetMapping("/{id}/reports")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<List<StaffProfileResponse>>> getDirectReports(@PathVariable Long id) {
        List<StaffProfileResponse> response = staffProfileService.getDirectReports(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}/history")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<List<ProfileAuditHistoryResponse>>> getStaffHistory(@PathVariable Long id) {
        List<ProfileAuditHistoryResponse> response = staffProfileService.getStaffHistory(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================
    // ANALYTICS
    // ==========================================

    @GetMapping("/analytics/headcount")
    @HasPermission("STAFF_MANAGE")
    public ResponseEntity<ApiResponse<StaffHeadcountAnalyticsResponse>> getStaffHeadcountAnalytics() {
        StaffHeadcountAnalyticsResponse response = staffProfileService.getStaffHeadcountAnalytics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
