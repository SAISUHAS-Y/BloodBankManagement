package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Full Enterprise Role-Permission Matrix Grid Payload")
public class PermissionMatrixResponse {

    @Schema(description = "List of all defined roles")
    private List<RoleSummary> roles;

    @Schema(description = "List of all system modules grouping permissions")
    private List<String> modules;

    @Schema(description = "List of all system permission details grouped by module")
    private Map<String, List<PermissionResponse>> permissionsByModule;

    @Schema(
        example = "{\"ROLE_SUPER_ADMIN\": [1, 2, 3, 4], \"ROLE_DOCTOR\": [1, 5]}",
        description = "Grid mapping role code to set of assigned permission IDs"
    )
    private Map<String, Set<Long>> matrix;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RoleSummary {
        private Long id;
        private String code;
        private String displayName;
        private String category;
        private boolean systemRole;
        private boolean enabled;
        private String parentRoleCode;
    }
}
