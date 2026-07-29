package com.bloodbank.identity.application.service;

import com.bloodbank.identity.application.dto.LoginResponse;
import com.bloodbank.identity.application.dto.SsoLoginRequest;

public interface SsoService {

    LoginResponse authenticateSso(SsoLoginRequest request, String ipAddress, String userAgent);
}
