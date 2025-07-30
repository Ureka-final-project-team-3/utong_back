package com.ureka.team3.utong_backend.datatrade.service.trade.contract;

import com.ureka.team3.utong_backend.datatrade.dto.TradeExecutionDto;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import com.ureka.team3.utong_backend.datatrade.service.notification.ContractNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractServiceImpl implements ContractService {
    
    private final ContractRepository contractRepository;
    private final ContractNotificationService contractNotificationService;

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
        
        sendNotificationsAsync(savedContract);

        return savedContract;
    }
    
    @Async
    private void sendNotificationsAsync(Contract contract) {
        try {
            contractNotificationService.sendContractNotification(contract);
            
            log.info("계약 알림 전송 완료 - 계약 ID: {}", contract.getId());
                    
        } catch (Exception e) {
            log.error("계약 알림 전송 실패 - 계약 ID: {}, 오류: {}", 
                    contract.getId(), e.getMessage(), e);
        }
    }
}