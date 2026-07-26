package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.ChangePasswordRequest;
import com.bloodbank.identity.application.dto.LoginRequest;
import com.bloodbank.identity.application.dto.LoginResponse;
import com.bloodbank.identity.application.dto.RefreshTokenRequest;

import com.bloodbank.identity.application.dto.ForgotPasswordRequest;
import com.bloodbank.identity.application.dto.ResetPasswordRequest;
import com.bloodbank.identity.application.dto.VerifyEmailRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);
    LoginResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent);
    void logout(String refreshTokenStr);
    void logoutAll(String username);
    void changePassword(String username, ChangePasswordRequest request);

    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
    void verifyEmail(VerifyEmailRequest request);
}
