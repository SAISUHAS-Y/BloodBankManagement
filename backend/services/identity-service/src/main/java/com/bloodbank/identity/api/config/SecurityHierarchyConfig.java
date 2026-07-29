package com.bloodbank.identity.api.config;

import com.bloodbank.identity.application.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityHierarchyConfig {

    private final RoleService roleService;

    @Bean
    public RoleHierarchy roleHierarchy() {
        try {
            String hierarchyString = roleService.buildSpringSecurityRoleHierarchyString();
            if (hierarchyString != null && !hierarchyString.isBlank()) {
                log.info("Initialized Spring Security RoleHierarchy Engine with rules:\n{}", hierarchyString);
                return RoleHierarchyImpl.fromHierarchy(hierarchyString);
            }
        } catch (Exception e) {
            log.warn("Could not load dynamic role hierarchy at startup (tables may be uninitialized): {}", e.getMessage());
        }
        return RoleHierarchyImpl.fromHierarchy("");
    }
}
