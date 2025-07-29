package com.ureka.team3.utong_backend.common.exception.business;

import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class RouletteWinnersFullException extends BusinessException {
    public RouletteWinnersFullException() {
        super(ErrorCode.ROULETTE_WINNERS_FULL);
    }
    
    public RouletteWinnersFullException(String message) {
        super(ErrorCode.ROULETTE_WINNERS_FULL, message);
    }
}