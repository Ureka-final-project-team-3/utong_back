package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class AlreadyCancelOrderException extends BusinessException {
    public AlreadyCancelOrderException() {
        super(ErrorCode.ALREADY_CANCELED);
    }
}
