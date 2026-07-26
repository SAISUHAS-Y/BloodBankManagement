package com.bloodbank.identity.application.service;

import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.identity.application.dto.AssignRoleRequest;
import com.bloodbank.identity.application.dto.RegisterUserRequest;
import com.bloodbank.identity.application.dto.RoleRequest;
import com.bloodbank.identity.application.dto.RoleResponse;
import com.bloodbank.identity.application.dto.PermissionResponse;

import com.bloodbank.common.core.dto.PageResponse;
import com.bloodbank.identity.application.dto.UserSearchRequest;

import java.util.List;

public interface AdminService {
    UserSummaryResponse createUser(RegisterUserRequest request);
    UserSummaryResponse getUserById(Long id);
    UserSummaryResponse getUserSummaryByUsername(String username);
    List<UserSummaryResponse> getAllUsers();
    PageResponse<UserSummaryResponse> searchUsers(UserSearchRequest request);
    void deleteUser(Long id);

    void activateUser(Long id);
    void deactivateUser(Long id);
    void lockUser(Long id);
    void unlockUser(Long id);
    void suspendUser(Long id);

    RoleResponse createRole(RoleRequest request);
    List<RoleResponse> getAllRoles();
    void deleteRole(Long id);
    void assignRolesToUser(AssignRoleRequest request);

    List<PermissionResponse> getAllPermissions();
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);
}
