package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request model to confirm identity and disable MFA")
public class MfaDisableRequest {

    @NotBlank(message = "Password confirmation is required")
    @Schema(example = "SecretP@ssword123", description = "Account password to authorize disabling 2FA")
    private String password;

    @NotBlank(message = "TOTP token code is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "TOTP code must be a 6-digit numeric token")
    @Schema(example = "489123", description = "Current 6-digit TOTP token")
    private String totpCode;
}
