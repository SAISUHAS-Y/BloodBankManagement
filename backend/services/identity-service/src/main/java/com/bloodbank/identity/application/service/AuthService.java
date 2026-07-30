package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.request.ChangePasswordRequest;
import com.bloodbank.identity.application.dto.request.ForgotPasswordRequest;
import com.bloodbank.identity.application.dto.request.LoginRequest;
import com.bloodbank.identity.application.dto.request.RefreshTokenRequest;
import com.bloodbank.identity.application.dto.request.ResetPasswordRequest;
import com.bloodbank.identity.application.dto.request.VerifyEmailRequest;
import com.bloodbank.identity.application.dto.response.LoginResponse;

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
