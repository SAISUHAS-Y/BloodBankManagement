package com.bloodbank.bloodbank.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.common.contracts.dto.BloodStockAdjustRequest;
import com.bloodbank.bloodbank.application.dto.request.BloodStockSearchRequest;
import com.bloodbank.bloodbank.application.dto.request.StockAdjustmentRequest;
import com.bloodbank.bloodbank.application.dto.response.BloodStockResponse;
import com.bloodbank.bloodbank.application.dto.response.StockSearchResponse;
import com.bloodbank.bloodbank.application.service.BloodStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/blood-stocks")
@RequiredArgsConstructor
@Slf4j
public class BloodStockController {

    private final BloodStockService bloodStockService;

    @PatchMapping("/{id}/adjust")
    @HasPermission("BLOOD_STOCK_MANAGE")
    public ResponseEntity<ApiResponse<BloodStockResponse>> adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody StockAdjustmentRequest request,
            Principal principal) {
        
        Long adjustedByUserId = 0L;
        if (principal != null) {
            try {
                adjustedByUserId = Long.parseLong(principal.getName());
            } catch (NumberFormatException e) {
                log.warn("Principal name is not a numeric user ID: {}", principal.getName());
            }
        }

        BloodStockResponse response = bloodStockService.adjustStock(
                id, request.getUnits(), request.getReason(), adjustedByUserId);
        return ResponseEntity.ok(ApiResponse.success("Blood stock adjusted successfully", response));
    }

    @GetMapping("/blood-bank/{bloodBankId}")
    @HasPermission("BLOOD_STOCK_VIEW")
    public ResponseEntity<ApiResponse<List<BloodStockResponse>>> getStockByBloodBank(@PathVariable Long bloodBankId) {
        List<BloodStockResponse> response = bloodStockService.getStockByBloodBank(bloodBankId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @RequestMapping(value = "/search", method = {RequestMethod.POST})
    @HasPermission("BLOOD_STOCK_VIEW")
    public ResponseEntity<ApiResponse<List<StockSearchResponse>>> searchAvailableStock(
            @Valid @RequestBody BloodStockSearchRequest request) {
        
        List<StockSearchResponse> response = bloodStockService.searchAvailableStock(
                request.getBloodGroupCode(),
                request.getStateId(),
                request.getDistrictId(),
                request.getMinUnits()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/stock/increment")
    @HasPermission("BLOOD_STOCK_MANAGE")
    public ResponseEntity<ApiResponse<Void>> incrementStock(
            @Valid @RequestBody BloodStockAdjustRequest request) {
        log.info("S2S stock increment request received for blood bank ID: {}", request.getBloodBankId());
        bloodStockService.incrementStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock incremented successfully", null));
    }

    @PostMapping("/stock/decrement")
    @HasPermission("BLOOD_STOCK_MANAGE")
    public ResponseEntity<ApiResponse<Void>> decrementStock(
            @Valid @RequestBody BloodStockAdjustRequest request) {
        log.info("S2S stock decrement request received for blood bank ID: {}", request.getBloodBankId());
        bloodStockService.decrementStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock decremented successfully", null));
    }
}
