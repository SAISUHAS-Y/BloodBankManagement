package com.bloodbank.transaction.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.transaction.application.dto.BloodRequestDto;
import com.bloodbank.transaction.application.dto.BloodRequestResponse;
import com.bloodbank.transaction.application.dto.IssuanceRequest;
import com.bloodbank.transaction.application.dto.IssuanceResponse;
import com.bloodbank.transaction.application.service.BloodRequestService;
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
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class BloodRequestController {

    private final BloodRequestService bloodRequestService;

    @PostMapping("/blood-requests")
    @HasPermission("REQUEST_CREATE")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> createRequest(
            @Valid @RequestBody BloodRequestDto requestDto,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        BloodRequestResponse response = bloodRequestService.createRequest(requestDto, username);
        return new ResponseEntity<>(
                ApiResponse.success("Blood request created successfully in PENDING state", response),
                HttpStatus.CREATED
        );
    }

    @PatchMapping("/blood-requests/{id}/approve")
    @HasPermission("REQUEST_APPROVE")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> approveRequest(
            @PathVariable Long id,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        BloodRequestResponse response = bloodRequestService.approveRequest(id, username);
        return ResponseEntity.ok(ApiResponse.success("Blood request APPROVED successfully", response));
    }

    @PatchMapping("/blood-requests/{id}/reject")
    @HasPermission("REQUEST_APPROVE")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> rejectRequest(
            @PathVariable Long id,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        BloodRequestResponse response = bloodRequestService.rejectRequest(id, username);
        return ResponseEntity.ok(ApiResponse.success("Blood request REJECTED successfully", response));
    }

    @PatchMapping("/blood-requests/{id}/cancel")
    @HasPermission("REQUEST_CREATE")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> cancelRequest(
            @PathVariable Long id,
            Principal principal) {
        
        Long userId = 0L;
        if (principal != null) {
            try {
                userId = Long.parseLong(principal.getName());
            } catch (NumberFormatException e) {
                log.warn("Principal name is not numeric user ID: {}", principal.getName());
            }
        }

        BloodRequestResponse response = bloodRequestService.cancelRequest(id, userId, false);
        return ResponseEntity.ok(ApiResponse.success("Blood request CANCELLED successfully", response));
    }

    @PostMapping("/issuances")
    @HasPermission("REQUEST_FULFILL")
    public ResponseEntity<ApiResponse<IssuanceResponse>> issueBlood(
            @Valid @RequestBody IssuanceRequest request) {
        
        log.info("REST request to issue blood for request ID: {}", request.getBloodRequestId());
        IssuanceResponse response = bloodRequestService.issueBlood(request);
        return new ResponseEntity<>(
                ApiResponse.success("Blood units issued and request fulfilled successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/blood-requests/{id}")
    @HasPermission("REQUEST_VIEW")
    public ResponseEntity<ApiResponse<BloodRequestResponse>> getRequestById(@PathVariable Long id) {
        BloodRequestResponse response = bloodRequestService.getRequestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/blood-requests")
    @HasPermission("REQUEST_VIEW")
    public ResponseEntity<ApiResponse<Page<BloodRequestResponse>>> searchRequests(
            @RequestParam(required = false) Long hospitalId,
            @RequestParam(required = false) Long bloodGroupId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<BloodRequestResponse> response = bloodRequestService.searchRequests(
                hospitalId, bloodGroupId, status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/transactions/internal/hospitals/{hospitalId}/has-requests")
    public ResponseEntity<ApiResponse<Boolean>> hasHospitalRequests(@PathVariable Long hospitalId) {
        log.info("Internal S2S check: does hospital ID {} have requests?", hospitalId);
        boolean hasRequests = bloodRequestService.hasHospitalRequests(hospitalId);
        return ResponseEntity.ok(ApiResponse.success(hasRequests));
    }
}
