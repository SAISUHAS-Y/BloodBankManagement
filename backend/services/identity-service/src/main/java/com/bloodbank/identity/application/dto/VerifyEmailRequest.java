package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Payload to confirm email address ownership using verification token")
public class VerifyEmailRequest {

    @NotBlank(message = "Verification token is required")
    @Schema(example = "evt_8912a10-4b92-4112", description = "Email verification token string")
    private String token;
}
