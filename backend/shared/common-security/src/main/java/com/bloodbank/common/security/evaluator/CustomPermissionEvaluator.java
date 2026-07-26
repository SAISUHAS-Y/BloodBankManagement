package com.bloodbank.common.security.evaluator;

import com.bloodbank.common.security.audit.SecurityAuditLogger;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final SecurityAuditLogger auditLogger;

    /**
     * Caffeine Cache for Permission Evaluation:
     * - Key format: username + ":" + requiredPermission
     * - TTL: 5 Minutes (Expire after write)
     * - RATIONALE & TRADEOFF: Caching authorization decisions in-memory for 5 minutes avoids repeated SpEL parsing,
     *   DB queries, or authority iteration on high-throughput REST endpoints.
     *   The tradeoff is that administrative permission revocation will take up to 5 minutes to take effect fleet-wide.
     *   For an enterprise blood bank system, 5-minute propagation is acceptable while keeping latency ultra-low.
     */
    private final Cache<String, Boolean> permissionCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    @Autowired
    public CustomPermissionEvaluator(SecurityAuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || permission == null) {
            logDenial(authentication, String.valueOf(permission));
            return false;
        }
        return checkCachedPermission(authentication, permission.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || permission == null) {
            logDenial(authentication, String.valueOf(permission));
            return false;
        }
        return checkCachedPermission(authentication, permission.toString());
    }

    private boolean checkCachedPermission(Authentication authentication, String requiredPermission) {
        String cacheKey = authentication.getName() + ":" + requiredPermission;
        Boolean hasPrivilege = permissionCache.get(cacheKey, key -> hasPrivilege(authentication, requiredPermission));

        if (Boolean.FALSE.equals(hasPrivilege)) {
            logDenial(authentication, requiredPermission);
        }

        return Boolean.TRUE.equals(hasPrivilege);
    }

    private boolean hasPrivilege(Authentication authentication, String requiredPermission) {
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(authority -> authority.equals(requiredPermission));
    }

    private void logDenial(Authentication authentication, String requiredPermission) {
        String username = authentication != null ? authentication.getName() : "ANONYMOUS";
        HttpServletRequest currentRequest = getCurrentHttpRequest();
        auditLogger.logPermissionDenial(username, requiredPermission, "CustomPermissionEvaluator", currentRequest);
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
