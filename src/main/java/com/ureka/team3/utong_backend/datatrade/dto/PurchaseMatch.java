package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PurchaseMatch {
    private OrderDto matchedOrder;
    private Long amount;
    private Long pricePerUnit;

    public static PurchaseMatch of(OrderDto sellOrder, long used) {
        return PurchaseMatch.builder()
                .matchedOrder(sellOrder)
                .pricePerUnit(sellOrder.getPrice())
                .amount(used)
                .build();
    }
}
