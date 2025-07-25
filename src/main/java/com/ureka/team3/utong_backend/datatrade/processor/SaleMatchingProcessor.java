package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatchingResult;

public interface SaleMatchingProcessor {
    SaleMatchingResult handle(DataTradeDto.DataTradeRequestDto requestDto);

}
