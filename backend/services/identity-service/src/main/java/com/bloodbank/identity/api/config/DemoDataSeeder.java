package com.bloodbank.identity.api.config;

import com.bloodbank.identity.domain.entity.Permission;
import com.bloodbank.identity.domain.entity.Role;
import com.bloodbank.identity.domain.entity.RolePermission;
import com.bloodbank.identity.domain.entity.User;
import com.bloodbank.identity.domain.entity.UserRole;
import com.bloodbank.identity.domain.repository.PermissionRepository;
import com.bloodbank.identity.domain.repository.RolePermissionRepository;
import com.bloodbank.identity.domain.repository.RoleRepository;
import com.bloodbank.identity.domain.repository.UserRepository;
import com.bloodbank.identity.domain.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Ensures demo user accounts (admin, hospital_staff, bank_staff, donor_user)
 * have verified active status, known password 'Password123!', and full permissions
 * across all roles so all features can be tested without 403 Forbidden errors.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDemoAccounts() {
        log.info("Checking and initializing demo user accounts...");

        String encodedPassword = passwordEncoder.encode("Password123!");

        // 1. Seed demo users
        seedUser("admin", "admin@bloodbank.org", "System Administrator", encodedPassword);
        seedUser("hospital_staff", "staff@cityhospital.org", "Hospital Staff Member", encodedPassword);
        seedUser("bank_staff", "operator@bloodcenter.org", "Blood Bank Operator", encodedPassword);
        seedUser("donor_user", "donor@bloodbank.org", "Voluntary Donor User", encodedPassword);

        // 2. Map all permissions to all roles to ensure zero 403 authorization blocks
        ensureAllPermissionsForRoles();

        log.info("Demo user accounts and permissions initialized successfully.");
    }

    private void seedUser(String username, String email, String fullName, String passwordHash) {
        User user = userRepository.findByUsername(username).orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            return newUser;
        });

        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordHash);
        user.setActive(true);
        user.setLocked(false);
        user.setMustChangePassword(false);
        user.setFailedLoginAttempts(0);
        user.setCreatedBy("SYSTEM");
        user.setUpdatedBy("SYSTEM");

        user = userRepository.save(user);

        // Assign SUPER_ADMIN role (ID 1)
        Optional<Role> roleOpt = roleRepository.findByName("SUPER_ADMIN");
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            List<UserRole> existingRoles = userRoleRepository.findByUserId(user.getId());
            boolean hasRole = existingRoles.stream().anyMatch(ur -> ur.getRole() != null && role.getId().equals(ur.getRole().getId()));
            if (!hasRole) {
                UserRole ur = new UserRole();
                ur.setUser(user);
                ur.setRole(role);
                ur.setCreatedBy("SYSTEM");
                ur.setUpdatedBy("SYSTEM");
                userRoleRepository.save(ur);
            }
        }
    }

    private void ensureAllPermissionsForRoles() {
        List<Role> roles = roleRepository.findAll();
        List<Permission> permissions = permissionRepository.findAll();

        for (Role role : roles) {
            List<RolePermission> existingRolePermissions = rolePermissionRepository.findByRole(role);
            for (Permission perm : permissions) {
                boolean exists = existingRolePermissions.stream()
                        .anyMatch(rp -> rp.getPermission() != null && perm.getId().equals(rp.getPermission().getId()));
                if (!exists) {
                    RolePermission rp = new RolePermission();
                    rp.setRole(role);
                    rp.setPermission(perm);
                    rp.setGrantedBy("SYSTEM");
                    rp.setCreatedBy("SYSTEM");
                    rp.setUpdatedBy("SYSTEM");
                    rolePermissionRepository.save(rp);
                }
            }
        }
    }
}
