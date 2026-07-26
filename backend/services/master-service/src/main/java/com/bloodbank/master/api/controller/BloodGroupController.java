package com.bloodbank.master.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.contracts.dto.BloodGroupContractResponse;
import com.bloodbank.master.application.dto.BloodGroupResponse;
import com.bloodbank.master.application.service.BloodGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/blood-groups")
@RequiredArgsConstructor
@Slf4j
public class BloodGroupController {

    private final BloodGroupService bloodGroupService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BloodGroupResponse>>> getAllBloodGroups() {
        List<BloodGroupResponse> groups = bloodGroupService.getAllBloodGroups();
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @GetMapping("/{code}")
    public ResponseEntity<ApiResponse<BloodGroupContractResponse>> getBloodGroupByCode(
            @PathVariable String code) {
        
        BloodGroupResponse bg = bloodGroupService.getBloodGroupByCode(code);
        BloodGroupContractResponse response = BloodGroupContractResponse.builder()
                .id(bg.getId())
                .code(bg.getCode())
                .displayName(bg.getDisplayName())
                .universalDonor(bg.isUniversalDonor())
                .universalRecipient(bg.isUniversalRecipient())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
