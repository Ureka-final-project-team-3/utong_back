package com.ureka.team3.utong_backend.datatrade.service.trade.sale;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;

public interface SaleRequestService {
    SaleDataRequest save(Account account, DataTradeDto.DataTradeRequestDto dto);

    SaleDataRequest findSaleOrderById(String saleOrderId);

    void subtractSell(SaleDataRequest saleDataRequest, Long amount);

    void changeStatus(SaleDataRequest saleOrderById, SaleOrderResult saleOrderResult);
}
