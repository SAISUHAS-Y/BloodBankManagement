package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.exception.BusinessRuleViolationException;
import com.bloodbank.common.exception.DuplicateResourceException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.identity.application.dto.CloneRoleRequest;
import com.bloodbank.identity.application.dto.CreateRoleRequest;
import com.bloodbank.identity.application.dto.RoleHierarchyUpdateRequest;
import com.bloodbank.identity.application.dto.RoleResponse;
import com.bloodbank.identity.application.service.RoleService;
import com.bloodbank.identity.domain.entity.Permission;
import com.bloodbank.identity.domain.entity.Role;
import com.bloodbank.identity.domain.entity.RolePermission;
import com.bloodbank.identity.domain.repository.PermissionRepository;
import com.bloodbank.identity.domain.repository.RolePermissionRepository;
import com.bloodbank.identity.domain.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
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
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.findByName(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("Role code already exists: " + request.getCode());
        }

        Role role = new Role();
        role.setName(request.getCode());
        role.setDisplayName(request.getDisplayName());
        role.setDescription(request.getDescription());
        role.setCategory(request.getCategory() != null ? request.getCategory() : "GENERAL");
        role.setSystemRole(false);
        role.setEnabled(true);

        if (request.getParentRoleCode() != null && !request.getParentRoleCode().isBlank()) {
            Role parentRole = roleRepository.findByName(request.getParentRoleCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent role not found: " + request.getParentRoleCode()));
            role.setParentRole(parentRole);
        }

        Role savedRole = roleRepository.save(role);

        Set<Long> permIds = new HashSet<>();
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            List<Permission> permissions = permissionRepository.findAllById(request.getPermissionIds());
            List<RolePermission> rolePermissions = permissions.stream().map(p -> {
                RolePermission rp = new RolePermission();
                rp.setRole(savedRole);
                rp.setPermission(p);
                permIds.add(p.getId());
                return rp;
            }).toList();
            rolePermissionRepository.saveAll(rolePermissions);
        }

        log.info("Created new enterprise role [{}] ID [{}]", savedRole.getName(), savedRole.getId());
        return mapToResponse(savedRole, permIds);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        Set<Long> permIds = getRolePermissionIds(role.getId());
        return mapToResponse(role, permIds);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleByCode(String code) {
        Role role = roleRepository.findByName(code)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with code: " + code));
        Set<Long> permIds = getRolePermissionIds(role.getId());
        return mapToResponse(role, permIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return roles.stream().map(role -> {
            Set<Long> permIds = getRolePermissionIds(role.getId());
            return mapToResponse(role, permIds);
        }).toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public RoleResponse cloneRole(Long sourceRoleId, CloneRoleRequest request) {
        Role sourceRole = roleRepository.findById(sourceRoleId)
                .orElseThrow(() -> new ResourceNotFoundException("Source role not found with ID: " + sourceRoleId));

        if (roleRepository.findByName(request.getTargetRoleCode()).isPresent()) {
            throw new DuplicateResourceException("Target role code already exists: " + request.getTargetRoleCode());
        }

        Role newRole = new Role();
        newRole.setName(request.getTargetRoleCode());
        newRole.setDisplayName(request.getTargetDisplayName());
        newRole.setDescription(request.getTargetDescription() != null ? request.getTargetDescription() : sourceRole.getDescription());
        newRole.setCategory(sourceRole.getCategory());
        newRole.setSystemRole(false);
        newRole.setEnabled(true);
        newRole.setParentRole(sourceRole.getParentRole());

        Role savedRole = roleRepository.save(newRole);

        List<RolePermission> sourceRPs = rolePermissionRepository.findByRoleId(sourceRoleId);
        List<RolePermission> newRPs = sourceRPs.stream().map(srp -> {
            RolePermission rp = new RolePermission();
            rp.setRole(savedRole);
            rp.setPermission(srp.getPermission());
            return rp;
        }).toList();
        rolePermissionRepository.saveAll(newRPs);

        Set<Long> permIds = newRPs.stream().map(rp -> rp.getPermission().getId()).collect(Collectors.toSet());
        log.info("Cloned role [{}] into new target role [{}]", sourceRole.getName(), savedRole.getName());
        return mapToResponse(savedRole, permIds);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void copyPermissions(Long sourceRoleId, Long targetRoleId) {
        Role sourceRole = roleRepository.findById(sourceRoleId)
                .orElseThrow(() -> new ResourceNotFoundException("Source role not found with ID: " + sourceRoleId));

        Role targetRole = roleRepository.findById(targetRoleId)
                .orElseThrow(() -> new ResourceNotFoundException("Target role not found with ID: " + targetRoleId));

        List<RolePermission> sourceRPs = rolePermissionRepository.findByRoleId(sourceRoleId);
        Set<Long> existingTargetPermIds = rolePermissionRepository.findByRoleId(targetRoleId).stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());

        List<RolePermission> toAdd = sourceRPs.stream()
                .filter(srp -> !existingTargetPermIds.contains(srp.getPermission().getId()))
                .map(srp -> {
                    RolePermission rp = new RolePermission();
                    rp.setRole(targetRole);
                    rp.setPermission(srp.getPermission());
                    return rp;
                }).toList();

        rolePermissionRepository.saveAll(toAdd);
        log.info("Copied [{}] new permissions from role [{}] to role [{}]", toAdd.size(), sourceRole.getName(), targetRole.getName());
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void setRoleEnabled(Long roleId, boolean enabled) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        if (role.isSystemRole()) {
            throw new BusinessRuleViolationException("System roles cannot be disabled");
        }
        role.setEnabled(enabled);
        roleRepository.save(role);
        log.info("Role [{}] enabled status set to [{}]", role.getName(), enabled);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void updateRoleHierarchy(RoleHierarchyUpdateRequest request) {
        Map<String, String> map = request.getParentToChildMap();
        map.forEach((parentCode, childCode) -> {
            Role parentRole = roleRepository.findByName(parentCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent role not found: " + parentCode));
            Role childRole = roleRepository.findByName(childCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Child role not found: " + childCode));

            childRole.setParentRole(parentRole);
            roleRepository.save(childRole);
            log.info("Updated role hierarchy: [{}] is now parent of [{}]", parentCode, childCode);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getRoleHierarchyMap() {
        List<Role> roles = roleRepository.findAll();
        Map<String, String> result = new HashMap<>();
        roles.forEach(role -> {
            if (role.getParentRole() != null) {
                result.put(role.getParentRole().getName(), role.getName());
            }
        });
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public String buildSpringSecurityRoleHierarchyString() {
        Map<String, String> hierarchyMap = getRoleHierarchyMap();
        StringBuilder sb = new StringBuilder();
        hierarchyMap.forEach((parent, child) -> {
            if (!sb.isEmpty()) {
                sb.append(" \n ");
            }
            sb.append(parent).append(" > ").append(child);
        });
        return sb.toString();
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        if (role.isSystemRole()) {
            throw new BusinessRuleViolationException("System roles cannot be deleted");
        }
        roleRepository.delete(role);
        log.info("Soft-deleted role [{}] ID [{}]", role.getName(), id);
    }

    private Set<Long> getRolePermissionIds(Long roleId) {
        return rolePermissionRepository.findByRoleId(roleId).stream()
                .map(rp -> rp.getPermission().getId())
                .collect(Collectors.toSet());
    }

    private RoleResponse mapToResponse(Role role, Set<Long> permissionIds) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .displayName(role.getDisplayName() != null ? role.getDisplayName() : role.getName())
                .description(role.getDescription())
                .category(role.getCategory())
                .systemRole(role.isSystemRole())
                .enabled(role.isEnabled())
                .parentRoleCode(role.getParentRole() != null ? role.getParentRole().getName() : null)
                .permissionIds(permissionIds)
                .build();
    }
}
