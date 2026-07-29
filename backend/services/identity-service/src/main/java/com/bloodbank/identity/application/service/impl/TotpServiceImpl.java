package com.bloodbank.identity.application.service.impl;

import com.bloodbank.identity.application.service.TotpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

@Service
@Slf4j
public class TotpServiceImpl implements TotpService {

    private static final String BASE32_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int SECRET_BYTE_LENGTH = 20; // 160-bit key
    private static final int TIME_STEP_SECONDS = 30;
    private static final int WINDOW_STEPS = 1; // Allow +- 30 seconds clock skew drift

    @Override
    public String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_BYTE_LENGTH];
        random.nextBytes(bytes);
        return encodeBase32(bytes);
    }

    @Override
    public String generateQrCodeUri(String secretKey, String accountName, String issuer) {
        try {
            String encodedAccount = URLEncoder.encode(accountName, StandardCharsets.UTF_8);
            String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
            return String.format(
                    "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=%d",
                    encodedIssuer, encodedAccount, secretKey, encodedIssuer, TIME_STEP_SECONDS
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to format QR code URI", e);
        }
    }

    @Override
    public boolean verifyTotpCode(String secretKey, String code) {
        if (secretKey == null || code == null || !code.matches("^[0-9]{6}$")) {
            return false;
        }

        try {
            byte[] keyBytes = decodeBase32(secretKey);
            long currentTimestamp = System.currentTimeMillis() / 1000L;
            long currentFrame = currentTimestamp / TIME_STEP_SECONDS;

            for (int i = -WINDOW_STEPS; i <= WINDOW_STEPS; i++) {
                long frame = currentFrame + i;
                String generatedCode = generateTotpForFrame(keyBytes, frame);
                if (code.equals(generatedCode)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error verifying TOTP token", e);
        }

        return false;
    }

    @Override
    public int getValidationWindowTimeSeconds() {
        return TIME_STEP_SECONDS;
    }

    private String generateTotpForFrame(byte[] keyBytes, long frame) throws Exception {
        byte[] data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (frame & 0xFF);
            frame >>= 8;
        }

        SecretKeySpec signKey = new SecretKeySpec(keyBytes, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xF;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % 1_000_000;
        return String.format("%06d", otp);
    }

    private String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int next = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            next = (next << 8) | (b & 0xFF);
            bitsLeft += 8;

            while (bitsLeft >= 5) {
                int index = (next >> (bitsLeft - 5)) & 0x1F;
                sb.append(BASE32_ALPHABET.charAt(index));
                bitsLeft -= 5;
            }
        }

        if (bitsLeft > 0) {
            int index = (next << (5 - bitsLeft)) & 0x1F;
            sb.append(BASE32_ALPHABET.charAt(index));
        }

        return sb.toString();
    }

    private byte[] decodeBase32(String base32Str) {
        String cleanStr = base32Str.toUpperCase().replaceAll("[^A-Z2-7]", "");
        byte[] result = new byte[cleanStr.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int count = 0;

        for (char c : cleanStr.toCharArray()) {
            int val = BASE32_ALPHABET.indexOf(c);
            if (val == -1) continue;

            buffer = (buffer << 5) | val;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                result[count++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }

        return result;
    }
}
