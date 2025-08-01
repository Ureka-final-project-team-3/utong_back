package com.ureka.team3.utong_backend.datatrade.service.trade.purchase;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.exception.business.OrderNotFoundException;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyDataRequestServiceImpl implements BuyDataRequestService {
    private final BuyDataRequestRepository buyDataRequestRepository;
    private final DataTradePolicy dataTradePolicy;

    @Override
    public BuyDataRequest save(Account account, DataTradeDto.DataTradeRequestDto dto) {
        return buyDataRequestRepository.save(BuyDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .lineId(account.getDefaultLine())
                .status(dataTradePolicy.getStatusCode(BuyOrderResult.WAITING.name()).getCode())
                .remaining(dto.getDataAmount())
                .build());
    }

    @Override
    public BuyDataRequest findBuyOrderById(String buyOrderId) {
        return buyDataRequestRepository.findById(buyOrderId).orElseThrow(OrderNotFoundException::new);
    }

    @Override
    public void subtractPurchased(BuyDataRequest saved, long quantity) {
        saved.subtractRemain(quantity);
    }

    @Override
    public void changeStatus(BuyDataRequest buyOrderById, BuyOrderResult buyOrderResult) {
        buyOrderById.changeStatus(dataTradePolicy.getStatusCode(buyOrderResult.name()).getCode());
    }
}
