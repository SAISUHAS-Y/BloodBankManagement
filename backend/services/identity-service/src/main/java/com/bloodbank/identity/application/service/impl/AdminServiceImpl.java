package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.common.events.user.UserRegisteredEvent;
import com.bloodbank.common.exception.BusinessRuleViolationException;
import com.bloodbank.common.exception.DuplicateResourceException;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.exception.enums.ErrorCode;
import com.bloodbank.common.security.audit.SecurityAuditLogger;
import com.bloodbank.identity.application.dto.request.AssignRoleRequest;
import com.bloodbank.identity.application.dto.request.RegisterUserRequest;
import com.bloodbank.identity.application.dto.request.RoleRequest;
import com.bloodbank.identity.application.dto.request.UserSearchRequest;
import com.bloodbank.identity.application.dto.response.PermissionResponse;
import com.bloodbank.identity.application.dto.response.RoleResponse;
import com.bloodbank.identity.application.event.UserEventPublisher;
import com.bloodbank.identity.application.service.AdminService;
import com.bloodbank.identity.domain.entity.AuthAuditLog;
import com.bloodbank.identity.domain.entity.Permission;
import com.bloodbank.identity.domain.entity.Role;
import com.bloodbank.identity.domain.entity.RolePermission;
import com.bloodbank.identity.domain.entity.User;
import com.bloodbank.identity.domain.entity.UserRole;
import com.bloodbank.identity.domain.enums.AuthEventType;
import com.bloodbank.identity.domain.repository.AuthAuditLogRepository;
import com.bloodbank.identity.domain.repository.PermissionRepository;
import com.bloodbank.identity.domain.repository.RolePermissionRepository;
import com.bloodbank.identity.domain.repository.RoleRepository;
import com.bloodbank.identity.domain.repository.UserRepository;
import com.bloodbank.identity.domain.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AuthAuditLogRepository authAuditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;
    private final SecurityAuditLogger securityAuditLogger;

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public UserSummaryResponse createUser(RegisterUserRequest request) {
        log.info("Creating new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new InvalidInputException("Username already in use");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidInputException("Email already in use");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setActive(true);
        user.setLocked(false);
        user.setMustChangePassword(false);
        user.setCreatedBy("ADMIN_PORTAL");
        user.setUpdatedBy("ADMIN_PORTAL");

        User savedUser = userRepository.save(user);
        Set<String> roles = new HashSet<>();

        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
            
            UserRole userRole = new UserRole();
            userRole.setUser(savedUser);
            userRole.setRole(role);
            userRole.setCreatedBy("ADMIN_PORTAL");
            userRole.setUpdatedBy("ADMIN_PORTAL");
            userRoleRepository.save(userRole);
            roles.add(role.getName());
        }

        Set<String> permissions = getUserPermissionCodes(savedUser);

        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .occurredAt(Instant.now())
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .build();
        
        userEventPublisher.publishUserRegistered(event, UUID.randomUUID().toString());
        writeAuditLog(savedUser.getId(), AuthEventType.LOGIN_SUCCESS, "ADMIN_PORTAL", "N/A", "{\"event\": \"USER_CREATED\", \"username\": \"" + savedUser.getUsername() + "\"}");

        return buildUserSummary(savedUser, roles, permissions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_summary", key = "#id")
    public UserSummaryResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        
        Set<String> roles = getUserRoleNames(user);
        Set<String> permissions = getUserPermissionCodes(user);
        
        return buildUserSummary(user, roles, permissions);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "user_summary", key = "#username")
    public UserSummaryResponse getUserSummaryByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        Set<String> roles = getUserRoleNames(user);
        Set<String> permissions = getUserPermissionCodes(user);

        return buildUserSummary(user, roles, permissions);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> {
                    Set<String> roles = getUserRoleNames(user);
                    Set<String> permissions = getUserPermissionCodes(user);
                    return buildUserSummary(user, roles, permissions);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.bloodbank.common.core.dto.PageResponse<UserSummaryResponse> searchUsers(UserSearchRequest request) {
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                "ASC".equalsIgnoreCase(request.getSortDirection()) ? org.springframework.data.domain.Sort.Direction.ASC : org.springframework.data.domain.Sort.Direction.DESC,
                request.getSortBy() != null ? request.getSortBy() : "createdAt"
        );

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(request.getPage(), request.getSize(), sort);
        org.springframework.data.jpa.domain.Specification<User> spec = com.bloodbank.identity.domain.repository.specification.UserSpecifications.buildSearchSpecification(request);

        org.springframework.data.domain.Page<User> userPage = userRepository.findAll(spec, pageable);

        return com.bloodbank.common.core.dto.PageResponse.from(userPage, user -> {
            Set<String> roles = getUserRoleNames(user);
            Set<String> permissions = getUserPermissionCodes(user);
            return buildUserSummary(user, roles, permissions);
        });
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.activate();
        userRepository.save(user);
        log.info("User [{}] status set to ACTIVE", user.getUsername());
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.deactivate();
        userRepository.save(user);
        log.info("User [{}] status set to DEACTIVATED", user.getUsername());
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void lockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.lockAccount(Instant.now().plus(24, java.time.temporal.ChronoUnit.HOURS));
        userRepository.save(user);
        log.info("User [{}] locked by admin", user.getUsername());
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.unlockAccount();
        userRepository.save(user);
        log.info("User [{}] unlocked by admin", user.getUsername());
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void suspendUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        user.suspend();
        userRepository.save(user);
        log.info("User [{}] status set to SUSPENDED", user.getUsername());
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        long remainingAdmins = userRoleRepository.countActiveUsersWithPermission("USER_MANAGE");
        if (remainingAdmins <= 1 && getUserPermissionCodes(user).contains("USER_MANAGE")) {
            throw new BusinessRuleViolationException("Cannot delete user as it would leave zero active users with administrative privileges.", ErrorCode.LAST_ADMIN_REVOCATION_PREVENTED);
        }

        user.delete();
        userRepository.save(user);
        writeAuditLog(id, AuthEventType.LOGOUT, "ADMIN_PORTAL", "N/A", "{\"event\": \"USER_DELETED\", \"userId\": " + id + "}");
        log.info("Soft-deleted user with ID: {}", id);
    }

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByName(request.getName())) {
            throw new InvalidInputException("Role name already exists");
        }

        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setSystemRole(false);
        role.setCreatedBy("ADMIN_PORTAL");
        role.setUpdatedBy("ADMIN_PORTAL");

        Role saved = roleRepository.save(role);
        return RoleResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .description(saved.getDescription())
                .systemRole(saved.isSystemRole())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> RoleResponse.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .description(role.getDescription())
                        .systemRole(role.isSystemRole())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        if (role.isSystemRole()) {
            throw new InvalidInputException("Cannot delete system role: " + role.getName());
        }

        if (userRoleRepository.existsByRoleId(id)) {
            log.warn("Attempted to delete role ID {} which is currently assigned to users.", id);
            throw new DuplicateResourceException("Role cannot be deleted while assigned to active users.", ErrorCode.ROLE_IN_USE);
        }

        roleRepository.delete(role);
        writeAuditLog(null, AuthEventType.LOGOUT, "ADMIN_PORTAL", "N/A", "{\"event\": \"ROLE_DELETED\", \"roleName\": \"" + role.getName() + "\"}");
        log.info("Deleted role with ID: {}", id);
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void assignRolesToUser(AssignRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        userRoleRepository.deleteByUserId(user.getId());

        for (Long roleId : request.getRoleIds()) {
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

            UserRole ur = new UserRole();
            ur.setUser(user);
            ur.setRole(role);
            ur.setCreatedBy("ADMIN_PORTAL");
            ur.setUpdatedBy("ADMIN_PORTAL");
            userRoleRepository.save(ur);
        }

        long activeAdmins = userRoleRepository.countActiveUsersWithPermission("USER_MANAGE");
        if (activeAdmins == 0) {
            throw new BusinessRuleViolationException("Cannot complete role revocation as it leaves zero active users with administrative privileges.", ErrorCode.LAST_ADMIN_REVOCATION_PREVENTED);
        }

        writeAuditLog(user.getId(), AuthEventType.LOGIN_SUCCESS, "ADMIN_PORTAL", "N/A", "{\"event\": \"ROLES_ASSIGNED\", \"targetUserId\": " + user.getId() + "}");
        securityAuditLogger.logSecurityEvent("ROLE_ASSIGNED", "ADMIN_PORTAL", "Roles updated for user ID: " + user.getId(), "N/A");
        log.info("Updated roles for user ID: {}", request.getUserId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(perm -> PermissionResponse.builder()
                        .id(perm.getId())
                        .code(perm.getCode())
                        .description(perm.getDescription())
                        .module(perm.getModule())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "user_summary", allEntries = true)
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        rolePermissionRepository.deleteByRoleId(role.getId());

        for (Long permId : permissionIds) {
            Permission perm = permissionRepository.findById(permId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permission not found with ID: " + permId));

            RolePermission rp = new RolePermission();
            rp.setRole(role);
            rp.setPermission(perm);
            rp.setGrantedAt(Instant.now());
            rp.setGrantedBy("ADMIN_PORTAL");
            rolePermissionRepository.save(rp);
        }

        long activeAdmins = userRoleRepository.countActiveUsersWithPermission("USER_MANAGE");
        if (activeAdmins == 0) {
            throw new BusinessRuleViolationException("Cannot complete permission revocation as it leaves zero active users with administrative privileges.", ErrorCode.LAST_ADMIN_REVOCATION_PREVENTED);
        }

        writeAuditLog(null, AuthEventType.LOGIN_SUCCESS, "ADMIN_PORTAL", "N/A", "{\"event\": \"PERMISSIONS_ASSIGNED\", \"roleId\": " + roleId + "}");
        securityAuditLogger.logSecurityEvent("PERMISSION_GRANTED", "ADMIN_PORTAL", "Permissions updated for role ID: " + roleId, "N/A");
        log.info("Updated permissions for role ID: {}", roleId);
    }

    private void writeAuditLog(Long userId, AuthEventType eventType, String ipAddress, String userAgent, String metadata) {
        AuthAuditLog logEntity = new AuthAuditLog();
        logEntity.setUserId(userId);
        logEntity.setEventType(eventType);
        logEntity.setIpAddress(ipAddress);
        logEntity.setUserAgent(userAgent);
        logEntity.setOccurredAt(Instant.now());
        logEntity.setMetadata(metadata);
        authAuditLogRepository.save(logEntity);
    }

    private Set<String> getUserRoleNames(User user) {
        return userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }

    private Set<String> getUserPermissionCodes(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        if (userRoles.isEmpty()) {
            return Set.of();
        }
        Set<Long> roleIds = userRoles.stream()
                .map(ur -> ur.getRole().getId())
                .collect(Collectors.toSet());

        return rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toSet());
    }

    private UserSummaryResponse buildUserSummary(User user, Set<String> roles, Set<String> permissions) {
        String fullName = user.getFullName();
        String firstName = "";
        String lastName = "";
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) {
                lastName = parts[1];
            }
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .permissions(permissions)
                .active(user.isActive())
                .build();
    }
}
