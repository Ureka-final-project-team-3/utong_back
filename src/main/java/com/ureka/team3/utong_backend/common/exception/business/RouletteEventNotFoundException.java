package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class RouletteEventNotFoundException extends BusinessException {
    public RouletteEventNotFoundException() {
        super(ErrorCode.ROULETTE_EVENT_NOT_FOUND);
    }
    
    public RouletteEventNotFoundException(String message) {
        super(ErrorCode.ROULETTE_EVENT_NOT_FOUND, message);
    }
}