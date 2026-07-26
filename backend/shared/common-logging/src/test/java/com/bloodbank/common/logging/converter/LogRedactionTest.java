package com.bloodbank.common.logging.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogRedactionTest {

    private final SensitiveDataMaskingConverter converter = new SensitiveDataMaskingConverter();

    @Test
    void testPasswordRedactionInJsonString() {
        String inputJson = "{\"username\": \"john_doe\", \"password\": \"Secret123!\", \"role\": \"ADMIN\"}";
        String redacted = converter.transform(null, inputJson);

        assertFalse(redacted.contains("Secret123!"));
        assertTrue(redacted.contains("\"password\": \"[REDACTED]\""));
        assertTrue(redacted.contains("\"username\": \"john_doe\""));
    }

    @Test
    void testTokenAndAuthorizationRedactionInToStringFormat() {
        String inputToString = "LoginRequest(username=admin, token=eyJhbGciOi..., authorization=Bearer abc123xyz)";
        String redacted = converter.transform(null, inputToString);

        assertFalse(redacted.contains("Bearer abc123xyz"));
        assertFalse(redacted.contains("eyJhbGciOi..."));
        assertTrue(redacted.contains("token=[REDACTED]"));
        assertTrue(redacted.contains("authorization=[REDACTED]"));
    }
}
