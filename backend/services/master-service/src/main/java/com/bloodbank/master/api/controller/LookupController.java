package com.bloodbank.master.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.common.contracts.dto.LookupItemContractResponse;
import com.bloodbank.master.application.dto.CreateLookupCategoryRequest;
import com.bloodbank.master.application.dto.CreateLookupItemRequest;
import com.bloodbank.master.application.dto.LookupCategoryResponse;
import com.bloodbank.master.application.dto.LookupItemResponse;
import com.bloodbank.master.application.dto.UpdateLookupCategoryRequest;
import com.bloodbank.master.application.dto.UpdateLookupItemRequest;
import com.bloodbank.master.application.service.LookupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/master")
@RequiredArgsConstructor
@Slf4j
public class LookupController {

    private final LookupService lookupService;

    // ==========================================
    // LOOKUP ITEM READ APIS
    // ==========================================

    @GetMapping("/lookups/{categoryCode}")
    public ResponseEntity<ApiResponse<List<LookupItemResponse>>> getItemsByCategory(
            @PathVariable String categoryCode) {
        
        List<LookupItemResponse> items = lookupService.getItemsByCategory(categoryCode);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/lookups/{categoryCode}/{itemCode}")
    public ResponseEntity<ApiResponse<LookupItemContractResponse>> getLookupItem(
            @PathVariable String categoryCode,
            @PathVariable String itemCode) {
        
        LookupItemResponse item = lookupService.getItemByCategoryAndCode(categoryCode, itemCode);
        LookupItemContractResponse response = LookupItemContractResponse.builder()
                .id(item.getId())
                .code(item.getCode())
                .label(item.getLabel())
                .sortOrder(item.getSortOrder())
                .active(item.isActive())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/lookups/{categoryCode}/{itemCode}/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateLookupItemActive(
            @PathVariable String categoryCode,
            @PathVariable String itemCode) {
        
        boolean isActive = lookupService.validateLookupItemActive(categoryCode, itemCode);
        return ResponseEntity.ok(ApiResponse.success(isActive));
    }

    // ==========================================
    // ADMIN LOOKUP CATEGORY APIS
    // ==========================================

    @GetMapping("/admin/lookups/categories")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<List<LookupCategoryResponse>>> getAllCategories() {
        List<LookupCategoryResponse> categories = lookupService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @PostMapping("/admin/lookups/categories")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<LookupCategoryResponse>> createCategory(
            @Valid @RequestBody CreateLookupCategoryRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "ADMIN";
        LookupCategoryResponse response = lookupService.createCategory(request, username);
        return new ResponseEntity<>(ApiResponse.success("Category created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/admin/lookups/categories/{id}")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<LookupCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLookupCategoryRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "ADMIN";
        LookupCategoryResponse response = lookupService.updateCategory(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    @DeleteMapping("/admin/lookups/categories/{id}")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "ADMIN";
        lookupService.deleteCategory(id, username);
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully", null));
    }

    // ==========================================
    // ADMIN LOOKUP ITEM APIS
    // ==========================================

    @PostMapping("/admin/lookups/items")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<LookupItemResponse>> createItem(
            @Valid @RequestBody CreateLookupItemRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "ADMIN";
        LookupItemResponse response = lookupService.createItem(request, username);
        return new ResponseEntity<>(ApiResponse.success("Lookup item created successfully", response), HttpStatus.CREATED);
    }

    @PutMapping("/admin/lookups/items/{id}")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<LookupItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLookupItemRequest request,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "ADMIN";
        LookupItemResponse response = lookupService.updateItem(id, request, username);
        return ResponseEntity.ok(ApiResponse.success("Lookup item updated successfully", response));
    }

    @DeleteMapping("/admin/lookups/items/{id}")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deactivateItem(
            @PathVariable Long id,
            Principal principal) {
        
        String username = principal != null ? principal.getName() : "ADMIN";
        lookupService.deactivateItem(id, username);
        return ResponseEntity.ok(ApiResponse.success("Lookup item deactivated successfully (soft-deleted)", null));
    }

    // ==========================================
    // ADMIN CACHE FLUSH API
    // ==========================================

    @PostMapping("/admin/cache/flush")
    @HasPermission("MASTER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> flushCache() {
        lookupService.flushAllCaches();
        return ResponseEntity.ok(ApiResponse.success("Master service Redis cache flushed successfully", null));
    }
}
