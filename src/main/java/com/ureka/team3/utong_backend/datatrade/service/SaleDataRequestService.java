package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;

public interface SaleDataRequestService {
    SaleDataRequest save(Account account, DataTradeDto.SaleDataRequestDto dto);

    SaleDataRequest findSaleOrderById(String saleOrderId);

    boolean existsWaitingSaleRequest(String lineId);

    void changeStatusToAllComplete(SaleDataRequest saved);

    void changeStatusToPartComplete(SaleDataRequest saved);
}
