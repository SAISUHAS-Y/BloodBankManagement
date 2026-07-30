package com.bloodbank.bloodbank.application.service.impl;

import com.bloodbank.bloodbank.application.dto.response.BloodStockResponse;
import com.bloodbank.bloodbank.application.dto.response.StockSearchResponse;
import com.bloodbank.bloodbank.application.service.BloodStockService;
import com.bloodbank.bloodbank.domain.entity.BloodStock;
import com.bloodbank.bloodbank.domain.entity.ProcessedStockTransaction;
import com.bloodbank.bloodbank.domain.entity.StockAdjustmentLog;
import com.bloodbank.bloodbank.domain.repository.BloodStockRepository;
import com.bloodbank.bloodbank.domain.repository.ProcessedStockTransactionRepository;
import com.bloodbank.bloodbank.domain.repository.StockAdjustmentLogRepository;
import com.bloodbank.common.contracts.client.MasterServiceClient;
import com.bloodbank.common.contracts.dto.BloodGroupContractResponse;
import com.bloodbank.common.contracts.dto.BloodStockAdjustRequest;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodStockServiceImpl implements BloodStockService {

    private final BloodStockRepository bloodStockRepository;
    private final StockAdjustmentLogRepository adjustmentLogRepository;
    private final ProcessedStockTransactionRepository processedStockTransactionRepository;
    private final MasterServiceClient masterServiceClient;
    private final MasterDataResolver dataResolver;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public BloodStockResponse adjustStock(Long id, double units, String reason, Long adjustedByUserId) {
        log.info("Adjusting blood stock ID={} to units={} by userId={} for reason: {}",
                id, units, adjustedByUserId, reason);

        if (units < 0) {
            throw new InvalidInputException("Available units cannot be negative: " + units);
        }

        // PESSIMISTIC LOCK: Acquire row lock via SELECT FOR UPDATE to prevent race conditions
        BloodStock stock = bloodStockRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood stock record not found with ID: " + id));

        double previousUnits = stock.getUnitsAvailable();
        stock.setUnitsAvailable(units);
        stock.setLastUpdatedAt(Instant.now());
        stock.setUpdatedBy("USER_" + adjustedByUserId);

        BloodStock saved = bloodStockRepository.save(stock);

        StockAdjustmentLog logEntry = new StockAdjustmentLog();
        logEntry.setBloodStockId(saved.getId());
        logEntry.setPreviousUnits(previousUnits);
        logEntry.setNewUnits(units);
        logEntry.setReason(reason);
        logEntry.setAdjustedBy(adjustedByUserId);
        logEntry.setAdjustedAt(Instant.now());
        logEntry.setCreatedBy("USER_" + adjustedByUserId);
        logEntry.setUpdatedBy("USER_" + adjustedByUserId);
        adjustmentLogRepository.save(logEntry);

        evictStockCache(stock.getBloodBank().getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "blood_stock_bank", key = "#bloodBankId")
    public List<BloodStockResponse> getStockByBloodBank(Long bloodBankId) {
        log.info("Fetching blood stock list from database for blood bank ID: {}", bloodBankId);
        return bloodStockRepository.findByBloodBankId(bloodBankId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockSearchResponse> searchAvailableStock(String bloodGroupCode, Long stateId, Long districtId, double minUnits) {
        log.info("Searching available stock: bloodGroupCode={} stateId={} districtId={} minUnits={}",
                bloodGroupCode, stateId, districtId, minUnits);

        List<BloodGroupContractResponse> groups = masterServiceClient.getBloodGroups().getData();
        BloodGroupContractResponse targetGroup = groups.stream()
                .filter(g -> g.getCode().equalsIgnoreCase(bloodGroupCode))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("Invalid blood group code: " + bloodGroupCode));

        List<BloodStock> stocks = bloodStockRepository.searchAvailableStock(targetGroup.getId(), stateId, districtId, minUnits);

        return stocks.stream()
                .map(s -> StockSearchResponse.builder()
                        .bloodBankId(s.getBloodBank().getId())
                        .bloodBankName(s.getBloodBank().getName())
                        .stateName(dataResolver.getStateName(s.getBloodBank().getStateId()))
                        .districtName(dataResolver.getDistrictName(s.getBloodBank().getStateId(), s.getBloodBank().getDistrictId()))
                        .cityName(dataResolver.getCityName(s.getBloodBank().getDistrictId(), s.getBloodBank().getCityId()))
                        .addressLine(s.getBloodBank().getAddressLine())
                        .phone(s.getBloodBank().getPhone())
                        .unitsAvailable(s.getUnitsAvailable())
                        .lastUpdatedAt(s.getLastUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void handleDonationCompleted(Long bloodBankId, String bloodGroupCode, String componentTypeCode, double units, String traceId) {
        log.info("Processing donation completed event callback: bloodBankId={} group={} component={} units={} traceId={}",
                bloodBankId, bloodGroupCode, componentTypeCode, units, traceId);

        List<BloodGroupContractResponse> groups = masterServiceClient.getBloodGroups().getData();
        BloodGroupContractResponse targetGroup = groups.stream()
                .filter(g -> g.getCode().equalsIgnoreCase(bloodGroupCode))
                .findFirst()
                .orElseThrow(() -> new InvalidInputException("Invalid blood group code in donation completed: " + bloodGroupCode));

        // PESSIMISTIC LOCK: Acquire row lock via SELECT FOR UPDATE
        BloodStock stock = bloodStockRepository.findForUpdate(bloodBankId, targetGroup.getId(), componentTypeCode)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Zeroed stock record not initialized for blood bank " + bloodBankId + 
                        ", group " + bloodGroupCode + ", and component " + componentTypeCode));

        double previousUnits = stock.getUnitsAvailable();
        stock.setUnitsAvailable(previousUnits + units);
        stock.setLastUpdatedAt(Instant.now());
        stock.setUpdatedBy("EVENT_DONATION_COMPLETED");
        bloodStockRepository.save(stock);

        StockAdjustmentLog logEntry = new StockAdjustmentLog();
        logEntry.setBloodStockId(stock.getId());
        logEntry.setPreviousUnits(previousUnits);
        logEntry.setNewUnits(stock.getUnitsAvailable());
        logEntry.setReason("Donation Completed Event ID transition");
        logEntry.setAdjustedBy(0L);
        logEntry.setAdjustedAt(Instant.now());
        logEntry.setCreatedBy("SYSTEM_EVENT");
        logEntry.setUpdatedBy("SYSTEM_EVENT");
        adjustmentLogRepository.save(logEntry);

        evictStockCache(bloodBankId);
    }

    @Override
    @Transactional
    public void incrementStock(BloodStockAdjustRequest request) {
        log.info("Incrementing stock for blood bank ID: {}, group: {}, component: {}, units: {}, key: {}",
                request.getBloodBankId(), request.getBloodGroupId(), request.getComponentTypeCode(),
                request.getUnits(), request.getIdempotencyKey());

        if (processedStockTransactionRepository.existsById(request.getIdempotencyKey())) {
            log.warn("Stock transaction with idempotency key {} already processed. Skipping.", request.getIdempotencyKey());
            return;
        }

        // PESSIMISTIC LOCK: Acquire row lock via SELECT FOR UPDATE
        BloodStock stock = bloodStockRepository.findForUpdate(
                request.getBloodBankId(), request.getBloodGroupId(), request.getComponentTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Blood stock record not found/initialized for blood bank " 
                        + request.getBloodBankId() + ", group " + request.getBloodGroupId() 
                        + ", component " + request.getComponentTypeCode()));

        double previousUnits = stock.getUnitsAvailable();
        stock.setUnitsAvailable(previousUnits + request.getUnits());
        stock.setLastUpdatedAt(Instant.now());
        stock.setUpdatedBy("S2S_INCREMENT");
        bloodStockRepository.save(stock);

        StockAdjustmentLog logEntry = new StockAdjustmentLog();
        logEntry.setBloodStockId(stock.getId());
        logEntry.setPreviousUnits(previousUnits);
        logEntry.setNewUnits(stock.getUnitsAvailable());
        logEntry.setReason(request.getReason() != null ? request.getReason() : "S2S Auto Increment");
        logEntry.setAdjustedBy(0L);
        logEntry.setAdjustedAt(Instant.now());
        logEntry.setCreatedBy("S2S_SYSTEM");
        logEntry.setUpdatedBy("S2S_SYSTEM");
        adjustmentLogRepository.save(logEntry);

        processedStockTransactionRepository.save(new ProcessedStockTransaction(request.getIdempotencyKey(), Instant.now()));

        evictStockCache(request.getBloodBankId());
        log.info("Successfully incremented stock by {} units.", request.getUnits());
    }

    @Override
    @Transactional
    public void decrementStock(BloodStockAdjustRequest request) {
        log.info("Decrementing stock for blood bank ID: {}, group: {}, component: {}, units: {}, key: {}",
                request.getBloodBankId(), request.getBloodGroupId(), request.getComponentTypeCode(),
                request.getUnits(), request.getIdempotencyKey());

        if (processedStockTransactionRepository.existsById(request.getIdempotencyKey())) {
            log.warn("Stock transaction with idempotency key {} already processed. Skipping.", request.getIdempotencyKey());
            return;
        }

        // PESSIMISTIC LOCK: Acquire row lock via SELECT FOR UPDATE
        BloodStock stock = bloodStockRepository.findForUpdate(
                request.getBloodBankId(), request.getBloodGroupId(), request.getComponentTypeCode())
                .orElseThrow(() -> new ResourceNotFoundException("Blood stock record not found/initialized for blood bank " 
                        + request.getBloodBankId() + ", group " + request.getBloodGroupId() 
                        + ", component " + request.getComponentTypeCode()));

        double previousUnits = stock.getUnitsAvailable();
        if (previousUnits < request.getUnits()) {
            throw new InvalidInputException("Insufficient stock in blood bank. Requested: " 
                    + request.getUnits() + " units, but only " + previousUnits + " units are available.");
        }

        stock.setUnitsAvailable(previousUnits - request.getUnits());
        stock.setLastUpdatedAt(Instant.now());
        stock.setUpdatedBy("S2S_DECREMENT");
        bloodStockRepository.save(stock);

        StockAdjustmentLog logEntry = new StockAdjustmentLog();
        logEntry.setBloodStockId(stock.getId());
        logEntry.setPreviousUnits(previousUnits);
        logEntry.setNewUnits(stock.getUnitsAvailable());
        logEntry.setReason(request.getReason() != null ? request.getReason() : "S2S Auto Decrement");
        logEntry.setAdjustedBy(0L);
        logEntry.setAdjustedAt(Instant.now());
        logEntry.setCreatedBy("S2S_SYSTEM");
        logEntry.setUpdatedBy("S2S_SYSTEM");
        adjustmentLogRepository.save(logEntry);

        processedStockTransactionRepository.save(new ProcessedStockTransaction(request.getIdempotencyKey(), Instant.now()));

        evictStockCache(request.getBloodBankId());
        log.info("Successfully decremented stock by {} units.", request.getUnits());
    }

    private void evictStockCache(Long bloodBankId) {
        Cache cache = cacheManager.getCache("blood_stock_bank");
        if (cache != null) {
            cache.evict(bloodBankId);
            log.info("Evicted key '{}' from 'blood_stock_bank' cache.", bloodBankId);
        }
    }

    private BloodStockResponse mapToResponse(BloodStock stock) {
        return BloodStockResponse.builder()
                .id(stock.getId())
                .bloodBankId(stock.getBloodBank().getId())
                .bloodBankName(stock.getBloodBank().getName())
                .bloodGroupId(stock.getBloodGroupId())
                .bloodGroupLabel(dataResolver.getBloodGroupLabel(stock.getBloodGroupId()))
                .componentTypeCode(stock.getComponentTypeCode())
                .componentTypeLabel(dataResolver.getComponentTypeLabel(stock.getComponentTypeCode()))
                .unitsAvailable(stock.getUnitsAvailable())
                .lastUpdatedAt(stock.getLastUpdatedAt())
                .build();
    }
}
