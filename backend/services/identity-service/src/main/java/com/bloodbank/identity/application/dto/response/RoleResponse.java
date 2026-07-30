package com.bloodbank.identity.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name;
    private String displayName;
    private String description;
    private String category;
    private boolean systemRole;
    private boolean enabled;
    private String parentRoleCode;
    private Set<Long> permissionIds;
}
