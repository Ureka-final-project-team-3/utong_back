package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;

public interface PurchaseMatchingProcessor {
    PurchaseMatchingResult handle(DataTradeDto.DataTradeRequestDto requestDto);

}
