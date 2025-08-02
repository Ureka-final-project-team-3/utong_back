package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.ContractDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;

public interface TradeProcessor {
    ContractDto processBuyMatches(BuyDataRequest request, TradeMatch match);

    ContractDto processSaleMatches(SaleDataRequest request, TradeMatch match);
}
