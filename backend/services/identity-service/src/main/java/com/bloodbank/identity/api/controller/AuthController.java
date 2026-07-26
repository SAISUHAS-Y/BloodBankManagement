package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.identity.application.dto.ChangePasswordRequest;
import com.bloodbank.identity.application.dto.LoginRequest;
import com.bloodbank.identity.application.dto.LoginResponse;
import com.bloodbank.identity.application.dto.RefreshTokenRequest;
import com.bloodbank.identity.application.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpServletRequest) {
        
        String ipAddress = getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");
        
        LoginResponse response = authService.login(request, ipAddress, userAgent);
        
        return new ResponseEntity<>(
                ApiResponse.success("Login successful", response),
                HttpStatus.OK
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpServletRequest) {

        String ipAddress = getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");

        LoginResponse response = authService.refreshToken(request, ipAddress, userAgent);

        return new ResponseEntity<>(
                ApiResponse.success("Token refreshed successfully", response),
                HttpStatus.OK
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        authService.logout(request.getRefreshToken());
        
        return new ResponseEntity<>(
                ApiResponse.success("Logout successful", null),
                HttpStatus.OK
        );
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll(Principal principal) {
        if (principal == null) {
            return new ResponseEntity<>(
                    ApiResponse.failure(HttpStatus.UNAUTHORIZED.value(), "Unauthorized action", "ERR_UNAUTHORIZED_ACTION"),
                    HttpStatus.UNAUTHORIZED
            );
        }
        authService.logoutAll(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("All sessions logged out successfully", null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Principal principal) {
        
        if (principal == null) {
            return new ResponseEntity<>(
                    ApiResponse.failure(HttpStatus.UNAUTHORIZED.value(), "Unauthorized action", "ERR_UNAUTHORIZED_ACTION"),
                    HttpStatus.UNAUTHORIZED
            );
        }

        authService.changePassword(principal.getName(), request);

        return new ResponseEntity<>(
                ApiResponse.success("Password changed successfully. All other sessions have been terminated.", null),
                HttpStatus.OK
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody com.bloodbank.identity.application.dto.ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If the email is registered, a password reset token has been dispatched", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody com.bloodbank.identity.application.dto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully", null));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody com.bloodbank.identity.application.dto.VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success("Email address verified successfully", null));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
