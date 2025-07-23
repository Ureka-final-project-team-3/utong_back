package com.ureka.team3.utong_backend.datatrade.service.queue;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.*;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueueStatusServiceImpl implements OrderQueueStatusService {
    private final OrderRepository orderRepository;
    private final ContractRepository contractRepository;

    @Override
    @Transactional(readOnly = true)
    public OrdersQueueDto getInitData(String dataCode) {
        Map<Long, Long> allBuyOrderNumbers = orderRepository.getAllBuyOrderNumbers(dataCode);
        Map<Long, Long> allSellOrderNumbers = orderRepository.getAllSellOrderNumbers(dataCode);
        List<ContractDto> recentContracts
                = contractRepository.findLatestContractByDataCode(dataCode, DataTradePolicy.CONTRACT_LIST_SIZE)
                .stream()
                .map(contract -> ContractDto.of(contract, dataCode))
                .toList();
//        List<ContractDto> recentContracts = null;

        return OrdersQueueDto.builder()
                .buyOrderQuantity(allBuyOrderNumbers)
                .sellOrderQuantity(allSellOrderNumbers)
                .recentContracts(recentContracts)
                .build();
    }

    @Override
    public ApiResponse getCurrentAllQueue() {
        AllBuyOrderQueueDto buyOrderQueueDto = getAllBuyOrderQueueDto();
        AllSaleOrderQueueDto saleOrderQueueDto = getAllSaleOrderQueueDto();

        return ApiResponse.success(AllOrderQueueDto.builder()
                .buyOrderQueueDto(buyOrderQueueDto)
                .saleOrderQueueDto(saleOrderQueueDto)
                .build());
    }

    private AllBuyOrderQueueDto getAllBuyOrderQueueDto() {
        Map<Long, List<OrderDto>> allLteBuyOrders = orderRepository.findAllBuyOrders("001");
        Map<Long, List<OrderDto>> allBuy5gOrders = orderRepository.findAllBuyOrders("002");

        return AllBuyOrderQueueDto.builder()
                .LteBuyOrders(convertToExceptTimeMap(allLteBuyOrders))
                ._5gBuyOrders(convertToExceptTimeMap(allBuy5gOrders))
                .build();
    }

    private AllSaleOrderQueueDto getAllSaleOrderQueueDto() {
        Map<Long, List<OrderDto>> allLteSellOrders = orderRepository.findAllSellOrders("001");
        Map<Long, List<OrderDto>> allSell5gOrders = orderRepository.findAllSellOrders("002");

        return AllSaleOrderQueueDto.builder()
                .LteSaleOrders(convertToExceptTimeMap(allLteSellOrders))
                ._5gSaleOrders(convertToExceptTimeMap(allSell5gOrders))
                .build();
    }

    private Map<Long, List<OrderExceptTimeDto>> convertToExceptTimeMap(Map<Long, List<OrderDto>> originalMap) {
        return originalMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream()
                                .map(order -> OrderExceptTimeDto.builder()
                                        .orderId(order.getOrderId())
                                        .quantity(order.getQuantity())
                                        .price(order.getPrice())
                                        .dataCode(order.getDataCode())
                                        .build())
                                .collect(Collectors.toList())
                ));
    }

}
