package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class RouletteEventNotActiveException extends BusinessException {
    public RouletteEventNotActiveException() {
        super(ErrorCode.ROULETTE_EVENT_NOT_ACTIVE);
    }
    
    public RouletteEventNotActiveException(String message) {
        super(ErrorCode.ROULETTE_EVENT_NOT_ACTIVE, message);
    }
}