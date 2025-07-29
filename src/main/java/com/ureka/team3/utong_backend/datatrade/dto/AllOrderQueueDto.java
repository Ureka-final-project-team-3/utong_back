package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AllOrderQueueDto {
    private AllSaleOrderQueueDto saleOrderQueueDto;
    private AllBuyOrderQueueDto buyOrderQueueDto;
}
