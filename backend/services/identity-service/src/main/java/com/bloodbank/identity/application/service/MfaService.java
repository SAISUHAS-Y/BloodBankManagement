package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.request.MfaDisableRequest;
import com.bloodbank.identity.application.dto.request.MfaEnableRequest;
import com.bloodbank.identity.application.dto.request.MfaVerifyRequest;
import com.bloodbank.identity.application.dto.response.LoginResponse;
import com.bloodbank.identity.application.dto.response.MfaBackupCodesResponse;
import com.bloodbank.identity.application.dto.response.MfaSetupResponse;

public interface MfaService {

    MfaSetupResponse setupMfa(String username);

    MfaBackupCodesResponse enableMfa(String username, MfaEnableRequest request);

    void disableMfa(String username, MfaDisableRequest request);

    MfaBackupCodesResponse generateBackupCodes(String username);

    LoginResponse verifyMfaAndCompleteLogin(MfaVerifyRequest request, String ipAddress, String userAgent);

    String generateMfaTransactionToken(Long userId, String username);
}
