package com.ureka.team3.utong_backend.common.exception.business;


import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;

public class PriceNotFoundException extends BusinessException {
    public PriceNotFoundException(String message) { super(ErrorCode.PRICE_NOT_FOUND, message); }

    public PriceNotFoundException() { super(ErrorCode.PRICE_NOT_FOUND); }
}
