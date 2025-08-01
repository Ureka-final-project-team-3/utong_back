package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.domain.result.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;

public interface BuyMatchingProcessor {
    BuyMatchingResult handle(DataTradeDto.DataTradeRequestDto requestDto);

}
