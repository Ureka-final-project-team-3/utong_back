package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderRedisDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRedisRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.ureka.team3.utong_backend.datatrade.utils.TimeUtil.toEpochMillis;

@Service
@RequiredArgsConstructor
public class DataTradeServiceImpl implements DataTradeService {
    private final SaleDataRequestRepository saleDataRequestRepository;
    private final BuyDataRequestRepository buyDataRequestRepository;
    private final AccountRepository accountRepository;
    private final OrderRedisRepository orderRedisRepository;

    @Override
    public ApiResponse requestBuy(Account account, DataTradeDto.BuyDataRequestDto buyRequestDto) {
        // 1. 구매 요청 저장
        BuyDataRequest saved = buyDataRequestRepository.save(
                BuyDataRequest.builder()
                        .price(buyRequestDto.getPrice())
                        .account(account)
                        .quantity(buyRequestDto.getDataAmount())
                        .dataCode(buyRequestDto.getDataCode())
                        .build()
        );

        // 2. 구매 요청 대기열에 추가
        OrderRedisDto dto = OrderRedisDto.builder()
                .orderId(saved.getId())
                .createdAt(toEpochMillis(saved.getCreatedAt()))
                .expiredAt(toEpochMillis(saved.getExpiredAt()))
                .quantity(saved.getQuantity())
                .dataCode(saved.getDataCode())
                .price(saved.getPrice())
                .build();

        orderRedisRepository.savePurchaseOrder(dto);
        return ApiResponse.success("구매 등록 완료", saved.getId());
    }

    @Override
    public ApiResponse requestSale(Account account, DataTradeDto.SaleDataRequestDto saleRequestDto) {
        // 1. 판매 요청 저장
        SaleDataRequest saved = saleDataRequestRepository.save(
                SaleDataRequest.builder()
                        .price(saleRequestDto.getPrice())
                        .account(account)
                        .quantity(saleRequestDto.getDataAmount())
                        .dataCode(saleRequestDto.getDataCode())
                        .build()
        );

        // 2. 판매 요청 대기열에 추가
        OrderRedisDto dto = OrderRedisDto.builder()
                .orderId(saved.getId())
                .createdAt(toEpochMillis(saved.getCreatedAt()))
                .expiredAt(toEpochMillis(saved.getExpiredAt()))
                .quantity(saved.getQuantity())
                .dataCode(saved.getDataCode())
                .price(saved.getPrice())
                .build();

        orderRedisRepository.saveSellOrder(dto);

        return ApiResponse.success("판매 등록 완료", saved.getId());
    }
}
