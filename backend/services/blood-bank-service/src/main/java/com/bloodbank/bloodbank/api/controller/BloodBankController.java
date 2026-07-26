package com.bloodbank.bloodbank.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.bloodbank.application.dto.BloodBankRequest;
import com.bloodbank.bloodbank.application.dto.BloodBankResponse;
import com.bloodbank.bloodbank.application.service.BloodBankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/blood-banks")
@RequiredArgsConstructor
@Slf4j
public class BloodBankController {

    private final BloodBankService bloodBankService;

    @PostMapping
    @HasPermission("BLOOD_BANK_MANAGE")
    public ResponseEntity<ApiResponse<BloodBankResponse>> createBloodBank(
            @Valid @RequestBody BloodBankRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        BloodBankResponse response = bloodBankService.createBloodBank(request, username);
        return new ResponseEntity<>(
                ApiResponse.success("Blood bank facility registered successfully", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @HasPermission("BLOOD_BANK_MANAGE")
    public ResponseEntity<ApiResponse<BloodBankResponse>> updateBloodBank(
            @PathVariable Long id,
            @Valid @RequestBody BloodBankRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        BloodBankResponse response = bloodBankService.updateBloodBank(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Blood bank facility updated successfully", response));
    }

    @GetMapping("/{id}")
    @HasPermission("BLOOD_BANK_VIEW")
    public ResponseEntity<ApiResponse<BloodBankResponse>> getBloodBankById(@PathVariable Long id) {
        BloodBankResponse response = bloodBankService.getBloodBankById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @HasPermission("BLOOD_BANK_VIEW")
    public ResponseEntity<ApiResponse<Page<BloodBankResponse>>> searchBloodBanks(
            @RequestParam(required = false) Long stateId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<BloodBankResponse> response = bloodBankService.searchBloodBanks(
                stateId, districtId, cityId, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
