package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.exception.business.OrderNotFoundException;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleDataRequestServiceImpl implements SaleDataRequestService {
    private final SaleDataRequestRepository saleDataRequestRepository;

    @Override
    public SaleDataRequest save(Account account, DataTradeDto.SaleDataRequestDto dto) {
        return saleDataRequestRepository.save(SaleDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .status("003")
                .remaining(dto.getDataAmount())
                .lineId(account.getDefaultLine())
                .build());
    }

    @Override
    public SaleDataRequest findSaleOrderById(String saleOrderId) {
        return saleDataRequestRepository.findById(saleOrderId).orElseThrow(OrderNotFoundException::new);
    }

    @Override
    public void subtractSell(SaleDataRequest saved, Long amount) {
        saved.subtractRemain(amount);
    }
}
