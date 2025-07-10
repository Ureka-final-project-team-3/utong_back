package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException() {
        super(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    public AccountNotFoundException(String message) {
        super(ErrorCode.ACCOUNT_NOT_FOUND, message);
    }
}