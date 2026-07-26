package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Schema(description = "Request model for creating a custom enterprise role")
public class CreateRoleRequest {

    @NotBlank(message = "Role code is required")
    @Pattern(regexp = "^ROLE_[A-Z0-9_]{3,45}$", message = "Role code must start with 'ROLE_' followed by uppercase letters, numbers, or underscores")
    @Schema(example = "ROLE_LAB_SUPERVISOR", description = "Unique system identifier for the role")
    private String code;

    @NotBlank(message = "Role display name is required")
    @Size(min = 2, max = 100, message = "Role display name must be between 2 and 100 characters")
    @Schema(example = "Laboratory Supervisor", description = "Human-readable role title")
    private String displayName;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    @Schema(example = "Manages blood testing procedures and sample release", description = "Role description")
    private String description;

    @NotBlank(message = "Role category is required")
    @Schema(example = "LABORATORY", description = "Functional classification (e.g., MEDICAL, ADMIN, LABORATORY, DONOR)")
    private String category = "GENERAL";

    @Schema(example = "ROLE_LAB_TECHNICIAN", description = "Optional parent role code to inherit permissions from")
    private String parentRoleCode;

    @Schema(example = "[10, 11, 12, 15]", description = "Initial list of permission IDs assigned to role")
    private Set<Long> permissionIds;
}
