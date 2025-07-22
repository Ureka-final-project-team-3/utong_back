package com.ureka.team3.utong_backend.datatrade.service.trade.purchase;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;

public interface BuyDataRequestService {
    BuyDataRequest save(Account account, DataTradeDto.BuyDataRequestDto dto);

    BuyDataRequest findBuyOrderById(String buyOrderId);

    void subtractPurchased(BuyDataRequest saved, long quantity);

    void changeStatus(BuyDataRequest buyOrderById, BuyOrderResult buyOrderResult);

}
