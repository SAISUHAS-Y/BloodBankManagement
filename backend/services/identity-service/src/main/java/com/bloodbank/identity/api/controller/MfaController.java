package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.identity.application.dto.request.MfaDisableRequest;
import com.bloodbank.identity.application.dto.request.MfaEnableRequest;
import com.bloodbank.identity.application.dto.response.MfaBackupCodesResponse;
import com.bloodbank.identity.application.dto.response.MfaSetupResponse;
import com.bloodbank.identity.application.service.MfaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/mfa")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Two-Factor Authentication (2FA)", description = "Simple endpoints for turning on 2FA (Authenticator App), getting backup codes, and turning off 2FA")
public class MfaController {

    private final MfaService mfaService;

    @PostMapping("/setup")
    @Operation(summary = "Start 2FA Setup (Get QR Code)", description = "Get QR code link to scan with Google Authenticator or Authy app")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setupMfa(Principal principal) {
        MfaSetupResponse response = mfaService.setupMfa(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("MFA setup initiated. Scan QR code to continue.", response));
    }

    @PostMapping("/enable")
    @Operation(summary = "Enable 2FA (Get Backup Codes)", description = "Confirm 6-digit code from app to activate 2FA and receive emergency backup codes")
    public ResponseEntity<ApiResponse<MfaBackupCodesResponse>> enableMfa(
            @Valid @RequestBody MfaEnableRequest request,
            Principal principal) {
        MfaBackupCodesResponse response = mfaService.enableMfa(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("MFA enabled successfully. Store backup codes in a safe place.", response));
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable 2FA", description = "Turn off 2FA on your account")
    public ResponseEntity<ApiResponse<Void>> disableMfa(
            @Valid @RequestBody MfaDisableRequest request,
            Principal principal) {
        mfaService.disableMfa(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("MFA has been disabled successfully", null));
    }

    @PostMapping("/generate-backup-codes")
    @Operation(summary = "Generate New Backup Codes", description = "Generate 8 new emergency recovery backup codes")
    public ResponseEntity<ApiResponse<MfaBackupCodesResponse>> generateBackupCodes(Principal principal) {
        MfaBackupCodesResponse response = mfaService.generateBackupCodes(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Fresh backup codes generated successfully", response));
    }
}
