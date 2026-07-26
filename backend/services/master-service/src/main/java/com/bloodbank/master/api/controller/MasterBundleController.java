package com.bloodbank.master.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.master.application.dto.MasterDataBundleResponse;
import com.bloodbank.master.application.service.LookupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/master/bundle")
@RequiredArgsConstructor
@Slf4j
public class MasterBundleController {

    private final LookupService lookupService;

    @GetMapping
    public ResponseEntity<ApiResponse<MasterDataBundleResponse>> getMasterDataBundle() {
        log.info("Aggregating master data bundle for frontend initialization");
        MasterDataBundleResponse bundle = lookupService.getMasterDataBundle();
        return ResponseEntity.ok(ApiResponse.success(bundle));
    }
}
