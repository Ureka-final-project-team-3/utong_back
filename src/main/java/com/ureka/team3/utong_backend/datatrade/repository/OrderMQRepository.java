package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.dto.OrderMQDto;

import java.util.List;

public interface OrderMQRepository {
    // 구매 대기열 등록
    void savePurchaseOrder(OrderMQDto dto);
    // 판매 대기열 등록
    void saveSellOrder(OrderMQDto dto);
    // LTE, 5G를 선택하여 해당 데이터 종류의 최저가 판매 요청 반환

    List<OrderMQDto> findSellOrdersByPrice(String dataCode, long price);

    // LTE, 5G를 선택하여 해당 데이터 종류의 최저가 반환
    Long getLowestSellPrice(String dataCode);

    OrderMQDto popValidSellOrder(String dataCode, long price);

    void requeuePartialSellOrder(OrderMQDto order);

    Long getHighestBuyPrice(String dataCode);

    OrderMQDto popValidBuyOrder(String dataCode, Long highestBuyPrice);

    void requeuePartialBuyOrder(OrderMQDto buyOrder);
}
