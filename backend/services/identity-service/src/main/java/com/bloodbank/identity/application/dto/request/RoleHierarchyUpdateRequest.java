package com.bloodbank.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Schema(description = "Payload for configuring role inheritance mapping tree")
public class RoleHierarchyUpdateRequest {

    @NotEmpty(message = "Role hierarchy mapping cannot be empty")
    @Schema(
        example = "{\"ROLE_SUPER_ADMIN\": \"ROLE_BLOODBANK_ADMIN\", \"ROLE_BLOODBANK_ADMIN\": \"ROLE_DOCTOR\"}",
        description = "Key-value pair mapping parent role code to immediate child role code"
    )
    private Map<String, String> parentToChildMap;
}
