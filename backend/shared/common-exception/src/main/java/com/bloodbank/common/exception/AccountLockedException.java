package com.bloodbank.common.exception;

import com.bloodbank.common.exception.enums.ErrorCode;

public class AccountLockedException extends BaseException {

    public AccountLockedException(String message) {
        super(message, ErrorCode.ACCOUNT_LOCKED);
    }
}
