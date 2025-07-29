package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class NotExistDefaultLineException extends BusinessException {
    public NotExistDefaultLineException() {
        super(ErrorCode.NEED_DEFAULT_LINE);
    }
}
