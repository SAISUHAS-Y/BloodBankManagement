package com.bloodbank.bloodbank.application.service;

import com.bloodbank.bloodbank.application.dto.response.BloodStockResponse;
import com.bloodbank.bloodbank.application.dto.response.StockSearchResponse;

import java.util.List;

public interface BloodStockService {
    BloodStockResponse adjustStock(Long id, double units, String reason, Long adjustedByUserId);
    List<BloodStockResponse> getStockByBloodBank(Long bloodBankId);
    List<StockSearchResponse> searchAvailableStock(String bloodGroupCode, Long stateId, Long districtId, double minUnits);
    void handleDonationCompleted(Long bloodBankId, String bloodGroupCode, String componentTypeCode, double units, String traceId);
    void incrementStock(com.bloodbank.common.contracts.dto.BloodStockAdjustRequest request);
    void decrementStock(com.bloodbank.common.contracts.dto.BloodStockAdjustRequest request);
}
