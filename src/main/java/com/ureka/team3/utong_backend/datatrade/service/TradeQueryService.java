package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.TradeHistoryRequestDto;

public interface TradeQueryService {

    // 본인 데이터 구매 내역
    ApiResponse getMyPurchases(Account account, TradeHistoryRequestDto requestDto);

    // 본인 데이터 판매 내역
    ApiResponse getMySales(Account account, TradeHistoryRequestDto requestDto);
}
