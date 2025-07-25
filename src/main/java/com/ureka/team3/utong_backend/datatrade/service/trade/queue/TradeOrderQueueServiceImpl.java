package com.ureka.team3.utong_backend.datatrade.service.trade.queue;

import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ureka.team3.utong_backend.datatrade.utils.TimeUtil.toEpochMillis;

@Service
@RequiredArgsConstructor
public class TradeOrderQueueServiceImpl implements TradeOrderQueueService {
    private final OrderRepository orderRepository;

    @Override
    public Long getLoweSellPriceByDataCode(String dataCode) {
        return orderRepository.getLowestSellPrice(dataCode);
    }

    @Override
    public OrderDto popValidSellOrder(String dataCode, Long targetPrice) {
        return orderRepository.popValidSellOrder(dataCode, targetPrice);
    }

    @Override
    public void requeuePartialSellOrder(OrderDto sellOrder) {
        orderRepository.requeuePartialSellOrder(sellOrder);
    }

    @Override
    public void addToBuyOrderQueue(BuyDataRequest order, long remain) {
        OrderDto orderDto = OrderDto.builder()
                .orderId(order.getId())
                .createdAt(toEpochMillis(order.getCreatedAt()))
                .expiredAt(toEpochMillis(order.getExpiredAt()))
                .quantity(remain)
                .dataCode(order.getDataCode())
                .price(order.getPrice())
                .build();
        orderRepository.savePurchaseOrder(orderDto);
    }

    @Override
    public void addToSaleOrderQueue(SaleDataRequest order, long remain) {
        OrderDto orderDto = OrderDto.builder()
                .orderId(order.getId())
                .createdAt(toEpochMillis(order.getCreatedAt()))
                .expiredAt(toEpochMillis(order.getExpiredAt()))
                .quantity(remain)
                .dataCode(order.getDataCode())
                .price(order.getPrice())
                .build();
        orderRepository.saveSellOrder(orderDto);
    }

    @Override
    public OrderDto peekValidSellOrder(String dataCode, Long targetPrice) {
        return null;
    }

    @Override
    public Long getHighestBuyPrice(String dataCode) {
        return orderRepository.getHighestBuyPrice(dataCode);
    }

    @Override
    public OrderDto popValidBuyOrder(String dataCode, Long priceLimit) {
        return orderRepository.popValidBuyOrder(dataCode, priceLimit);
    }

    @Override
    public void requeuePartialBuyOrder(OrderDto buyOrder) {
        orderRepository.requeuePartialBuyOrder(buyOrder);
    }

    @Override
    public void removeFromBuyQueue(BuyDataRequest buyOrderById) {
        orderRepository.removeFromBuyQueue(buyOrderById);
    }

    @Override
    public void removeFromSaleQueue(SaleDataRequest saleOrderById) {
        orderRepository.removeFromSaleQueue(saleOrderById);
    }
}
