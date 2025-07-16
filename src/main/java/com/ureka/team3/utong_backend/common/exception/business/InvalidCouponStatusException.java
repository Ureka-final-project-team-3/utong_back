package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class InvalidCouponStatusException extends BusinessException {
    public InvalidCouponStatusException() {
        super(ErrorCode.INVALID_COUPON_STATUS);
    }
}
