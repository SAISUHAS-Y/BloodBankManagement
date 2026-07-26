package com.bloodbank.common.security.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class SecurityAuditLogger {

    public void logPermissionDenial(String username, String requiredPermission, String resourceOrEndpoint, HttpServletRequest request) {
        String clientIp = request != null ? request.getRemoteAddr() : "UNKNOWN_IP";
        String requestUri = request != null ? request.getRequestURI() : resourceOrEndpoint;
        String httpMethod = request != null ? request.getMethod() : "N/A";

        log.warn("[SECURITY AUDIT DENIAL] Timestamp: {}, User: '{}', RequestedPermission: '{}', Resource: '{}', Method: '{}', ClientIP: '{}'",
                Instant.now(),
                username != null ? username : "ANONYMOUS",
                requiredPermission,
                requestUri,
                httpMethod,
                clientIp);
    }

    public void logSecurityEvent(String eventType, String username, String details, String clientIp) {
        log.warn("[SECURITY EVENT - {}] Timestamp: {}, User: '{}', Details: '{}', ClientIP: '{}'",
                eventType,
                Instant.now(),
                username != null ? username : "ANONYMOUS",
                details,
                clientIp != null ? clientIp : "UNKNOWN_IP");
    }
}
