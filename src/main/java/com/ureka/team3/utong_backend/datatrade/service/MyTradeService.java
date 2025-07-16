package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.MyDataPurchaseDto;
import com.ureka.team3.utong_backend.datatrade.dto.MyDataSaleDto;

import java.util.List;

public interface MyTradeService {

    // 본인 데이터 구매 내역
    List<MyDataPurchaseDto> getMyPurchases(Account account, String range);

    // 본인 데이터 판매 내역
    List<MyDataSaleDto> getMySales(Account account, String range);
}
