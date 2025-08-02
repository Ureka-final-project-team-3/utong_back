package com.ureka.team3.utong_backend.datatrade.dto.query;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractResponseDto {
    // 각 거래 내역 ( 거래 성사 시간, 단위 가격, 거래 수량)
    private LocalDateTime contractDate;
    private Long pricePerUnit;
    private Long contractQuantity;
}
