package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.common.exception.AccountLockedException;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.exception.UnauthorizedActionException;
import com.bloodbank.common.security.audit.SecurityAuditLogger;
import com.bloodbank.common.security.jwt.JwtTokenProvider;
import com.bloodbank.identity.application.dto.request.ChangePasswordRequest;
import com.bloodbank.identity.application.dto.request.ForgotPasswordRequest;
import com.bloodbank.identity.application.dto.request.LoginRequest;
import com.bloodbank.identity.application.dto.request.RefreshTokenRequest;
import com.bloodbank.identity.application.dto.request.ResetPasswordRequest;
import com.bloodbank.identity.application.dto.request.VerifyEmailRequest;
import com.bloodbank.identity.application.dto.response.LoginResponse;
import com.bloodbank.identity.application.service.AuthService;
import com.bloodbank.identity.application.service.MfaService;
import com.bloodbank.identity.domain.entity.AuthAuditLog;
import com.bloodbank.identity.domain.entity.PasswordHistory;
import com.bloodbank.identity.domain.entity.RefreshToken;
import com.bloodbank.identity.domain.entity.User;
import com.bloodbank.identity.domain.entity.UserRole;
import com.bloodbank.identity.domain.entity.VerificationToken;
import com.bloodbank.identity.domain.enums.AuthEventType;
import com.bloodbank.identity.domain.repository.AuthAuditLogRepository;
import com.bloodbank.identity.domain.repository.PasswordHistoryRepository;
import com.bloodbank.identity.domain.repository.RefreshTokenRepository;
import com.bloodbank.identity.domain.repository.RolePermissionRepository;
import com.bloodbank.identity.domain.repository.UserMfaSecretRepository;
import com.bloodbank.identity.domain.repository.UserRepository;
import com.bloodbank.identity.domain.repository.UserRoleRepository;
import com.bloodbank.identity.domain.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthAuditLogRepository authAuditLogRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final PasswordHistoryRepository passwordHistoryRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserMfaSecretRepository mfaSecretRepository;
    private final MfaService mfaService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final SecurityAuditLogger securityAuditLogger;

    @Value("${app.security.jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${app.security.jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        log.info("Attempting login for user: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    writeAuditLog(null, AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                            "{\"error\": \"User not found\"}");
                    return new InvalidInputException("Invalid username or password");
                });

        if (!user.isActive()) {
            writeAuditLog(user.getId(), AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                    "{\"error\": \"Account is inactive\"}");
            throw new InvalidInputException("Your account is inactive. Please contact administrator.");
        }

        // 1. Account Lockout Check
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            writeAuditLog(user.getId(), AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                    "{\"error\": \"Account locked until " + user.getLockedUntil() + "\"}");
            securityAuditLogger.logSecurityEvent("ACCOUNT_LOCKED", user.getUsername(),
                    "Attempted login while account locked", ipAddress);
            throw new AccountLockedException(
                    "Account locked due to consecutive failed login attempts. Try again later.");
        } else if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(Instant.now())) {
            // Lock window expired, unlock account automatically
            user.setLocked(false);
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
        } else if (user.isLocked()) {
            writeAuditLog(user.getId(), AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                    "{\"error\": \"Account is locked\"}");
            throw new AccountLockedException("Your account is locked. Please contact administrator.");
        }

        // 2. Password Verification & Lockout Tracking
        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (passwordMatches) {
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            user.setLocked(false);
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);

            // MFA Challenge Check
            if (mfaSecretRepository.existsByUserIdAndMfaEnabledTrue(user.getId())) {
                String mfaToken = mfaService.generateMfaTransactionToken(user.getId(), user.getUsername());
                writeAuditLog(user.getId(), AuthEventType.LOGIN_SUCCESS, ipAddress, userAgent,
                        "{\"status\": \"Primary credentials verified, MFA challenge issued\"}");
                return LoginResponse.builder()
                        .mfaRequired(true)
                        .mfaToken(mfaToken)
                        .build();
            }
        } else {
            int failedAttempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(failedAttempts);

            if (failedAttempts >= 5) {
                user.setLocked(true);
                user.setLockedUntil(Instant.now().plus(15, ChronoUnit.MINUTES));
                userRepository.save(user);
                writeAuditLog(user.getId(), AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                        "{\"error\": \"5 failed login attempts. Account locked for 15 minutes.\"}");
                securityAuditLogger.logSecurityEvent("ACCOUNT_LOCKED", user.getUsername(),
                        "5 consecutive failed login attempts", ipAddress);
                throw new AccountLockedException(
                        "Account locked due to 5 consecutive failed login attempts. Try again later.");
            }

            userRepository.save(user);
            writeAuditLog(user.getId(), AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                    "{\"error\": \"Incorrect password\", \"attempts\": " + failedAttempts + "}");
            throw new InvalidInputException("Invalid username or password");
        }

        Set<String> roles = getUserRoleNames(user);
        Set<String> permissions = getUserPermissionCodes(user);

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), roles, permissions);

        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(Instant.now().plus(refreshExpirationMs, ChronoUnit.MILLIS));
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        writeAuditLog(user.getId(), AuthEventType.LOGIN_SUCCESS, ipAddress, userAgent, "{\"status\": \"Success\"}");

        UserSummaryResponse userSummary = buildUserSummary(user, roles, permissions);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(accessExpirationMs)
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request, String ipAddress, String userAgent) {
        String rawToken = request.getRefreshToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidInputException("Invalid refresh token"));

        if (existingToken.isRevoked()) {
            List<RefreshToken> activeTokens = refreshTokenRepository
                    .findByUserIdAndRevokedFalse(existingToken.getUserId());
            activeTokens.forEach(t -> {
                t.setRevoked(true);
                t.setRevokedAt(Instant.now());
            });
            refreshTokenRepository.saveAll(activeTokens);

            writeAuditLog(existingToken.getUserId(), AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                    "{\"error\": \"TOKEN_REUSE_DETECTED: Replay of revoked refresh token! All user sessions revoked.\"}");
            securityAuditLogger.logSecurityEvent("TOKEN_REUSE_DETECTED", "User-" + existingToken.getUserId(),
                    "Revoked refresh token replayed! All active sessions revoked.", ipAddress);
            throw new UnauthorizedActionException(
                    "Security alert: Revoked refresh token reuse detected. All active sessions have been revoked.");
        }

        if (existingToken.getExpiresAt().isBefore(Instant.now())) {
            existingToken.setRevoked(true);
            existingToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(existingToken);
            writeAuditLog(existingToken.getUserId(), AuthEventType.LOGIN_FAILED, ipAddress, userAgent,
                    "{\"error\": \"Expired refresh token\"}");
            throw new InvalidInputException("Expired refresh token. Please log in again.");
        }

        existingToken.setRevoked(true);
        existingToken.setRevokedAt(Instant.now());

        String newRawToken = UUID.randomUUID().toString();
        String newHash = hashToken(newRawToken);
        existingToken.setReplacedByToken(newHash);
        refreshTokenRepository.save(existingToken);

        RefreshToken newToken = new RefreshToken();
        newToken.setUserId(existingToken.getUserId());
        newToken.setTokenHash(newHash);
        newToken.setExpiresAt(Instant.now().plus(refreshExpirationMs, ChronoUnit.MILLIS));
        newToken.setRevoked(false);
        refreshTokenRepository.save(newToken);

        User user = userRepository.findById(existingToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<String> roles = getUserRoleNames(user);
        Set<String> permissions = getUserPermissionCodes(user);

        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), roles, permissions);
        writeAuditLog(user.getId(), AuthEventType.TOKEN_REFRESHED, ipAddress, userAgent, "{\"status\": \"Rotated\"}");

        UserSummaryResponse userSummary = buildUserSummary(user, roles, permissions);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawToken)
                .tokenType("Bearer")
                .expiresInMs(accessExpirationMs)
                .user(userSummary)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshTokenStr) {
        String tokenHash = hashToken(refreshTokenStr);
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);
        if (token != null && !token.isRevoked()) {
            token.setRevoked(true);
            token.setRevokedAt(Instant.now());
            refreshTokenRepository.save(token);
            writeAuditLog(token.getUserId(), AuthEventType.LOGOUT, "N/A", "N/A", "{\"status\": \"Successful logout\"}");
        }
    }

    @Override
    @Transactional
    public void logoutAll(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        activeTokens.forEach(t -> {
            t.setRevoked(true);
            t.setRevokedAt(Instant.now());
        });
        refreshTokenRepository.saveAll(activeTokens);

        writeAuditLog(user.getId(), AuthEventType.LOGOUT, "N/A", "N/A",
                "{\"status\": \"Logout all sessions executed\"}");
        securityAuditLogger.logSecurityEvent("LOGOUT_ALL", username, "All active sessions revoked by user request",
                "N/A");
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidInputException("Current password does not match");
        }

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < 8) {
            throw new InvalidInputException("New password must be at least 8 characters long");
        }

        List<PasswordHistory> pastPasswords = passwordHistoryRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        List<PasswordHistory> recentPasswords = pastPasswords.stream().limit(5).toList();
        for (PasswordHistory history : recentPasswords) {
            if (passwordEncoder.matches(newPassword, history.getPasswordHash())) {
                throw new InvalidInputException("Password matches a recently used password. Please choose a different password.");
            }
        }
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new InvalidInputException("New password cannot be identical to current password.");
        }

        String newPasswordHash = passwordEncoder.encode(newPassword);
        user.setPasswordHash(newPasswordHash);
        user.setMustChangePassword(false);
        userRepository.save(user);

        PasswordHistory historyRecord = new PasswordHistory();
        historyRecord.setUserId(user.getId());
        historyRecord.setPasswordHash(newPasswordHash);
        passwordHistoryRepository.save(historyRecord);

        List<RefreshToken> activeTokens = refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId());
        activeTokens.forEach(t -> {
            t.setRevoked(true);
            t.setRevokedAt(Instant.now());
        });
        refreshTokenRepository.saveAll(activeTokens);

        writeAuditLog(user.getId(), AuthEventType.PASSWORD_CHANGED, "N/A", "N/A",
                "{\"status\": \"Password updated, all sessions terminated\"}");
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            log.info("Forgot password requested for non-existent email: {}", request.getEmail());
            return;
        }

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        VerificationToken token = new VerificationToken();
        token.setUserId(user.getId());
        token.setTokenHash(tokenHash);
        token.setTokenType("PASSWORD_RESET");
        token.setTargetDestination(user.getEmail());
        token.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        verificationTokenRepository.save(token);

        log.info("Generated password reset token [{}] for user [{}]", rawToken, user.getUsername());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        String tokenHash = hashToken(request.getToken());
        VerificationToken vToken = verificationTokenRepository.findByTokenHashAndTokenType(tokenHash, "PASSWORD_RESET")
                .orElseThrow(() -> new InvalidInputException("Invalid or expired password reset token"));

        if (vToken.isUsed() || vToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidInputException("Password reset token has expired or already been used");
        }

        User user = userRepository.findById(vToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String newPasswordHash = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(newPasswordHash);
        user.setMustChangePassword(false);
        userRepository.save(user);

        vToken.setUsed(true);
        verificationTokenRepository.save(vToken);

        PasswordHistory historyRecord = new PasswordHistory();
        historyRecord.setUserId(user.getId());
        historyRecord.setPasswordHash(newPasswordHash);
        passwordHistoryRepository.save(historyRecord);

        log.info("Password reset successfully for user [{}]", user.getUsername());
    }

    @Override
    @Transactional
    public void verifyEmail(VerifyEmailRequest request) {
        String tokenHash = hashToken(request.getToken());
        VerificationToken vToken = verificationTokenRepository.findByTokenHashAndTokenType(tokenHash, "EMAIL_VERIFICATION")
                .orElseThrow(() -> new InvalidInputException("Invalid email verification token"));

        if (vToken.isUsed() || vToken.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidInputException("Verification token has expired or already been used");
        }

        User user = userRepository.findById(vToken.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        vToken.setUsed(true);
        verificationTokenRepository.save(vToken);

        log.info("Email verified successfully for user [{}]", user.getUsername());
    }

    private void writeAuditLog(Long userId, AuthEventType eventType, String ipAddress, String userAgent,
            String metadata) {
        AuthAuditLog logEntity = new AuthAuditLog();
        logEntity.setUserId(userId);
        logEntity.setEventType(eventType);
        logEntity.setIpAddress(ipAddress);
        logEntity.setUserAgent(userAgent);
        logEntity.setOccurredAt(Instant.now());
        logEntity.setMetadata(metadata);
        authAuditLogRepository.save(logEntity);
    }

    private Set<String> getUserRoleNames(User user) {
        return userRoleRepository.findByUserId(user.getId()).stream()
                .map(ur -> ur.getRole().getName())
                .collect(Collectors.toSet());
    }

    private Set<String> getUserPermissionCodes(User user) {
        List<UserRole> userRoles = userRoleRepository.findByUserId(user.getId());
        if (userRoles.isEmpty()) {
            return Set.of();
        }
        Set<Long> roleIds = userRoles.stream()
                .map(ur -> ur.getRole().getId())
                .collect(Collectors.toSet());

        return rolePermissionRepository.findByRoleIdIn(roleIds).stream()
                .map(rp -> rp.getPermission().getCode())
                .collect(Collectors.toSet());
    }

    private UserSummaryResponse buildUserSummary(User user, Set<String> roles, Set<String> permissions) {
        String fullName = user.getFullName();
        if ((fullName == null || fullName.isBlank()) && user.getUsername() != null) {
            fullName = user.getUsername();
        }
        String firstName = "";
        String lastName = "";
        if (fullName != null && !fullName.isBlank()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) {
                lastName = parts[1];
            }
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(firstName)
                .lastName(lastName)
                .roles(roles)
                .permissions(permissions)
                .active(user.isActive())
                .build();
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }
}
