package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;

import java.util.List;
import java.util.Map;

// kafka로 구현해야함
public class OrderKafkaRepository implements OrderRepository {
    @Override
    public void savePurchaseOrder(OrderDto dto) {

    }

    @Override
    public void saveSellOrder(OrderDto dto) {

    }

    @Override
    public List<OrderDto> findSellOrdersByPrice(String dataCode, long price) {
        return List.of();
    }

    @Override
    public Long getLowestSellPrice(String dataCode) {
        return 0L;
    }

    @Override
    public OrderDto popValidSellOrder(String dataCode, long price) {
        return null;
    }

    @Override
    public void requeuePartialSellOrder(OrderDto order) {

    }

    @Override
    public Long getHighestBuyPrice(String dataCode) {
        return 0L;
    }

    @Override
    public OrderDto popValidBuyOrder(String dataCode, Long highestBuyPrice) {
        return null;
    }

    @Override
    public void requeuePartialBuyOrder(OrderDto buyOrder) {

    }

    @Override
    public OrderDto popFirstSellOrderFromList(String dataCode, long price) {
        return null;
    }

    @Override
    public OrderDto popFirstBuyOrderFromList(String dataCode, long price) {
        return null;
    }

    @Override
    public Map<Long, Long> getAllSellOrderNumbers(String dataCode) {
        return Map.of();
    }

    @Override
    public Map<Long, Long> getAllBuyOrderNumbers(String dataCode) {
        return Map.of();
    }

    @Override
    public void removeFromBuyQueue(BuyDataRequest buyOrderById) {

    }

    @Override
    public void removeFromSaleQueue(SaleDataRequest saleOrderById) {
        
    }
}
