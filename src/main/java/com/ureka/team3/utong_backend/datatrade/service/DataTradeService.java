package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;

public interface DataTradeService {

    ApiResponse requestBuy(Account account, DataTradeDto.BuyDataRequestDto buyRequestDto);

    ApiResponse requestSale(Account account, DataTradeDto.SaleDataRequestDto saleRequestDto);
}
