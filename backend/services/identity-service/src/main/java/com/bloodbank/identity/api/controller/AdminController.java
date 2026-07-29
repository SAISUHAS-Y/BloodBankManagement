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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Account Management", description = "Simple endpoints for creating, searching, locking, activating, and managing user accounts")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/admin/users")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Create New User Account", description = "Creates a new user profile with default role assignment")
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
    @Operation(summary = "List All User Accounts", description = "Retrieves a list of all registered user accounts")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getAllUsers() {
        List<UserSummaryResponse> response = adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/admin/users/search")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Search Users with Multi-Field Filters", description = "Executes paginated JPA Specification search with status, tenant, date range, and free-text filters")
    public ResponseEntity<ApiResponse<com.bloodbank.common.core.dto.PageResponse<UserSummaryResponse>>> searchUsers(
            @Valid @RequestBody com.bloodbank.identity.application.dto.UserSearchRequest request) {
        
        com.bloodbank.common.core.dto.PageResponse<UserSummaryResponse> response = adminService.searchUsers(request);
        return ResponseEntity.ok(ApiResponse.success("Users search executed successfully", response));
    }

    @PostMapping("/admin/users/{id}/activate")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Activate User Account", description = "Transitions user account status to ACTIVE")
    public ResponseEntity<ApiResponse<Void>> activateUser(@PathVariable Long id) {
        adminService.activateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User activated successfully", null));
    }

    @PostMapping("/admin/users/{id}/deactivate")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Deactivate User Account", description = "Transitions user account status to DEACTIVATED")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated successfully", null));
    }

    @PostMapping("/admin/users/{id}/lock")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Lock User Account", description = "Locks user account to block login attempts")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable Long id) {
        adminService.lockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User locked successfully", null));
    }

    @PostMapping("/admin/users/{id}/unlock")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Unlock User Account", description = "Unlocks user account to restore access")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable Long id) {
        adminService.unlockUser(id);
        return ResponseEntity.ok(ApiResponse.success("User unlocked successfully", null));
    }

    @PostMapping("/admin/users/{id}/suspend")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Suspend User Account", description = "Suspends user account due to administrative policy violation")
    public ResponseEntity<ApiResponse<Void>> suspendUser(@PathVariable Long id) {
        adminService.suspendUser(id);
        return ResponseEntity.ok(ApiResponse.success("User suspended successfully", null));
    }

    @DeleteMapping("/admin/users/{id}")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Delete User Account (Soft Delete)", description = "Soft-deletes user account record in database")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User soft-deleted successfully", null));
    }

    @PostMapping("/admin/users/assign-roles")
    @HasPermission("USER_MANAGE")
    @Operation(summary = "Assign Roles to User Account", description = "Reassigns target user's role set")
    public ResponseEntity<ApiResponse<Void>> assignRolesToUser(
            @Valid @RequestBody AssignRoleRequest request) {
        
        adminService.assignRolesToUser(request);
        return ResponseEntity.ok(ApiResponse.success("Roles assigned to user successfully", null));
    }

    @GetMapping("/users/{id}/summary")
    @Operation(summary = "Get User Summary by ID (Internal)", description = "Internal inter-service endpoint to fetch user summary by ID")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getInternalUserSummary(@PathVariable Long id) {
        UserSummaryResponse response = adminService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }

    @GetMapping("/users/summary")
    @Operation(summary = "Get User Summary by Username (Internal)", description = "Internal inter-service endpoint to fetch user summary by username")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> getInternalUserSummaryByUsername(@RequestParam String username) {
        UserSummaryResponse response = adminService.getUserSummaryByUsername(username);
        return ResponseEntity.ok(ApiResponse.success("User fetched successfully", response));
    }
}
