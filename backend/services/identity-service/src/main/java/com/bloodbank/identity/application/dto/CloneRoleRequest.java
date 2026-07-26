package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request model for cloning an existing role into a new target role")
public class CloneRoleRequest {

    @NotBlank(message = "Target role code is required")
    @Pattern(regexp = "^ROLE_[A-Z0-9_]{3,45}$", message = "Role code must start with 'ROLE_' followed by uppercase alphanumeric characters")
    @Schema(example = "ROLE_REGIONAL_LAB_SUPERVISOR", description = "New role code to create")
    private String targetRoleCode;

    @NotBlank(message = "Target role display name is required")
    @Size(min = 2, max = 100, message = "Display name must be between 2 and 100 characters")
    @Schema(example = "Regional Lab Supervisor", description = "Display name for cloned role")
    private String targetDisplayName;

    @Schema(example = "Cloned from ROLE_LAB_SUPERVISOR for regional facility", description = "Description for cloned role")
    private String targetDescription;
}
