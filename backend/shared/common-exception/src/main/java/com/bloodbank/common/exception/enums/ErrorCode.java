package com.bloodbank.common.exception.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    RESOURCE_NOT_FOUND("ERR_RESOURCE_NOT_FOUND", HttpStatus.NOT_FOUND, "Requested resource was not found"),
    BUSINESS_RULE_VIOLATION("ERR_BUSINESS_RULE_VIOLATION", HttpStatus.UNPROCESSABLE_CONTENT, "Business rule constraint violated"),
    DUPLICATE_RESOURCE("ERR_DUPLICATE_RESOURCE", HttpStatus.CONFLICT, "Resource already exists"),
    UNAUTHORIZED_ACTION("ERR_UNAUTHORIZED_ACTION", HttpStatus.FORBIDDEN, "Unauthorized action or insufficient privileges"),
    INVALID_INPUT("ERR_INVALID_INPUT", HttpStatus.BAD_REQUEST, "Invalid parameter or payload input"),
    INTERNAL_SERVER_ERROR("ERR_INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please contact support."),
    
    // Domain-Specific Expanded Error Codes
    DONOR_NOT_ELIGIBLE("ERR_DONOR_NOT_ELIGIBLE", HttpStatus.UNPROCESSABLE_CONTENT, "Donor is not eligible for donation: %s"),
    DONOR_CURRENTLY_DEFERRED("ERR_DONOR_CURRENTLY_DEFERRED", HttpStatus.UNPROCESSABLE_CONTENT, "Donor is currently deferred until %s"),
    INSUFFICIENT_STOCK("ERR_INSUFFICIENT_STOCK", HttpStatus.UNPROCESSABLE_CONTENT, "Insufficient blood stock for group: %s"),
    DUPLICATE_DONATION_IDEMPOTENCY_KEY("ERR_DUPLICATE_DONATION_IDEMPOTENCY_KEY", HttpStatus.CONFLICT, "Duplicate donation request with idempotency key: %s"),
    PERMISSION_DENIED("ERR_PERMISSION_DENIED", HttpStatus.FORBIDDEN, "Access denied: Required permission '%s' is missing"),
    LOOKUP_ITEM_INACTIVE("ERR_LOOKUP_ITEM_INACTIVE", HttpStatus.BAD_REQUEST, "Master lookup item '%s' is inactive or invalid"),
    ACCOUNT_LOCKED("ERR_ACCOUNT_LOCKED", HttpStatus.UNAUTHORIZED, "Account locked due to 5 consecutive failed login attempts. Try again later."),
    PASSWORD_CHANGE_REQUIRED("ERR_PASSWORD_CHANGE_REQUIRED", HttpStatus.FORBIDDEN, "Password change required before accessing system resources."),
    ROLE_IN_USE("ERR_ROLE_IN_USE", HttpStatus.CONFLICT, "Role cannot be deleted while assigned to active users."),
    LAST_ADMIN_REVOCATION_PREVENTED("ERR_LAST_ADMIN_REVOCATION_PREVENTED", HttpStatus.CONFLICT, "Cannot revoke permission/role as it would leave zero users with administrative privileges.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessageTemplate;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessageTemplate) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessageTemplate = defaultMessageTemplate;
    }
}
