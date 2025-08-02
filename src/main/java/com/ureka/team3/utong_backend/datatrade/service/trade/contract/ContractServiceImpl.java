package com.ureka.team3.utong_backend.datatrade.service.trade.contract;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ureka.team3.utong_backend.datatrade.dto.trade.TradeExecutionDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.repository.perman.ContractRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {
    
    private final ContractRepository contractRepository;

    @Override
    @Transactional
    public Contract save(TradeExecutionDto tradeExecutionDto) {
        Contract contract = Contract.builder()
                .saleDataRequest(tradeExecutionDto.getSaleOrder())
                .buyDataRequest(tradeExecutionDto.getBuyOrder())
                .price(tradeExecutionDto.getPricePerUnit())
                .amount(tradeExecutionDto.getQuantity())
                .build();

        Contract savedContract = contractRepository.save(contract);
        

        return savedContract;
    }
    
    
}