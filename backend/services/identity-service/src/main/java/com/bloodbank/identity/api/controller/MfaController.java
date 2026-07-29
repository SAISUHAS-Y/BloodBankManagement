package com.bloodbank.identity.api.controller;

import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.identity.application.dto.MfaBackupCodesResponse;
import com.bloodbank.identity.application.dto.MfaDisableRequest;
import com.bloodbank.identity.application.dto.MfaEnableRequest;
import com.bloodbank.identity.application.dto.MfaSetupResponse;
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
@Tag(name = "Multi-Factor Authentication", description = "Endpoints for configuring TOTP 2FA, backup recovery codes, and MFA verification")
public class MfaController {

    private final MfaService mfaService;

    @PostMapping("/setup")
    @Operation(summary = "Initiate MFA Setup", description = "Generates TOTP secret key and QR code URI for scanning with authenticator app")
    public ResponseEntity<ApiResponse<MfaSetupResponse>> setupMfa(Principal principal) {
        MfaSetupResponse response = mfaService.setupMfa(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("MFA setup initiated. Scan QR code to continue.", response));
    }

    @PostMapping("/enable")
    @Operation(summary = "Confirm & Enable MFA", description = "Verifies TOTP token, activates MFA on account, and returns 8 single-use backup codes")
    public ResponseEntity<ApiResponse<MfaBackupCodesResponse>> enableMfa(
            @Valid @RequestBody MfaEnableRequest request,
            Principal principal) {
        MfaBackupCodesResponse response = mfaService.enableMfa(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("MFA enabled successfully. Store backup codes in a safe place.", response));
    }

    @PostMapping("/disable")
    @Operation(summary = "Disable MFA", description = "Confirms password and TOTP token to deactivate 2FA on account")
    public ResponseEntity<ApiResponse<Void>> disableMfa(
            @Valid @RequestBody MfaDisableRequest request,
            Principal principal) {
        mfaService.disableMfa(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("MFA has been disabled successfully", null));
    }

    @PostMapping("/generate-backup-codes")
    @Operation(summary = "Regenerate Backup Recovery Codes", description = "Replaces existing backup codes with 8 fresh single-use emergency recovery codes")
    public ResponseEntity<ApiResponse<MfaBackupCodesResponse>> generateBackupCodes(Principal principal) {
        MfaBackupCodesResponse response = mfaService.generateBackupCodes(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Fresh backup codes generated successfully", response));
    }
}
