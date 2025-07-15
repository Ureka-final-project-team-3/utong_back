package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;

public interface BuyMatchingProcessor {
    BuyMatchingResult handle(DataTradeDto.BuyDataRequestDto requestDto);

}
