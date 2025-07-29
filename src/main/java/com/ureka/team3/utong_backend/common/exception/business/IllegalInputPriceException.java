package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class IllegalInputPriceException extends BusinessException {
    public IllegalInputPriceException() {
        super(ErrorCode.ILLEGAL_INPUT_PRICE);
    }
}
