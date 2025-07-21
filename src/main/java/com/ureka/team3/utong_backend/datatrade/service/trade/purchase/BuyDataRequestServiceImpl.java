package com.ureka.team3.utong_backend.datatrade.service.trade.purchase;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.exception.business.OrderNotFoundException;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyDataRequestServiceImpl implements BuyDataRequestService {
    private final BuyDataRequestRepository buyDataRequestRepository;

    @Override
    public BuyDataRequest save(Account account, DataTradeDto.BuyDataRequestDto dto) {
        return buyDataRequestRepository.save(BuyDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .lineId(account.getDefaultLine())
                .status("003")
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
}
