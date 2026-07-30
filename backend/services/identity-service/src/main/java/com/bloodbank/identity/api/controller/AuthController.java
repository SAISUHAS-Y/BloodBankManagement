package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.identity.application.dto.request.ChangePasswordRequest;
import com.bloodbank.identity.application.dto.request.ForgotPasswordRequest;
import com.bloodbank.identity.application.dto.request.LoginRequest;
import com.bloodbank.identity.application.dto.request.MfaVerifyRequest;
import com.bloodbank.identity.application.dto.request.RefreshTokenRequest;
import com.bloodbank.identity.application.dto.request.ResetPasswordRequest;
import com.bloodbank.identity.application.dto.request.VerifyEmailRequest;
import com.bloodbank.identity.application.dto.response.LoginResponse;
import com.bloodbank.identity.application.service.AuthService;
import com.bloodbank.identity.application.service.MfaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User Authentication & Login", description = "Simple endpoints for logging in, logging out, resetting passwords, and verifying emails")
public class AuthController {

    private final AuthService authService;
    private final MfaService mfaService;

    @PostMapping("/login")
    @Operation(summary = "Login to Account", description = "Enter username and password to log in")
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

    @PostMapping("/verify-mfa")
    @Operation(summary = "Verify 2-Factor Authentication Code", description = "Enter 6-digit 2FA code or backup code to complete login")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyMfa(
            @Valid @RequestBody MfaVerifyRequest request,
            HttpServletRequest httpServletRequest) {

        String ipAddress = getClientIp(httpServletRequest);
        String userAgent = httpServletRequest.getHeader("User-Agent");

        LoginResponse response = mfaService.verifyMfaAndCompleteLogin(request, ipAddress, userAgent);

        return new ResponseEntity<>(
                ApiResponse.success("MFA verification successful", response),
                HttpStatus.OK
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Login Session", description = "Get a new login token when your current token expires")
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
    @Operation(summary = "Logout Current Device", description = "Log out from current device session")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        
        authService.logout(request.getRefreshToken());
        
        return new ResponseEntity<>(
                ApiResponse.success("Logout successful", null),
                HttpStatus.OK
        );
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout From All Devices", description = "Log out from all devices at once")
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
    @Operation(summary = "Change My Password", description = "Change your password (cannot reuse previous 5 passwords)")
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
    @Operation(summary = "Send Password Reset Email", description = "Request a password reset link to your email")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If the email is registered, a password reset token has been dispatched", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset Password Using Link", description = "Set a new password using the reset link token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully", null));
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify Email Address", description = "Confirm and verify your email address")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
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
