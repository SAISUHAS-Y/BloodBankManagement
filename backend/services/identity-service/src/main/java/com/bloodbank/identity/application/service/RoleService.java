package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.CreateRoleRequest;
import com.bloodbank.identity.application.dto.RoleHierarchyUpdateRequest;
import com.bloodbank.identity.application.dto.RoleResponse;

import java.util.List;
import java.util.Map;

public interface RoleService {

    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse getRoleById(Long id);

    RoleResponse getRoleByCode(String code);

    List<RoleResponse> getAllRoles();

    void setRoleEnabled(Long roleId, boolean enabled);

    void updateRoleHierarchy(RoleHierarchyUpdateRequest request);

    Map<String, String> getRoleHierarchyMap();

    String buildSpringSecurityRoleHierarchyString();

    void deleteRole(Long id);
}
