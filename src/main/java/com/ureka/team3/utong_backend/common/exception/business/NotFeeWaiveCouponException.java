package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class NotFeeWaiveCouponException extends BusinessException {
    public NotFeeWaiveCouponException() {
        super(ErrorCode.NOT_FEE_WAIVE_COUPON);
    }
}
