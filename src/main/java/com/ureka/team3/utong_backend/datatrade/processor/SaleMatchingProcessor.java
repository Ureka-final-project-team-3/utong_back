package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;

public interface SaleMatchingProcessor {
    SaleMatchingResult handle(DataTradeDto.DataTradeRequestDto requestDto);

}
