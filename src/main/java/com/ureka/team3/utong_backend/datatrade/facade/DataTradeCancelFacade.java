package com.ureka.team3.utong_backend.datatrade.facade;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import org.springframework.transaction.annotation.Transactional;

public interface DataTradeCancelFacade {
    @Transactional
    ApiResponse cancelBuyWaiting(Account account, DataTradeDto.CancelWaitingTradeRequestDto requestDto);

    @Transactional
    ApiResponse cancelSaleWaiting(Account account, DataTradeDto.CancelWaitingTradeRequestDto requestDto);
}
