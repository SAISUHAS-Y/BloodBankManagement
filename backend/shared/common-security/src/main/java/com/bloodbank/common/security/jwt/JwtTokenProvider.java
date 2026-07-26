package com.bloodbank.common.security.jwt;

import com.bloodbank.common.security.constant.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Token provider that supports both Symmetric (HS256) and Asymmetric (RS256) signing.
 * 
 * In a production microservices environment:
 * - The Auth Service uses the Private Key to sign JWTs (RS256).
 * - Downstream services use the Public Key to verify JWTs, preventing key exposure.
 * - Symmetrical (HS256) fallback is supported if only a secret string is provided.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.security.jwt.secret:}")
    private String jwtSecret;

    @Value("${app.security.jwt.private-key:}")
    private String privateKeyStr;

    @Value("${app.security.jwt.public-key:}")
    private String publicKeyStr;

    @Value("${app.security.jwt.kid:key-1}")
    private String activeKeyId;

    private SecretKey symmetricKey;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private boolean useAsymmetric = false;
    private final Map<String, PublicKey> publicKeyMap = new java.util.concurrent.ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        try {
            if (privateKeyStr != null && !privateKeyStr.isBlank() && publicKeyStr != null && !publicKeyStr.isBlank()) {
                byte[] privateKeyBytes = Decoders.BASE64.decode(cleanKey(privateKeyStr));
                byte[] publicKeyBytes = Decoders.BASE64.decode(cleanKey(publicKeyStr));

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));
                this.publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publicKeyBytes));
                this.useAsymmetric = true;
                this.publicKeyMap.put(activeKeyId, this.publicKey);
                log.info("JWT Token Provider initialized using RS256 (Asymmetric RSA) with kid '{}'.", activeKeyId);
            } else if (jwtSecret != null && !jwtSecret.isBlank()) {
                byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
                this.symmetricKey = Keys.hmacShaKeyFor(keyBytes);
                log.info("JWT Token Provider initialized using HS256 (Symmetric HMAC).");
            } else {
                java.security.KeyPairGenerator keyPairGenerator = java.security.KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(2048);
                java.security.KeyPair keyPair = keyPairGenerator.generateKeyPair();
                this.privateKey = keyPair.getPrivate();
                this.publicKey = keyPair.getPublic();
                this.useAsymmetric = true;
                this.publicKeyMap.put(activeKeyId, this.publicKey);
                log.info("No JWT key configuration provided. Dynamically generated RSA 2048-bit keypair for RS256 signing with kid '{}'.", activeKeyId);
            }
        } catch (Exception e) {
            log.error("Failed to initialize JWT Token Provider keys", e);
            throw new IllegalStateException("Failed to initialize JWT keys", e);
        }
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public java.security.interfaces.RSAPublicKey getRSAPublicKey() {
        return (this.publicKey instanceof java.security.interfaces.RSAPublicKey rsaKey) ? rsaKey : null;
    }

    public String getActiveKeyId() {
        return this.activeKeyId;
    }

    private String cleanKey(String key) {
        return key.replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
    }

    /**
     * Generate token with subject (username), userId, roles, permissions, and kid header.
     */
    public String generateToken(String username, Long userId, Collection<String> roles, Collection<String> permissions) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + SecurityConstants.ACCESS_TOKEN_EXPIRATION_MS);

        Map<String, Object> claims = new HashMap<>();
        claims.put(SecurityConstants.USER_ID_CLAIM, userId);
        claims.put(SecurityConstants.ROLES_CLAIM, roles);
        claims.put(SecurityConstants.PERMISSIONS_CLAIM, permissions);

        java.security.Key signingKey = useAsymmetric ? privateKey : symmetricKey;
        if (signingKey == null) {
            throw new IllegalStateException("Private Key is required to sign JWT tokens.");
        }

        var builder = Jwts.builder()
                .header()
                .keyId(activeKeyId)
                .and()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey);

        return builder.compact();
    }

    /**
     * Lookup public key by Key ID (kid) for verification to support seamless key rotation.
     */
    public PublicKey lookupPublicKeyByKid(String kid) {
        if (kid == null) {
            return this.publicKey;
        }
        return publicKeyMap.getOrDefault(kid, this.publicKey);
    }

    /**
     * Validate JWT. Returns true if valid, false if expired/malformed.
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty.");
        }
        return false;
    }

    /**
     * Extract Username (subject) from JWT.
     */
    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extract User ID from JWT.
     */
    public Long getUserIdFromToken(String token) {
        Number userId = getClaims(token).get(SecurityConstants.USER_ID_CLAIM, Number.class);
        return userId != null ? userId.longValue() : null;
    }

    /**
     * Extract authorities (permissions + roles) from JWT.
     */
    public Collection<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaims(token);

        @SuppressWarnings("unchecked")
        List<String> roles = claims.get(SecurityConstants.ROLES_CLAIM, List.class);
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get(SecurityConstants.PERMISSIONS_CLAIM, List.class);

        Collection<GrantedAuthority> authorities = new java.util.ArrayList<>();
        
        if (roles != null) {
            roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .forEach(authorities::add);
        }
        
        if (permissions != null) {
            permissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        return authorities;
    }

    private Claims getClaims(String token) {
        var parserBuilder = Jwts.parser();
        if (useAsymmetric) {
            parserBuilder.verifyWith(publicKey);
        } else {
            parserBuilder.verifyWith(symmetricKey);
        }
        return parserBuilder.build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
