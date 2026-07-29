package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.LoginResponse;
import com.bloodbank.identity.application.dto.MfaBackupCodesResponse;
import com.bloodbank.identity.application.dto.MfaDisableRequest;
import com.bloodbank.identity.application.dto.MfaEnableRequest;
import com.bloodbank.identity.application.dto.MfaSetupResponse;
import com.bloodbank.identity.application.dto.MfaVerifyRequest;

public interface MfaService {

    MfaSetupResponse setupMfa(String username);

    MfaBackupCodesResponse enableMfa(String username, MfaEnableRequest request);

    void disableMfa(String username, MfaDisableRequest request);

    MfaBackupCodesResponse generateBackupCodes(String username);

    LoginResponse verifyMfaAndCompleteLogin(MfaVerifyRequest request, String ipAddress, String userAgent);

    String generateMfaTransactionToken(Long userId, String username);
}
