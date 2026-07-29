package com.bloodbank.identity.application.service;

public interface TotpService {

    String generateSecretKey();

    String generateQrCodeUri(String secretKey, String accountName, String issuer);

    boolean verifyTotpCode(String secretKey, String code);

    int getValidationWindowTimeSeconds();
}
