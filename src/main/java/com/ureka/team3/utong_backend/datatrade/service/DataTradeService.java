package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;

public interface DataTradeService {

    ApiResponse requestBuy(String username, DataTradeDto.BuyDataRequestDto purchaseRequestDto);

    ApiResponse requestSale(String username, DataTradeDto.SaleDataRequestDto saleRequestDto);
}
