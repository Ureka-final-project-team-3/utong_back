package com.ureka.team3.utong_backend.datatrade.service.queue;

import com.ureka.team3.utong_backend.datatrade.dto.OrdersQueueDto;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueueStatusServiceImpl implements OrderQueueStatusService {
    private final OrderRepository orderRepository;

    @Override
    public OrdersQueueDto getInitData(String dataCode) {
        Map<Long, Long> allBuyOrderNumbers = orderRepository.getAllBuyOrderNumbers(dataCode);
        Map<Long, Long> allSellOrderNumbers = orderRepository.getAllSellOrderNumbers(dataCode);
        return OrdersQueueDto.builder()
                .buyOrderQuantity(allBuyOrderNumbers)
                .sellOrderQuantity(allSellOrderNumbers)
                .build();
    }
}
