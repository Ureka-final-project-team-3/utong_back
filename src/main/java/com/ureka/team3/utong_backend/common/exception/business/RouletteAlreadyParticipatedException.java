package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class RouletteAlreadyParticipatedException extends BusinessException {
    public RouletteAlreadyParticipatedException() {
        super(ErrorCode.ROULETTE_ALREADY_PARTICIPATED);
    }
    
    public RouletteAlreadyParticipatedException(String message) {
        super(ErrorCode.ROULETTE_ALREADY_PARTICIPATED, message);
    }
}