package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.identity.application.dto.PermissionMatrixResponse;
import com.bloodbank.identity.application.dto.PermissionResponse;
import com.bloodbank.identity.application.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/permissions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Permission System", description = "Endpoints for inspecting system permissions, modules, and the Role-Permission Matrix grid")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/matrix")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "Get Role-Permission Matrix Grid", description = "Fetches the complete matrix grid mapping all roles to module-grouped permissions")
    public ResponseEntity<ApiResponse<PermissionMatrixResponse>> getPermissionMatrix() {
        PermissionMatrixResponse matrix = permissionService.getPermissionMatrix();
        return ResponseEntity.ok(ApiResponse.success("Permission matrix fetched successfully", matrix));
    }

    @GetMapping
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "Get All System Permissions", description = "Returns all registered system permissions")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/modules")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "Get System Modules", description = "Returns list of all distinct system modules")
    public ResponseEntity<ApiResponse<List<String>>> getAllModules() {
        List<String> modules = permissionService.getAllModules();
        return ResponseEntity.ok(ApiResponse.success(modules));
    }

    @GetMapping("/categories")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "Get Permission Categories", description = "Returns list of all permission categories")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        List<String> categories = permissionService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    @GetMapping("/grouped-by-module")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "Get Permissions Grouped by Module", description = "Returns system permissions categorized by module")
    public ResponseEntity<ApiResponse<Map<String, List<PermissionResponse>>>> getPermissionsGroupedByModule() {
        Map<String, List<PermissionResponse>> grouped = permissionService.getPermissionsGroupedByModule();
        return ResponseEntity.ok(ApiResponse.success(grouped));
    }

    @PostMapping("/{permissionId}/dependencies/{dependsOnPermissionId}")
    @HasPermission("PERMISSION_MANAGE")
    @Operation(summary = "Add Permission Prerequisite Dependency", description = "Registers a requirement that permissionId requires dependsOnPermissionId")
    public ResponseEntity<ApiResponse<Void>> addPermissionDependency(
            @PathVariable Long permissionId,
            @PathVariable Long dependsOnPermissionId) {
        permissionService.addPermissionDependency(permissionId, dependsOnPermissionId);
        return ResponseEntity.ok(ApiResponse.success("Permission dependency registered successfully", null));
    }
}
