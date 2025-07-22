package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Builder
public class OrderExceptTimeDto {
    private String orderId;     // 주문 ID
    private long quantity;      // 데이터 용량 (GB 등)
    private long price;
    private String dataCode;
}
