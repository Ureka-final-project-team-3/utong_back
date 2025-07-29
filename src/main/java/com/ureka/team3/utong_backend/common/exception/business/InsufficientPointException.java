package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class InsufficientPointException extends BusinessException {

    public InsufficientPointException() {
        super(ErrorCode.INSUFFICIENT_POINT);
    }

    public InsufficientPointException(String message) {
        super(ErrorCode.INSUFFICIENT_POINT, message);
    }
}