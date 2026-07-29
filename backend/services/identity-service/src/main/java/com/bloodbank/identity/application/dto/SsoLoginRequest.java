package com.bloodbank.identity.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request model for Enterprise SSO OAuth2/OIDC authentication")
public class SsoLoginRequest {

    @NotBlank(message = "SSO provider is required")
    @Schema(example = "GOOGLE", description = "OAuth2/OIDC Identity Provider (e.g., GOOGLE, MICROSOFT, GITHUB)")
    private String provider;

    @NotBlank(message = "SSO ID token or authorization code is required")
    @Schema(example = "eyJhbGciOiJSUzI1NiIsImtpZCI6...", description = "Signed JWT ID token from Identity Provider")
    private String idToken;
}
