package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class NotDataCouponException extends BusinessException {
    public NotDataCouponException() {
        super(ErrorCode.NOT_DATA_COUPON);
    }
}
