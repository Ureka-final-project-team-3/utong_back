package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class ExistWaitingSaleRequestException extends BusinessException {
    public ExistWaitingSaleRequestException() {
        super(ErrorCode.EXIST_SALE_REQUEST);
    }
}
