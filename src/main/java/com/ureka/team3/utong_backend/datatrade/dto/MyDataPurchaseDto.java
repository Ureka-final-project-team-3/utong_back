package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 본인 데이터 구매 내역 조회
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyDataPurchaseDto {
    private String purchaseId;
    private String tradeStatus;      // 거래대기 / 거래완료
    private String dataType;         // LTE / 5G
    private Long quantity;           // 구매량
    private LocalDateTime tradeDate; // 거래일 (또는 요청일)
    private Long pricePerGb;         // 1GB당 가격
}
