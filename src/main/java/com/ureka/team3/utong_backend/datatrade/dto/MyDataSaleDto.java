package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 본인 데이터 판매 내역 조회
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyDataSaleDto {
    private String saleId;
    private String Status;      // 판매예약 / 거래완료
    private String dataCode;         // LTE / 5G
    private Long quantity;           // 판매량
    private LocalDateTime tradeDate; // 거래일 (또는 등록일)
    private Long pricePerGb;         // 1GB당 가격
}
