package com.bloodbank.common.exception;

import com.bloodbank.common.exception.enums.ErrorCode;

/**
 * Exception thrown when user input parameters fail validation checks.
 */
public class InvalidInputException extends BaseException {
    
    public InvalidInputException(String message) {
        super(message, ErrorCode.INVALID_INPUT);
    }
}
