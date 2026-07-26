package com.bloodbank.identity.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignRoleRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotEmpty(message = "Role IDs list cannot be empty")
    private Set<Long> roleIds;
}
