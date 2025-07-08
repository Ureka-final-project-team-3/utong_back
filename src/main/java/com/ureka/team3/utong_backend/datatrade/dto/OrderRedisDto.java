package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRedisDto {
    private String orderId;     // 주문 ID
    private long quantity;      // 데이터 용량 (GB 등)
    private long createdAt;
    private long expiredAt;
}
