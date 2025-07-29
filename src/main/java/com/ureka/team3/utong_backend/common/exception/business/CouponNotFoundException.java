package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class CouponNotFoundException extends BusinessException {

    public CouponNotFoundException() {
        super(ErrorCode.COUPON_NOT_FOUND);
    }

    public CouponNotFoundException(String message) {
        super(ErrorCode.COUPON_NOT_FOUND, message);
    }
}
