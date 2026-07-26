package com.bloodbank.common.exception;

import com.bloodbank.common.exception.enums.ErrorCode;

public class BusinessRuleViolationException extends BaseException {
    
    public BusinessRuleViolationException(String message) {
        super(message, ErrorCode.BUSINESS_RULE_VIOLATION);
    }

    public BusinessRuleViolationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
