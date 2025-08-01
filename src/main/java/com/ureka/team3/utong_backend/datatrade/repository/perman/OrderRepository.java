package com.ureka.team3.utong_backend.datatrade.repository.perman;

import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;

import java.util.List;
import java.util.Map;

public interface OrderRepository {
    // 구매 대기열 등록
    void savePurchaseOrder(OrderDto dto);

    // 판매 대기열 등록
    void saveSellOrder(OrderDto dto);
    // LTE, 5G를 선택하여 해당 데이터 종류의 최저가 판매 요청 반환

    List<OrderDto> findSellOrdersByPrice(String dataCode, long price);

    // LTE, 5G를 선택하여 해당 데이터 종류의 최저가 반환
    Long getLowestSellPrice(String dataCode);

    OrderDto popValidSellOrder(String dataCode, long price);

    void requeuePartialSellOrder(OrderDto order);

    Long getHighestBuyPrice(String dataCode);

    OrderDto popValidBuyOrder(String dataCode, Long highestBuyPrice);

    void requeuePartialBuyOrder(OrderDto buyOrder);

    OrderDto popFirstSellOrderFromList(String dataCode, long price);

    OrderDto popFirstBuyOrderFromList(String dataCode, long price);

    Map<Long, Long> getAllSellOrderNumbers(String dataCode);

    Map<Long, Long> getAllBuyOrderNumbers(String dataCode);

    void removeFromBuyQueue(BuyDataRequest buyOrderById);

    void removeFromSaleQueue(SaleDataRequest saleOrderById);

    Map<Long, List<OrderDto>> findAllSellOrders(String dataCode);

    Map<Long, List<OrderDto>> findAllBuyOrders(String dataCode);
}
