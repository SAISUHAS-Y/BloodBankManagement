package com.bloodbank.identity.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Identity & Security Service API",
                version = "1.0",
                description = "REST APIs for User Authentication, Account Governance, Roles, Permissions, MFA, and Security Audit"
        ),
        tags = {
                @Tag(name = "User Authentication & Login", description = "Simple endpoints for logging in, logging out, resetting passwords, and verifying emails"),
                @Tag(name = "User Account Management", description = "Simple endpoints for creating, searching, locking, activating, and managing user accounts"),
                @Tag(name = "Roles Management", description = "Simple endpoints for creating custom roles, enabling/disabling roles, and managing role hierarchy"),
                @Tag(name = "Permissions Management", description = "Simple endpoints for viewing permissions, modules, and the role-permission matrix grid"),
                @Tag(name = "Two-Factor Authentication (2FA)", description = "Simple endpoints for turning on 2FA (Authenticator App), getting backup codes, and turning off 2FA"),
                @Tag(name = "Active Device Sessions", description = "Simple endpoints for viewing logged-in devices and logging out specific or all other devices"),
                @Tag(name = "Security Audit Logs", description = "Simple endpoints for searching login security audit logs and viewing security activity statistics"),
                @Tag(name = "Security Keys (JWKS)", description = "Public security key endpoint used by system components to verify login tokens")
        }
)
public class OpenApiConfig {
}
