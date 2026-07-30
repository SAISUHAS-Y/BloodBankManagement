package com.bloodbank.identity.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request model to complete 2-Step login authentication using TOTP or emergency backup code")
public class MfaVerifyRequest {

    @NotBlank(message = "MFA transaction token is required")
    @Schema(example = "mfa_challenge_77189a-0912384a", description = "Short-lived MFA challenge transaction token returned during initial login")
    private String mfaToken;

    @NotBlank(message = "Verification code is required")
    @Schema(example = "489123", description = "6-digit TOTP token OR 10-character single-use emergency backup code")
    private String code;
}
