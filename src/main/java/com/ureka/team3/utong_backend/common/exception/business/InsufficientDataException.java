package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class InsufficientDataException extends BusinessException {
    public InsufficientDataException() {
        super(ErrorCode.INSUFFICIENT_DATA);
    }
}
