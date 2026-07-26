package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.identity.application.dto.CloneRoleRequest;
import com.bloodbank.identity.application.dto.CreateRoleRequest;
import com.bloodbank.identity.application.dto.RoleHierarchyUpdateRequest;
import com.bloodbank.identity.application.dto.RoleResponse;
import com.bloodbank.identity.application.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role Governance", description = "Endpoints for defining, cloning, inheriting, and enabling custom enterprise roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Create Enterprise Role", description = "Creates a new custom enterprise role with optional parent inheritance")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return new ResponseEntity<>(ApiResponse.success("Role created successfully", response), HttpStatus.CREATED);
    }

    @GetMapping
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Get All Roles", description = "Lists all defined roles and their assigned permission IDs")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/{id}")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Get Role Details", description = "Fetches a specific role by ID")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.success(role));
    }

    @PostMapping("/{id}/clone")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Clone Target Role", description = "Duplicates an existing role's metadata and permission matrix into a new role")
    public ResponseEntity<ApiResponse<RoleResponse>> cloneRole(@PathVariable Long id, @Valid @RequestBody CloneRoleRequest request) {
        RoleResponse response = roleService.cloneRole(id, request);
        return new ResponseEntity<>(ApiResponse.success("Role cloned successfully", response), HttpStatus.CREATED);
    }

    @PostMapping("/{sourceRoleId}/copy-permissions/{targetRoleId}")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Copy Permissions Across Roles", description = "Copies non-duplicate permission assignments from a source role to a target role")
    public ResponseEntity<ApiResponse<Void>> copyPermissions(@PathVariable Long sourceRoleId, @PathVariable Long targetRoleId) {
        roleService.copyPermissions(sourceRoleId, targetRoleId);
        return ResponseEntity.ok(ApiResponse.success("Permissions copied successfully", null));
    }

    @PostMapping("/{id}/enable")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Enable Role", description = "Activates a custom role for assignment")
    public ResponseEntity<ApiResponse<Void>> enableRole(@PathVariable Long id) {
        roleService.setRoleEnabled(id, true);
        return ResponseEntity.ok(ApiResponse.success("Role enabled successfully", null));
    }

    @PostMapping("/{id}/disable")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Disable Role", description = "Deactivates a custom role to block new assignments")
    public ResponseEntity<ApiResponse<Void>> disableRole(@PathVariable Long id) {
        roleService.setRoleEnabled(id, false);
        return ResponseEntity.ok(ApiResponse.success("Role disabled successfully", null));
    }

    @GetMapping("/hierarchy")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Get Role Hierarchy Tree", description = "Returns parent-to-child role inheritance mappings")
    public ResponseEntity<ApiResponse<Map<String, String>>> getRoleHierarchy() {
        Map<String, String> hierarchy = roleService.getRoleHierarchyMap();
        return ResponseEntity.ok(ApiResponse.success("Role hierarchy retrieved", hierarchy));
    }

    @PutMapping("/hierarchy")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Update Role Hierarchy Mapping", description = "Configures parent-to-child inheritance rules dynamically")
    public ResponseEntity<ApiResponse<Void>> updateRoleHierarchy(@Valid @RequestBody RoleHierarchyUpdateRequest request) {
        roleService.updateRoleHierarchy(request);
        return ResponseEntity.ok(ApiResponse.success("Role hierarchy updated successfully", null));
    }

    @DeleteMapping("/{id}")
    @HasPermission("ROLE_MANAGE")
    @Operation(summary = "Delete Custom Role", description = "Soft-deletes a non-system custom role")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }
}
