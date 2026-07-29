package com.bloodbank.identity.application.service.impl;

import com.bloodbank.common.contracts.dto.UserSummaryResponse;
import com.bloodbank.common.exception.InvalidInputException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.exception.UnauthorizedActionException;
import com.bloodbank.common.security.jwt.JwtTokenProvider;
import com.bloodbank.identity.application.dto.LoginResponse;
import com.bloodbank.identity.application.dto.MfaBackupCodesResponse;
import com.bloodbank.identity.application.dto.MfaDisableRequest;
import com.bloodbank.identity.application.dto.MfaEnableRequest;
import com.bloodbank.identity.application.dto.MfaSetupResponse;
import com.bloodbank.identity.application.dto.MfaVerifyRequest;
import com.bloodbank.identity.application.service.MfaService;
import com.bloodbank.identity.application.service.TotpService;
import com.bloodbank.identity.domain.entity.RefreshToken;
import com.bloodbank.identity.domain.entity.User;
import com.bloodbank.identity.domain.entity.UserMfaSecret;
import com.bloodbank.identity.domain.entity.UserRole;
import com.bloodbank.identity.domain.repository.RefreshTokenRepository;
import com.bloodbank.identity.domain.repository.RolePermissionRepository;
import com.bloodbank.identity.domain.repository.UserMfaSecretRepository;
import com.bloodbank.identity.domain.repository.UserRepository;
import com.bloodbank.identity.domain.repository.UserRoleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MfaServiceImpl implements MfaService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int BACKUP_CODE_COUNT = 8;
    private static final int BACKUP_CODE_LENGTH = 10;

    private final UserMfaSecretRepository mfaSecretRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TotpService totpService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Value("${app.security.jwt.access-expiration-ms:900000}")
    private long accessExpirationMs;

    @Value("${app.name:BloodBankManagement}")
    private String appName;

    // Temporary store for MFA challenge tokens (mfaToken -> ChallengeData)
    private final Map<String, ChallengeData> challengeMap = new ConcurrentHashMap<>();

    private record ChallengeData(Long userId, String username, Instant expiresAt) {}

    @Override
    @Transactional
    public MfaSetupResponse setupMfa(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserMfaSecret mfaSecret = mfaSecretRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserMfaSecret newSecret = new UserMfaSecret();
                    newSecret.setUserId(user.getId());
                    return newSecret;
                });

        if (mfaSecret.isMfaEnabled()) {
            throw new InvalidInputException("MFA is already enabled on your account");
        }

        String secretKey = totpService.generateSecretKey();
        mfaSecret.setSecretKey(secretKey);
        mfaSecret.setMfaType("TOTP");
        mfaSecretRepository.save(mfaSecret);

        String qrUri = totpService.generateQrCodeUri(secretKey, user.getEmail(), appName);
        log.info("Generated TOTP setup secret for user [{}]", username);

        return MfaSetupResponse.builder()
                .secretKey(secretKey)
                .qrCodeUri(qrUri)
                .build();
    }

    @Override
    @Transactional
    public MfaBackupCodesResponse enableMfa(String username, MfaEnableRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserMfaSecret mfaSecret = mfaSecretRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidInputException("MFA secret not initialized. Please initiate setup first."));

        if (!totpService.verifyTotpCode(mfaSecret.getSecretKey(), request.getTotpCode())) {
            throw new InvalidInputException("Invalid TOTP verification code");
        }

        mfaSecret.setMfaEnabled(true);
        List<String> plainBackupCodes = generateAndSaveBackupCodes(mfaSecret);
        mfaSecretRepository.save(mfaSecret);

        log.info("MFA enabled successfully for user [{}]", username);
        return MfaBackupCodesResponse.builder()
                .backupCodes(plainBackupCodes)
                .build();
    }

    @Override
    @Transactional
    public void disableMfa(String username, MfaDisableRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedActionException("Invalid account password confirmation");
        }

        UserMfaSecret mfaSecret = mfaSecretRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidInputException("MFA is not enabled on this account"));

        if (!totpService.verifyTotpCode(mfaSecret.getSecretKey(), request.getTotpCode())) {
            throw new InvalidInputException("Invalid TOTP verification code");
        }

        mfaSecret.setMfaEnabled(false);
        mfaSecret.setScratchCodes(null);
        mfaSecretRepository.save(mfaSecret);

        log.info("MFA disabled for user [{}]", username);
    }

    @Override
    @Transactional
    public MfaBackupCodesResponse generateBackupCodes(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        UserMfaSecret mfaSecret = mfaSecretRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidInputException("MFA is not enabled on this account"));

        if (!mfaSecret.isMfaEnabled()) {
            throw new InvalidInputException("MFA is not enabled on this account");
        }

        List<String> plainBackupCodes = generateAndSaveBackupCodes(mfaSecret);
        mfaSecretRepository.save(mfaSecret);

        log.info("Regenerated 8 backup recovery codes for user [{}]", username);
        return MfaBackupCodesResponse.builder()
                .backupCodes(plainBackupCodes)
                .build();
    }

    @Override
    public String generateMfaTransactionToken(Long userId, String username) {
        String token = "mfa_challenge_" + UUID.randomUUID().toString();
        challengeMap.put(token, new ChallengeData(userId, username, Instant.now().plus(5, ChronoUnit.MINUTES)));
        return token;
    }

    @Override
    @Transactional
    public LoginResponse verifyMfaAndCompleteLogin(MfaVerifyRequest request, String ipAddress, String userAgent) {
        ChallengeData challenge = challengeMap.remove(request.getMfaToken());
        if (challenge == null || challenge.expiresAt().isBefore(Instant.now())) {
            throw new InvalidInputException("MFA challenge token has expired or is invalid. Please log in again.");
        }

        User user = userRepository.findById(challenge.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserMfaSecret mfaSecret = mfaSecretRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidInputException("MFA configuration error"));

        boolean verified = false;
        String rawCode = request.getCode().trim();

        if (rawCode.length() == 6 && rawCode.matches("^[0-9]{6}$")) {
            verified = totpService.verifyTotpCode(mfaSecret.getSecretKey(), rawCode);
        } else if (rawCode.length() == BACKUP_CODE_LENGTH) {
            verified = verifyAndConsumeBackupCode(mfaSecret, rawCode);
        }

        if (!verified) {
            throw new InvalidInputException("Invalid 2FA verification code or emergency backup code");
        }

        Set<String> roles = getUserRoleNames(user);
        Set<String> permissions = getUserPermissionCodes(user);

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId(), roles, permissions);
        String refreshTokenStr = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hashToken(refreshTokenStr));
        refreshToken.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshTokenRepository.save(refreshToken);

        UserSummaryResponse userSummary = buildUserSummary(user, roles, permissions);

        log.info("2-Step MFA login completed successfully for user [{}]", user.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .expiresInMs(accessExpirationMs)
                .mfaRequired(false)
                .user(userSummary)
                .build();
    }

    private List<String> generateAndSaveBackupCodes(UserMfaSecret mfaSecret) {
        SecureRandom random = new SecureRandom();
        List<String> plainCodes = new ArrayList<>();
        List<String> hashedCodes = new ArrayList<>();

        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            String code = sb.toString();
            plainCodes.add(code);
            hashedCodes.add(hashToken(code));
        }

        try {
            mfaSecret.setScratchCodes(objectMapper.writeValueAsString(hashedCodes));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize backup codes", e);
        }

        return plainCodes;
    }

    private boolean verifyAndConsumeBackupCode(UserMfaSecret mfaSecret, String rawCode) {
        if (mfaSecret.getScratchCodes() == null || mfaSecret.getScratchCodes().isBlank()) {
            return false;
        }

        try {
            List<String> hashedCodes = objectMapper.readValue(mfaSecret.getScratchCodes(), new TypeReference<List<String>>() {});
            String targetHash = hashToken(rawCode.toUpperCase());

            if (hashedCodes.contains(targetHash)) {
                hashedCodes.remove(targetHash);
                mfaSecret.setScratchCodes(objectMapper.writeValueAsString(hashedCodes));
                mfaSecretRepository.save(mfaSecret);
                log.info("Emergency backup recovery code used and invalidated for user ID [{}]", mfaSecret.getUserId());
                return true;
            }
        } catch (Exception e) {
            log.error("Error checking backup code", e);
        }

        return false;
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
