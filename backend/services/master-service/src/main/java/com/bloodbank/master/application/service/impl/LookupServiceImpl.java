package com.bloodbank.master.application.service.impl;

import com.bloodbank.common.core.constant.LookupConstants;
import com.bloodbank.common.exception.BusinessRuleViolationException;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.exception.enums.ErrorCode;
import com.bloodbank.master.application.dto.BloodGroupResponse;
import com.bloodbank.master.application.dto.CreateLookupCategoryRequest;
import com.bloodbank.master.application.dto.CreateLookupItemRequest;
import com.bloodbank.master.application.dto.LookupCategoryResponse;
import com.bloodbank.master.application.dto.LookupItemResponse;
import com.bloodbank.master.application.dto.MasterDataBundleResponse;
import com.bloodbank.master.application.dto.UpdateLookupCategoryRequest;
import com.bloodbank.master.application.dto.UpdateLookupItemRequest;
import com.bloodbank.master.application.event.MasterEventPublisher;
import com.bloodbank.master.application.service.BloodGroupService;
import com.bloodbank.master.application.service.LookupService;
import com.bloodbank.master.domain.entity.LookupCategory;
import com.bloodbank.master.domain.entity.LookupItem;
import com.bloodbank.master.domain.repository.LookupCategoryRepository;
import com.bloodbank.master.domain.repository.LookupItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LookupServiceImpl implements LookupService {

    private final LookupItemRepository lookupItemRepository;
    private final LookupCategoryRepository lookupCategoryRepository;
    private final BloodGroupService bloodGroupService;
    private final CacheManager cacheManager;
    private final MasterEventPublisher eventPublisher;

    private final ReentrantLock bundleLock = new ReentrantLock();

    // ==========================================
    // CATEGORY MANAGEMENT
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lookup_categories")
    public List<LookupCategoryResponse> getAllCategories() {
        log.info("Fetching lookup categories from database");
        return lookupCategoryRepository.findAll().stream()
                .map(this::mapCategoryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lookup_categories", key = "#code")
    public LookupCategoryResponse getCategoryByCode(String code) {
        LookupCategory category = lookupCategoryRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup category not found: " + code));
        return mapCategoryToResponse(category);
    }

    @Override
    @Transactional
    public LookupCategoryResponse createCategory(CreateLookupCategoryRequest request, String createdBy) {
        log.info("Creating lookup category: {}", request.getCode());
        if (lookupCategoryRepository.existsByCode(request.getCode())) {
            throw new InvalidInputException("Lookup category code already exists: " + request.getCode());
        }

        LookupCategory category = new LookupCategory();
        category.setCode(request.getCode());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSystemCategory(false);
        category.setCreatedBy(createdBy);
        category.setUpdatedBy(createdBy);

        LookupCategory saved = lookupCategoryRepository.save(category);
        evictCategoryListCache();
        return mapCategoryToResponse(saved);
    }

    @Override
    @Transactional
    public LookupCategoryResponse updateCategory(Long id, UpdateLookupCategoryRequest request, String updatedBy) {
        log.info("Updating lookup category ID: {}", id);
        LookupCategory category = lookupCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup category not found with ID: " + id));

        // SYSTEM CATEGORY INTEGRITY RULE: System categories cannot have their code modified
        if (category.isSystemCategory() && !category.getCode().equals(request.getCode())) {
            throw new BusinessRuleViolationException(
                    "System category '" + category.getCode() + "' is protected and cannot have its code modified.",
                    ErrorCode.BUSINESS_RULE_VIOLATION
            );
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setUpdatedBy(updatedBy);

        LookupCategory saved = lookupCategoryRepository.save(category);
        evictCategoryListCache();
        return mapCategoryToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id, String updatedBy) {
        log.info("Deleting lookup category ID: {}", id);
        LookupCategory category = lookupCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup category not found with ID: " + id));

        // SYSTEM CATEGORY INTEGRITY RULE: System categories can NEVER be deleted
        if (category.isSystemCategory()) {
            throw new BusinessRuleViolationException(
                    "System category '" + category.getCode() + "' is protected and cannot be deleted.",
                    ErrorCode.BUSINESS_RULE_VIOLATION
            );
        }

        category.delete();
        lookupCategoryRepository.save(category);
        evictCategoryListCache();
        evictCategoryCache(category.getCode());
    }

    // ==========================================
    // LOOKUP ITEM MANAGEMENT
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lookups", key = "#categoryCode")
    public List<LookupItemResponse> getItemsByCategory(String categoryCode) {
        log.info("Fetching active lookup items from database for category: {}", categoryCode);
        return lookupItemRepository.findByCategoryCodeAndActiveTrueOrderBySortOrderAsc(categoryCode).stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "lookup_items", key = "#categoryCode + ':' + #itemCode")
    public LookupItemResponse getItemByCategoryAndCode(String categoryCode, String itemCode) {
        log.info("Fetching single lookup item from database: category={} code={}", categoryCode, itemCode);
        LookupItem item = lookupItemRepository.findByCategoryCodeAndCode(categoryCode, itemCode)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup item not found for category " + categoryCode + " and code " + itemCode));
        return mapItemToResponse(item);
    }

    @Override
    @Transactional
    public LookupItemResponse createItem(CreateLookupItemRequest request, String createdBy) {
        log.info("Creating lookup item: category={} code={}", request.getCategoryCode(), request.getCode());

        LookupCategory category = lookupCategoryRepository.findByCode(request.getCategoryCode())
                .orElseThrow(() -> new ResourceNotFoundException("Lookup category not found: " + request.getCategoryCode()));

        if (lookupItemRepository.findByCategoryCodeAndCode(request.getCategoryCode(), request.getCode()).isPresent()) {
            throw new InvalidInputException("Lookup item with code " + request.getCode() + " already exists in category " + request.getCategoryCode());
        }

        LookupItem item = new LookupItem();
        item.setCategory(category);
        item.setCode(request.getCode());
        item.setLabel(request.getLabel());
        item.setSortOrder(request.getSortOrder());
        item.setActive(true);
        item.setMetadata(request.getMetadata());
        item.setCreatedBy(createdBy);
        item.setUpdatedBy(createdBy);

        LookupItem saved = lookupItemRepository.save(item);

        evictItemCaches(request.getCategoryCode(), saved.getCode());
        eventPublisher.publishLookupItemChanged(request.getCategoryCode(), saved.getCode(), "CREATED", UUID.randomUUID().toString());

        return mapItemToResponse(saved);
    }

    @Override
    @Transactional
    public LookupItemResponse updateItem(Long id, UpdateLookupItemRequest request, String updatedBy) {
        log.info("Updating lookup item ID: {}", id);

        LookupItem item = lookupItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup item not found with ID: " + id));

        String categoryCode = item.getCategory().getCode();
        String itemCode = item.getCode();

        item.setLabel(request.getLabel());
        item.setSortOrder(request.getSortOrder());
        item.setActive(request.getActive());
        item.setMetadata(request.getMetadata());
        item.setUpdatedBy(updatedBy);

        LookupItem saved = lookupItemRepository.save(item);

        evictItemCaches(categoryCode, itemCode);

        String changeType = saved.isActive() ? "UPDATED" : "DEACTIVATED";
        eventPublisher.publishLookupItemChanged(categoryCode, itemCode, changeType, UUID.randomUUID().toString());

        return mapItemToResponse(saved);
    }

    @Override
    @Transactional
    public void deactivateItem(Long id, String updatedBy) {
        log.info("Deactivating lookup item ID: {}", id);

        LookupItem item = lookupItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lookup item not found with ID: " + id));

        String categoryCode = item.getCategory().getCode();
        String itemCode = item.getCode();

        item.setActive(false);
        item.setUpdatedBy(updatedBy);
        lookupItemRepository.save(item);

        evictItemCaches(categoryCode, itemCode);
        eventPublisher.publishLookupItemChanged(categoryCode, itemCode, "DEACTIVATED", UUID.randomUUID().toString());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateLookupItemActive(String categoryCode, String itemCode) {
        return lookupItemRepository.findByCategoryCodeAndCode(categoryCode, itemCode)
                .map(item -> item.isActive())
                .orElse(false);
    }

    // ==========================================
    // OPTIMIZED MASTER DATA BUNDLE & STAMPEDE PROTECTION
    // ==========================================

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "master_bundle", key = "'all'")
    public MasterDataBundleResponse getMasterDataBundle() {
        log.info("Cache miss for master data bundle — acquiring stampede protection lock");
        bundleLock.lock();
        try {
            return buildBundleSinglePass();
        } finally {
            bundleLock.unlock();
        }
    }

    /**
     * Proactive Background Scheduled Cache Refresh:
     * Keeps the master data bundle warm in Redis every 15 minutes to guarantee 0ms cache miss latency.
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void refreshMasterDataBundleCache() {
        log.info("Executing scheduled proactive warming of master_bundle cache");
        try {
            MasterDataBundleResponse freshBundle = buildBundleSinglePass();
            Cache bundleCache = cacheManager.getCache("master_bundle");
            if (bundleCache != null) {
                bundleCache.put("all", freshBundle);
                log.info("Proactively updated 'all' in master_bundle cache");
            }
        } catch (Exception e) {
            log.error("Failed to proactively refresh master_bundle cache", e);
        }
    }

    private MasterDataBundleResponse buildBundleSinglePass() {
        log.info("Executing SINGLE AGGREGATION PASS query for master data bundle");
        
        // QUERY 1: Fetch ALL active lookup items in 1 single database query
        List<LookupItem> allActiveItems = lookupItemRepository.findByActiveTrueOrderBySortOrderAsc();
        
        Map<String, List<LookupItemResponse>> groupedItems = allActiveItems.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getCategory().getCode(),
                        Collectors.mapping(this::mapItemToResponse, Collectors.toList())
                ));

        // QUERY 2: Fetch ALL active blood groups in 1 single database query
        List<BloodGroupResponse> bloodGroups = bloodGroupService.getAllBloodGroups();

        return MasterDataBundleResponse.builder()
                .bloodGroups(bloodGroups)
                .genders(groupedItems.getOrDefault(LookupConstants.CAT_GENDER, Collections.emptyList()))
                .documentTypes(groupedItems.getOrDefault(LookupConstants.CAT_DOCUMENT_TYPE, Collections.emptyList()))
                .titles(groupedItems.getOrDefault(LookupConstants.CAT_TITLE, Collections.emptyList()))
                .idTypes(groupedItems.getOrDefault(LookupConstants.CAT_ID_TYPE, Collections.emptyList()))
                .donationTypes(groupedItems.getOrDefault(LookupConstants.CAT_DONATION_TYPE, Collections.emptyList()))
                .deferralReasons(groupedItems.getOrDefault(LookupConstants.CAT_DEFERRAL_REASON, Collections.emptyList()))
                .maritalStatuses(groupedItems.getOrDefault(LookupConstants.CAT_MARITAL_STATUS, Collections.emptyList()))
                .relationships(groupedItems.getOrDefault(LookupConstants.CAT_RELATIONSHIP, Collections.emptyList()))
                .componentTypes(groupedItems.getOrDefault(LookupConstants.CAT_COMPONENT_TYPE, Collections.emptyList()))
                .build();
    }

    // ==========================================
    // CACHE FLUSHING & UTILITIES
    // ==========================================

    @Override
    public void flushAllCaches() {
        log.info("Admin initiated full cache flush for Master Service");
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("Flushed Redis cache region: {}", cacheName);
                }
            });
        }
    }

    private void evictCategoryListCache() {
        Cache cache = cacheManager.getCache("lookup_categories");
        if (cache != null) {
            cache.clear();
        }
    }

    private void evictCategoryCache(String categoryCode) {
        Cache cache = cacheManager.getCache("lookups");
        if (cache != null) {
            cache.evict(categoryCode);
        }
        evictBundleCache();
    }

    private void evictItemCaches(String categoryCode, String itemCode) {
        evictCategoryCache(categoryCode);
        Cache cache = cacheManager.getCache("lookup_items");
        if (cache != null) {
            String key = categoryCode + ":" + itemCode;
            cache.evict(key);
        }
        evictBundleCache();
    }

    private void evictBundleCache() {
        Cache cache = cacheManager.getCache("master_bundle");
        if (cache != null) {
            cache.evict("all");
            log.info("Evicted key 'all' from 'master_bundle' cache");
        }
    }

    private LookupCategoryResponse mapCategoryToResponse(LookupCategory category) {
        return LookupCategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .description(category.getDescription())
                .systemCategory(category.isSystemCategory())
                .build();
    }

    private LookupItemResponse mapItemToResponse(LookupItem item) {
        return LookupItemResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .label(item.getLabel())
                .sortOrder(item.getSortOrder())
                .active(item.isActive())
                .metadata(item.getMetadata())
                .build();
    }
}
