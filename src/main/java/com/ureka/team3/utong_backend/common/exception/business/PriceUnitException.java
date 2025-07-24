package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class PriceUnitException extends BusinessException {

    public PriceUnitException() {
        super(ErrorCode.UNIT_ERROR);
    }
}
