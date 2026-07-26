package com.bloodbank.common.security.aspect;

import com.bloodbank.common.exception.UnauthorizedActionException;
import com.bloodbank.common.security.annotation.HasPermission;
import com.bloodbank.common.security.audit.SecurityAuditLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class SecurityAspect {

    private final SecurityAuditLogger auditLogger;

    @Autowired
    public SecurityAspect(SecurityAuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    @Before("@annotation(hasPermission) || @within(hasPermission)")
    public void checkPermission(HasPermission hasPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String requiredPermission = hasPermission.value();

        if (authentication == null || !authentication.isAuthenticated()) {
            auditLogger.logPermissionDenial("ANONYMOUS", requiredPermission, "SecurityAspect", getCurrentHttpRequest());
            throw new UnauthorizedActionException("Authentication is required to perform this action");
        }

        boolean hasPrivilege = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .anyMatch(authority -> authority.equals(requiredPermission));

        if (!hasPrivilege) {
            auditLogger.logPermissionDenial(authentication.getName(), requiredPermission, "SecurityAspect", getCurrentHttpRequest());
            throw new UnauthorizedActionException("Access denied: You do not have the required permission: " + requiredPermission);
        }
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getRequest() : null;
    }
}
