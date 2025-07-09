package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class LineNotFoundException extends BusinessException {
    public LineNotFoundException() {
        super(ErrorCode.LINE_NOT_FOUND);
    }

    public LineNotFoundException(String message) {
        super(ErrorCode.LINE_NOT_FOUND, message);
    }
}
