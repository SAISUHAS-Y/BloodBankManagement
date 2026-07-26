package com.bloodbank.common.exception;

import com.bloodbank.common.exception.enums.ErrorCode;

public class ResourceNotFoundException extends BaseException {
    
    public ResourceNotFoundException(String message) {
        super(message, ErrorCode.RESOURCE_NOT_FOUND);
    }
}
