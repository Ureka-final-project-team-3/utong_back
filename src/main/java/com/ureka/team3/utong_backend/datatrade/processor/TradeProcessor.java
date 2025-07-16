package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.PurchaseMatch;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatch;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;

public interface TradeProcessor {
    void processBuyMatches(BuyDataRequest request, PurchaseMatch match);

    void processSaleMatches(SaleDataRequest request, SaleMatch match);
}
