package com.ureka.team3.utong_backend.datatrade.facade;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;

public interface DataTradeFacade {

    ApiResponse requestBuy(Account account, DataTradeDto.DataTradeRequestDto buyRequestDto);

    ApiResponse requestSale(Account account, DataTradeDto.DataTradeRequestDto saleRequestDto);
}
