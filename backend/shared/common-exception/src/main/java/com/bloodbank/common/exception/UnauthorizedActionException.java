package com.bloodbank.common.exception;

import com.bloodbank.common.exception.enums.ErrorCode;

public class UnauthorizedActionException extends BaseException {
    
    public UnauthorizedActionException(String message) {
        super(message, ErrorCode.UNAUTHORIZED_ACTION);
    }

    public UnauthorizedActionException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
