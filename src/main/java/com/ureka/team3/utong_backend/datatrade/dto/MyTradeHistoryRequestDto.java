package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 거래 내역 조회 시 날짜 필터 최근 1주일내, 최근 한 달 내, 최근 일년
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyTradeHistoryRequestDto {
    private String range; // "WEEK", "MONTH", "YEAR"
}
