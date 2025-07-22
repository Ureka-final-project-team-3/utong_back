package com.ureka.team3.utong_backend.datatrade.service.queue;

import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.ContractDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrdersQueueDto;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderQueueStatusServiceImpl implements OrderQueueStatusService {
    private final OrderRepository orderRepository;
    private final ContractRepository contractRepository;

    @Override
    public OrdersQueueDto getInitData(String dataCode) {
        Map<Long, Long> allBuyOrderNumbers = orderRepository.getAllBuyOrderNumbers(dataCode);
        Map<Long, Long> allSellOrderNumbers = orderRepository.getAllSellOrderNumbers(dataCode);
        List<ContractDto> recentContracts
                = contractRepository.findLatestContractByDataCode(dataCode, DataTradePolicy.CONTRACT_LIST_SIZE)
                    .stream()
                    .map(contract -> ContractDto.of(contract, dataCode))
                    .toList();

        return OrdersQueueDto.builder()
                .buyOrderQuantity(allBuyOrderNumbers)
                .sellOrderQuantity(allSellOrderNumbers)
                .recentContracts(recentContracts)
                .build();
    }
}
