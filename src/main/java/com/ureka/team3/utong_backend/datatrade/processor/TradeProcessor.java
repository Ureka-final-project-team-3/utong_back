package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.ContractDto;
import com.ureka.team3.utong_backend.datatrade.dto.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;

public interface TradeProcessor {
    ContractDto processBuyMatches(BuyDataRequest request, TradeMatch match);

    ContractDto processSaleMatches(SaleDataRequest request, TradeMatch match);
}
