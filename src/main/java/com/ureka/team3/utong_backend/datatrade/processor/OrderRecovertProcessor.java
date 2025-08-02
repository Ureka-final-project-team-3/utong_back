package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;

public interface OrderRecovertProcessor {
    void restoreSellOrdersOnFailure(PurchaseMatchingResult result);

    void restoreBuyOrdersOnFailure(SaleMatchingResult result);
}
