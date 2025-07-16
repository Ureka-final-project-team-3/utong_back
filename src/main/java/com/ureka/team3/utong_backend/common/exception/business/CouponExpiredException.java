package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class CouponExpiredException extends BusinessException {
    public CouponExpiredException() {
        super(ErrorCode.COUPON_EXPIRED);
    }
}
