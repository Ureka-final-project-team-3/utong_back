package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class ConcurrentAccessException extends BusinessException {
    public ConcurrentAccessException() { super(ErrorCode.CONCURRENT_ACCESS_ERROR); }

    public ConcurrentAccessException(String message) {
        super(ErrorCode.CONCURRENT_ACCESS_ERROR, message);
    }
}
