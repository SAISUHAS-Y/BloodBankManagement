package com.bloodbank.hospital.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.hospital.application.dto.HospitalRequest;
import com.bloodbank.hospital.application.dto.HospitalResponse;
import com.bloodbank.hospital.application.service.HospitalService;
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
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
@Slf4j
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    @HasPermission("HOSPITAL_MANAGE")
    public ResponseEntity<ApiResponse<HospitalResponse>> createHospital(
            @Valid @RequestBody HospitalRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        HospitalResponse response = hospitalService.createHospital(request, username);
        return new ResponseEntity<>(
                ApiResponse.success("Hospital created successfully", response),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{id}")
    @HasPermission("HOSPITAL_MANAGE")
    public ResponseEntity<ApiResponse<HospitalResponse>> updateHospital(
            @PathVariable Long id,
            @Valid @RequestBody HospitalRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        HospitalResponse response = hospitalService.updateHospital(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Hospital updated successfully", response));
    }

    @GetMapping("/{id}")
    @HasPermission("HOSPITAL_VIEW")
    public ResponseEntity<ApiResponse<HospitalResponse>> getHospitalById(@PathVariable Long id) {
        HospitalResponse response = hospitalService.getHospitalById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @HasPermission("HOSPITAL_VIEW")
    public ResponseEntity<ApiResponse<Page<HospitalResponse>>> searchHospitals(
            @RequestParam(required = false) Long stateId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(required = false) Long cityId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<HospitalResponse> response = hospitalService.searchHospitals(
                stateId, districtId, cityId, isActive, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    @HasPermission("HOSPITAL_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deactivateOrDeleteHospital(
            @PathVariable Long id,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "SYSTEM";
        hospitalService.deactivateOrDeleteHospital(id, username);
        return ResponseEntity.ok(ApiResponse.success("Hospital processed (deactivated if blood requests exist, otherwise soft-deleted)", null));
    }
}
