package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class ExceedSaleLimitException extends BusinessException {
    public ExceedSaleLimitException() {
        super(ErrorCode.EXCEED_SALE_LIMIT);
    }
}
