package com.bloodbank.common.exception;

import com.bloodbank.common.exception.enums.ErrorCode;

public class DuplicateResourceException extends BaseException {
    
    public DuplicateResourceException(String message) {
        super(message, ErrorCode.DUPLICATE_RESOURCE);
    }

    public DuplicateResourceException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}
