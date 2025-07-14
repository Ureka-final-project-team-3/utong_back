package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.dto.OrderMQDto;
import org.springframework.stereotype.Repository;

import java.util.List;

// kafka로 구현해야함
public class OrderKafkaRepository implements OrderMQRepository{
    @Override
    public void savePurchaseOrder(OrderMQDto dto) {

    }

    @Override
    public void saveSellOrder(OrderMQDto dto) {

    }

    @Override
    public List<OrderMQDto> findSellOrdersByPrice(String dataCode, long price) {
        return List.of();
    }

    @Override
    public Long getLowestSellPrice(String dataCode) {
        return 0L;
    }

    @Override
    public OrderMQDto popValidSellOrder(String dataCode, long price) {
        return null;
    }

    @Override
    public void requeuePartialSellOrder(OrderMQDto order) {

    }

    @Override
    public Long getHighestBuyPrice(String dataCode) {
        return 0L;
    }

    @Override
    public OrderMQDto popValidBuyOrder(String dataCode, Long highestBuyPrice) {
        return null;
    }

    @Override
    public void requeuePartialBuyOrder(OrderMQDto buyOrder) {

    }
}
