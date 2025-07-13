package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class GifticonNotFoundException extends BusinessException {

    public GifticonNotFoundException() {
        super(ErrorCode.GIFTICON_NOT_FOUND);
    }

    public GifticonNotFoundException(String message) {
        super(ErrorCode.GIFTICON_NOT_FOUND, message);
    }
}
