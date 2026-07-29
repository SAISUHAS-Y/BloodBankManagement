package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.identity.application.dto.LoginResponse;
import com.bloodbank.identity.application.dto.SsoLoginRequest;
import com.bloodbank.identity.application.service.SsoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/sso")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Enterprise SSO Authentication", description = "Endpoints for OAuth2/OIDC Single Sign-On using Google, Microsoft, and GitHub")
public class SsoController {

    private final SsoService ssoService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate via Enterprise SSO", description = "Validates Identity Provider ID token and returns JWT credentials with safe auto-provisioning")
    public ResponseEntity<ApiResponse<LoginResponse>> authenticateSso(
            @Valid @RequestBody SsoLoginRequest request,
            HttpServletRequest httpServletRequest) {

        String ipAddress = extractClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");

        LoginResponse response = ssoService.authenticateSso(request, ipAddress, userAgent);
        return ResponseEntity.ok(ApiResponse.success("SSO login successful", response));
    }

    private String extractClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
