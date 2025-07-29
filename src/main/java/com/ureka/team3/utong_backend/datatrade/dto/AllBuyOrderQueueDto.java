package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AllBuyOrderQueueDto {
    private Map<Long, List<OrderExceptTimeDto>> LteBuyOrders;
    private Map<Long, List<OrderExceptTimeDto>> _5gBuyOrders;
}
