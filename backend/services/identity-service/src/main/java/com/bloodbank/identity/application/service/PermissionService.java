package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.response.PermissionMatrixResponse;
import com.bloodbank.identity.application.dto.response.PermissionResponse;

import java.util.List;
import java.util.Map;

public interface PermissionService {

    PermissionMatrixResponse getPermissionMatrix();

    List<PermissionResponse> getAllPermissions();

    List<String> getAllCategories();

    List<String> getAllModules();

    Map<String, List<PermissionResponse>> getPermissionsGroupedByModule();
}
