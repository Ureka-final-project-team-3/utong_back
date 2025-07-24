package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class ExistWaitingPurchaseRequestException extends BusinessException {
    public ExistWaitingPurchaseRequestException() {
        super(ErrorCode.EXIST_PURCHASE_REQUEST);
    }
}
