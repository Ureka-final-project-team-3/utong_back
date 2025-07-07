package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class NotMyReviewException extends BusinessException {
    public NotMyReviewException() {
        super(ErrorCode.NOT_MY_REVIEW);
    }

    public NotMyReviewException(String message) {
        super(ErrorCode.NOT_MY_REVIEW, message);
    }
}
