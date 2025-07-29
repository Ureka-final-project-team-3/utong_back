package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class CannotCancelCompletedOrderException extends BusinessException {
    public CannotCancelCompletedOrderException() {
        super(ErrorCode.CANNOT_CANCEL_COMPLETE_ORDER);
    }
}
