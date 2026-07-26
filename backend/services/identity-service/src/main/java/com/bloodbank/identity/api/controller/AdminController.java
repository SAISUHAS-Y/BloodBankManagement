package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.identity.application.dto.AssignRoleRequest;
import com.bloodbank.identity.application.dto.PermissionResponse;
import com.bloodbank.identity.application.dto.RegisterUserRequest;
import com.bloodbank.identity.application.dto.RoleRequest;
import com.bloodbank.identity.application.dto.RoleResponse;
import com.bloodbank.identity.application.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/users")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> createUser(
            @Valid @RequestBody RegisterUserRequest request) {
        
        UserSummaryResponse response = adminService.createUser(request);
        return new ResponseEntity<>(
                ApiResponse.success("User created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/admin/users")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        List<UserSummaryResponse> response = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/admin/users/search")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<com.bloodbank.common.core.dto.PageResponse<UserSummaryResponse>>> searchUsers(
            @Valid @RequestBody com.bloodbank.identity.application.dto.UserSearchRequest request) {
        
        com.bloodbank.common.core.dto.PageResponse<UserSummaryResponse> response = adminService.searchUsers(request);
        return ResponseEntity.ok(ApiResponse.success("Users search executed successfully", response));
    }

    @PostMapping("/admin/users/{id}/activate")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
    }

    @PostMapping("/admin/users/{id}/deactivate")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @PostMapping("/admin/users/{id}/lock")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long id) {
        adminService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
    }

    @PostMapping("/admin/users/{id}/unlock")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        adminService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
    }

    @PostMapping("/admin/users/{id}/suspend")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable Long id) {
        adminService.suspendUser(id);
        return ResponseEntity.ok(ApiResponse.success("User suspended successfully", null));
    }

    @DeleteMapping("/admin/users/{id}")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User soft-deleted successfully", null));
    }

    @PostMapping("/admin/users/assign-roles")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> assignRolesToUser(
            @Valid @RequestBody AssignRoleRequest request) {
        
        adminService.assignRolesToUser(request);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned to user successfully", null));
    }

    @PostMapping("/admin/roles")
    @HasPermission("DONOR_VIEW")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(
            @Valid @RequestBody RoleRequest request) {
        
        RoleResponse response = adminService.createRole(request);
        return new ResponseEntity<>(
                ApiResponse.success("Role created successfully", response),
                HttpStatus.CREATED
        );
    }

    @GetMapping("/admin/roles")
    @HasPermission("DONOR_VIEW")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        List<RoleResponse> response = adminService.getAllRoles();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/admin/roles/{id}")
    @HasPermission("USER_MANAGE")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        adminService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success("Role deleted successfully", null));
    }

    @PostMapping("/admin/roles/{roleId}/permissions")
    @HasPermission("BLOODBANK_MANAGE")
    public ResponseEntity<ApiResponse<Void>> assignPermissionsToRole(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {
        
        adminService.assignPermissionsToRole(roleId, permissionIds);
        return ResponseEntity.ok(ApiResponse.success("Permissions assigned to role successfully", null));
    }

    @GetMapping("/admin/permissions")
    @HasPermission("BLOODBANK_MANAGE")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        List<PermissionResponse> response = adminService.getAllPermissions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/{id}/summary")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getInternalUserSummary(@PathVariable Long id) {
        UserSummaryResponse response = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @GetMapping("/users/summary")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getInternalUserSummaryByUsername(@RequestParam String username) {
        UserSummaryResponse response = adminService.getUserSummaryByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }
}
