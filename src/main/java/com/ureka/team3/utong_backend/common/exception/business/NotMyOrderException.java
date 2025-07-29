package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class NotMyOrderException extends BusinessException {
    public NotMyOrderException() {
        super(ErrorCode.NOT_MY_ORDER);
    }

    public NotMyOrderException(String message) {
        super(ErrorCode.NOT_MY_ORDER, message);
    }
}
