package com.ureka.team3.utong_backend.datatrade.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// 본인 데이터 판매 내역 조회
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleResponseDto {
    private String saleId;
    private String status;      // 판매예약 / 거래완료
    private String dataCode;         // LTE / 5G
    private Long quantity;           // 판매량
    private Long remaining;
    private LocalDateTime requestDate; // 거래일 (또는 등록일)
    private Long pricePerGb;         // 1GB당 가격
    private String phoneNumber;
    private Long totalPay;

    // 부분 체결된 계약 내역 리스트
    private List<ContractResponseDto> contractDto;
}
