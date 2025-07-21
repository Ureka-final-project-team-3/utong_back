package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class NotMyCouponException extends BusinessException {
    public NotMyCouponException() {
        super(ErrorCode.NOT_MY_COUPON);
    }
}
