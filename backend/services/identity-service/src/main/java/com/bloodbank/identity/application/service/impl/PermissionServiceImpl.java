package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.identity.application.dto.PermissionMatrixResponse;
import com.bloodbank.identity.application.dto.PermissionResponse;
import com.bloodbank.identity.application.service.PermissionService;
import com.bloodbank.identity.domain.entity.Permission;
import com.bloodbank.identity.domain.entity.PermissionDependency;
import com.bloodbank.identity.domain.entity.Role;
import com.bloodbank.identity.domain.entity.RolePermission;
import com.bloodbank.identity.domain.repository.PermissionDependencyRepository;
import com.bloodbank.identity.domain.repository.PermissionRepository;
import com.bloodbank.identity.domain.repository.RolePermissionRepository;
import com.bloodbank.identity.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PermissionDependencyRepository permissionDependencyRepository;

    @Override
    @Transactional(readOnly = true)
    public PermissionMatrixResponse getPermissionMatrix() {
        List<Role> roles = roleRepository.findAll();
        List<Permission> permissions = permissionRepository.findAll();
        List<RolePermission> rolePermissions = rolePermissionRepository.findAll();

        List<PermissionMatrixResponse.RoleSummary> roleSummaries = roles.stream()
                .map(r -> PermissionMatrixResponse.RoleSummary.builder()
                        .id(r.getId())
                        .code(r.getName())
                        .displayName(r.getDisplayName() != null ? r.getDisplayName() : r.getName())
                        .category(r.getCategory())
                        .systemRole(r.isSystemRole())
                        .enabled(r.isEnabled())
                        .parentRoleCode(r.getParentRole() != null ? r.getParentRole().getName() : null)
                        .build())
                .toList();

        List<String> modules = permissions.stream()
                .map(Permission::getModule)
                .distinct()
                .sorted()
                .toList();

        Map<String, List<PermissionResponse>> permissionsByModule = permissions.stream()
                .map(this::mapToResponse)
                .collect(Collectors.groupingBy(PermissionResponse::getModule));

        Map<String, Set<Long>> matrix = new HashMap<>();
        roles.forEach(r -> matrix.put(r.getName(), new HashSet<>()));

        rolePermissions.forEach(rp -> {
            String roleCode = rp.getRole().getName();
            if (matrix.containsKey(roleCode)) {
                matrix.get(roleCode).add(rp.getPermission().getId());
            }
        });

        return PermissionMatrixResponse.builder()
                .roles(roleSummaries)
                .modules(modules)
                .permissionsByModule(permissionsByModule)
                .matrix(matrix)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return permissionRepository.findAll().stream()
                .map(Permission::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllModules() {
        return permissionRepository.findAll().stream()
                .map(Permission::getModule)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<PermissionResponse>> getPermissionsGroupedByModule() {
        return getAllPermissions().stream()
                .collect(Collectors.groupingBy(PermissionResponse::getModule));
    }

    @Override
    @Transactional(readOnly = true)
    public void validatePermissionDependencies(List<Long> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return;
        }

        Set<Long> assignedSet = new HashSet<>(permissionIds);
        List<PermissionDependency> dependencies = permissionDependencyRepository.findByPermissionIdIn(permissionIds);

        for (PermissionDependency dep : dependencies) {
            if (!assignedSet.contains(dep.getDependsOnPermissionId())) {
                Permission perm = permissionRepository.findById(dep.getPermissionId()).orElse(null);
                Permission reqPerm = permissionRepository.findById(dep.getDependsOnPermissionId()).orElse(null);

                String permName = perm != null ? perm.getCode() : String.valueOf(dep.getPermissionId());
                String reqName = reqPerm != null ? reqPerm.getCode() : String.valueOf(dep.getDependsOnPermissionId());

                throw new InvalidInputException(
                        String.format("Permission [%s] requires missing prerequisite permission [%s]", permName, reqName)
                );
            }
        }
    }

    @Override
    @Transactional
    public void addPermissionDependency(Long permissionId, Long dependsOnPermissionId) {
        Permission perm = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permissionId));
        Permission reqPerm = permissionRepository.findById(dependsOnPermissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite permission not found with ID: " + dependsOnPermissionId));

        PermissionDependency dep = new PermissionDependency();
        dep.setPermissionId(perm.getId());
        dep.setDependsOnPermissionId(reqPerm.getId());
        permissionDependencyRepository.save(dep);

        log.info("Registered permission dependency: [{}] requires [{}]", perm.getCode(), reqPerm.getCode());
    }

    private PermissionResponse mapToResponse(Permission p) {
        return PermissionResponse.builder()
                .id(p.getId())
                .code(p.getCode())
                .displayName(p.getDisplayName() != null ? p.getDisplayName() : p.getCode())
                .description(p.getDescription())
                .module(p.getModule())
                .category(p.getCategory())
                .permissionGroup(p.getPermissionGroup())
                .build();
    }
}
