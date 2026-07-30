package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.identity.application.dto.response.PermissionMatrixResponse;
import com.bloodbank.identity.application.dto.response.PermissionResponse;
import com.bloodbank.identity.application.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permissions Management", description = "Simple endpoints for viewing permissions, modules, and the role-permission matrix grid")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/matrix")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "View Role-Permission Matrix", description = "View the complete grid mapping roles to permissions")
    public ResponseEntity<ApiResponse<PermissionMatrixResponse>> getPermissionMatrix() {
        PermissionMatrixResponse matrix = permissionService.getPermissionMatrix();
        return ResponseEntity.ok(ApiResponse.success("Permission matrix fetched successfully", matrix));
    }

    @GetMapping
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "View All Permissions", description = "View all permissions in the system")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/modules")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "View System Modules", description = "View list of all modules (e.g. Donor, Hospital, Blood Bank)")
    public ResponseEntity<ApiResponse<List<String>>> getAllModules() {
        List<String> modules = permissionService.getAllModules();
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/categories")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "View Permission Categories", description = "View permission category groupings")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        List<String> categories = permissionService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/grouped-by-module")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "View Permissions Grouped by Module", description = "View permissions organized under their respective modules")
    public ResponseEntity<ApiResponse<Map<String, List<PermissionResponse>>>> getPermissionsGroupedByModule() {
        Map<String, List<PermissionResponse>> grouped = permissionService.getPermissionsGroupedByModule();
        return ResponseEntity.ok(ApiResponse.success(grouped));
    }
}
