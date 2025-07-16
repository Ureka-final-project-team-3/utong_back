package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;

public interface TradeOrderQueueService {
    Long getLoweSellPriceByDataCode(String dataCode);

    OrderDto popValidSellOrder(String dataCode, Long targetPrice);

    void requeuePartialSellOrder(OrderDto sellOrder);

    void addToBuyOrderQueue(BuyDataRequest order, long remain);

    void addToSaleOrderQueue(SaleDataRequest order, long remain);

    OrderDto peekValidSellOrder(String dataCode, Long targetPrice);

    Long getHighestBuyPrice(String dataCode);

    OrderDto popValidBuyOrder(String dataCode, Long priceLimit);

    void requeuePartialBuyOrder(OrderDto buyOrder);
}
