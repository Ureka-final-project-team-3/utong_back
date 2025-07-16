package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.datatrade.dto.TradeExecutionDto;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContractServiceImpl implements ContractService {
    private final ContractRepository contractRepository;

    @Override
    public Contract save(TradeExecutionDto tradeExecutionDto) {

        Contract contract = Contract.builder()
                .saleDataRequest(tradeExecutionDto.getSaleOrder())
                .buyDataRequest(tradeExecutionDto.getBuyOrder())
                .price(tradeExecutionDto.getPricePerUnit())
                .amount(tradeExecutionDto.getQuantity())
                .build();

        return contractRepository.save(contract);
    }
}
