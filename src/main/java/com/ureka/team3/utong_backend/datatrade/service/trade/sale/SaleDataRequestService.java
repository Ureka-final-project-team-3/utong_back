package com.ureka.team3.utong_backend.datatrade.service.trade.sale;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;

public interface SaleDataRequestService {
    SaleDataRequest save(Account account, DataTradeDto.SaleDataRequestDto dto);

    SaleDataRequest findSaleOrderById(String saleOrderId);

    void subtractSell(SaleDataRequest saleDataRequest, Long amount);

    void changeStatus(SaleDataRequest saleOrderById, SaleOrderResult saleOrderResult);
}
