package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SaleMatch {
    private OrderDto matchedOrder;
    private Long amount;
    private Long pricePerUnit;

    public static SaleMatch of(OrderDto buyOrder, long used) {
        return SaleMatch.builder()
                .matchedOrder(buyOrder)
                .pricePerUnit(buyOrder.getPrice())
                .amount(used)
                .build();
    }
}
