package com.ureka.team3.utong_backend.datatrade.dto.trade;

import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TradeExecutionDto {
    private final BuyDataRequest buyOrder;
    private final SaleDataRequest saleOrder;
    private final long quantity;       // 체결 수량
    private final long pricePerUnit;   // 단가
}
