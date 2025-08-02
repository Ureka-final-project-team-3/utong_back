package com.ureka.team3.utong_backend.datatrade.service.trade.sale;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.exception.business.OrderNotFoundException;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;
import com.ureka.team3.utong_backend.datatrade.repository.perman.SaleRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SaleRequestServiceImpl implements SaleRequestService {
    private final SaleRequestRepository saleRequestRepository;
    private final DataTradePolicy dataTradePolicy;

    @Override
    public SaleDataRequest save(Account account, DataTradeDto.DataTradeRequestDto dto) {
        return saleRequestRepository.save(SaleDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .status(dataTradePolicy.getStatusCode(SaleOrderResult.WAITING.name()).getCode())
                .remaining(dto.getDataAmount())
                .lineId(account.getDefaultLine())
                .build());
    }

    @Override
    public SaleDataRequest findSaleOrderById(String saleOrderId) {
        return saleRequestRepository.findById(saleOrderId).orElseThrow(OrderNotFoundException::new);
    }

    @Override
    public void subtractSell(SaleDataRequest saved, Long amount) {
        saved.subtractRemain(amount);
    }

    @Override
    public void changeStatus(SaleDataRequest saleOrderById, SaleOrderResult saleOrderResult) {
        saleOrderById.changeStatus(dataTradePolicy.getStatusCode(saleOrderResult.name()).getCode());
    }
}
