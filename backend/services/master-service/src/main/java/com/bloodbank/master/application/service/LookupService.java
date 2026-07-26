package com.bloodbank.master.application.service;

import com.bloodbank.master.application.dto.CreateLookupCategoryRequest;
import com.bloodbank.master.application.dto.CreateLookupItemRequest;
import com.bloodbank.master.application.dto.LookupCategoryResponse;
import com.bloodbank.master.application.dto.LookupItemResponse;
import com.bloodbank.master.application.dto.MasterDataBundleResponse;
import com.bloodbank.master.application.dto.UpdateLookupCategoryRequest;
import com.bloodbank.master.application.dto.UpdateLookupItemRequest;

import java.util.List;

public interface LookupService {
    List<LookupCategoryResponse> getAllCategories();
    LookupCategoryResponse getCategoryByCode(String code);
    LookupCategoryResponse createCategory(CreateLookupCategoryRequest request, String createdBy);
    LookupCategoryResponse updateCategory(Long id, UpdateLookupCategoryRequest request, String updatedBy);
    void deleteCategory(Long id, String updatedBy);

    List<LookupItemResponse> getItemsByCategory(String categoryCode);
    LookupItemResponse getItemByCategoryAndCode(String categoryCode, String itemCode);
    LookupItemResponse createItem(CreateLookupItemRequest request, String createdBy);
    LookupItemResponse updateItem(Long id, UpdateLookupItemRequest request, String updatedBy);
    void deactivateItem(Long id, String updatedBy);
    boolean validateLookupItemActive(String categoryCode, String itemCode);

    MasterDataBundleResponse getMasterDataBundle();
    void flushAllCaches();
}
