package com.ureka.team3.utong_backend.datatrade.service.trade.queue;

import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;

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

    void removeFromBuyQueue(BuyDataRequest buyOrderById);

    void removeFromSaleQueue(SaleDataRequest saleOrderById);
}
