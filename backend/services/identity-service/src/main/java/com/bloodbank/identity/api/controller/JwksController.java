package com.bloodbank.identity.api.controller;

import com.bloodbank.common.security.jwt.JwtTokenProvider;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.interfaces.RSAPublicKey;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "JWKS Key Discovery", description = "RFC 7517 JSON Web Key Set discovery endpoints for JWT signature verification")
public class JwksController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(value = {"/.well-known/jwks.json", "/api/v1/jwks.json"}, produces = "application/json")
    @Operation(summary = "Get JSON Web Key Set (JWKS)", description = "Exposes public RSA signing key parameters for stateless token signature verification across gateway and microservices.")
    public ResponseEntity<Map<String, Object>> getJwks() {
        RSAPublicKey publicKey = jwtTokenProvider.getRSAPublicKey();
        if (publicKey == null) {
            log.warn("JWKS requested but RSA Public Key is not initialized.");
            return ResponseEntity.ok(Map.of("keys", java.util.List.of()));
        }

        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .keyID(jwtTokenProvider.getActiveKeyId())
                .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
                .keyUse(com.nimbusds.jose.jwk.KeyUse.SIGNATURE)
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return ResponseEntity.ok(jwkSet.toJSONObject());
    }
}
