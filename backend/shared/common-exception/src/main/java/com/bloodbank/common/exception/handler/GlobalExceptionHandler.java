package com.bloodbank.common.exception.handler;

import com.bloodbank.common.core.context.RequestContext;
import com.bloodbank.common.core.dto.ApiResponse;
import com.bloodbank.common.exception.BaseException;
import com.bloodbank.common.exception.BusinessRuleViolationException;
import com.bloodbank.common.exception.DuplicateResourceException;
import com.bloodbank.common.exception.ResourceNotFoundException;
import com.bloodbank.common.exception.UnauthorizedActionException;
import com.bloodbank.common.exception.enums.ErrorCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        String traceId = RequestContext.getTraceId();
        log.warn("[ResourceNotFoundException] Code: {}, Message: {}, TraceId: {}", ex.getErrorCode().getCode(), ex.getMessage(), traceId);
        ApiResponse<String> apiResponse = ApiResponse.failure(HttpStatus.NOT_FOUND.value(), ex.getMessage(), traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiResponse<String>> handleBusinessRuleViolationException(BusinessRuleViolationException ex) {
        String traceId = RequestContext.getTraceId();
        log.warn("[BusinessRuleViolationException] Code: {}, Message: {}, TraceId: {}", ex.getErrorCode().getCode(), ex.getMessage(), traceId);
        ApiResponse<String> apiResponse = ApiResponse.failure(HttpStatus.UNPROCESSABLE_CONTENT.value(), ex.getMessage(), traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.UNPROCESSABLE_CONTENT);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<String>> handleDuplicateResourceException(DuplicateResourceException ex) {
        String traceId = RequestContext.getTraceId();
        log.warn("[DuplicateResourceException] Code: {}, Message: {}, TraceId: {}", ex.getErrorCode().getCode(), ex.getMessage(), traceId);
        ApiResponse<String> apiResponse = ApiResponse.failure(HttpStatus.CONFLICT.value(), ex.getMessage(), traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<ApiResponse<String>> handleUnauthorizedActionException(UnauthorizedActionException ex) {
        String traceId = RequestContext.getTraceId();
        log.warn("[UnauthorizedActionException] Code: {}, Message: {}, TraceId: {}", ex.getErrorCode().getCode(), ex.getMessage(), traceId);
        ApiResponse<String> apiResponse = ApiResponse.failure(HttpStatus.FORBIDDEN.value(), ex.getMessage(), traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<String>> handleBaseException(BaseException ex) {
        String traceId = RequestContext.getTraceId();
        ErrorCode errorCode = ex.getErrorCode();
        log.error("[BaseException] Code: {}, Message: {}, TraceId: {}", errorCode.getCode(), ex.getMessage(), traceId, ex);
        ApiResponse<String> apiResponse = ApiResponse.failure(errorCode.getHttpStatus().value(), ex.getMessage(), traceId);
        return new ResponseEntity<>(apiResponse, errorCode.getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = RequestContext.getTraceId();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("[ValidationException] Errors: {}, TraceId: {}", errors, traceId);

        ApiResponse<Map<String, String>> apiResponse = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors, traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        String traceId = RequestContext.getTraceId();
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String propertyPath = violation.getPropertyPath().toString();
            String fieldName = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
            errors.put(fieldName, violation.getMessage());
        });
        log.warn("[ConstraintViolationException] Errors: {}, TraceId: {}", errors, traceId);

        ApiResponse<Map<String, String>> apiResponse = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), "Constraint validation failed", errors, traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<String>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String traceId = RequestContext.getTraceId();
        log.warn("[HttpMessageNotReadableException] Message: {}, TraceId: {}", ex.getMessage(), traceId);
        ApiResponse<String> apiResponse = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), "Malformed JSON request body or missing payload", traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        String traceId = RequestContext.getTraceId();
        String fieldName = ex.getName();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format("Parameter '%s' should be of type '%s'", fieldName, requiredType);
        log.warn("[MethodArgumentTypeMismatchException] Message: {}, TraceId: {}", message, traceId);
        ApiResponse<String> apiResponse = ApiResponse.failure(HttpStatus.BAD_REQUEST.value(), message, traceId);
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleAllExceptions(Exception ex) {
        String traceId = RequestContext.getTraceId();
        log.error("[UnhandledException] Message: {}, TraceId: {}", ex.getMessage(), traceId, ex);
        ApiResponse<String> apiResponse = ApiResponse.failure(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected server error occurred. Please contact support.",
                traceId
        );
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
