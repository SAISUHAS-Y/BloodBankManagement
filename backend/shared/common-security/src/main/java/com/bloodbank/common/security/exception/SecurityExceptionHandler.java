package com.bloodbank.common.security.exception;

import com.bloodbank.common.core.constant.AppConstants;
import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.exception.enums.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.UUID;

/**
 * Dedicated ControllerAdvice to handle security-related exceptions.
 * 
 * DESIGN DECISION: Keeping security exception handling inside the common-security module 
 * prevents the common-exception module from depending on spring-security-core, which avoids 
 * classpath pollution in microservices that do not require security.
 */
@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE) // Resolves security exceptions before general exception advice
public class SecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<String>> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String traceId = MDC.get(AppConstants.MDC_CORRELATION_ID_KEY);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        log.error("[AccessDeniedException] Message: {}, TraceId: {}", ex.getMessage(), traceId, ex);

        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .success(false)
                .status(HttpStatus.FORBIDDEN.value())
                .message("Access denied: You do not have permission to perform this action")
                .data(ErrorCode.UNAUTHORIZED_ACTION.getCode())
                .traceId(traceId)
                .build();

        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }
}
