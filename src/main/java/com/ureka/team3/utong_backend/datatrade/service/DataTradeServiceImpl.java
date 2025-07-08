package com.ureka.team3.utong_backend.datatrade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderRedisDto;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class DataTradeServiceImpl implements DataTradeService {
    private final SaleDataRequestRepository saleDataRequestRepository;
    private final BuyDataRequestRepository buyDataRequestRepository;
    private final AccountRepository accountRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public ApiResponse requestBuy(String username, DataTradeDto.BuyDataRequestDto purchaseRequestDto) {
        // 구현 예정
        return null;
    }

    @Override
    public ApiResponse requestSale(String username, DataTradeDto.SaleDataRequestDto saleRequestDto) {
        // 1. 사용자 조회 및 DB 저장
        Account account = accountRepository.findById(username)
                .orElseThrow(UserNotFoundException::new);

        SaleDataRequest saleDataRequest = SaleDataRequest.builder()
                .price(saleRequestDto.getPrice())
                .account(account)
                .quantity(saleRequestDto.getDataAmount())
                .dataCode(saleRequestDto.getDataCode())
                .build();

        saleDataRequestRepository.save(saleDataRequest);

        // 2. Redis 키 설정
        String redisDataCode = saleRequestDto.getDataCode();
        long price = saleRequestDto.getPrice();

        String listKey = "order_queue:sell:" + redisDataCode + ":" + price;
        String zsetKey = "orderbook:sell:" + redisDataCode;

        // 3. 시간 계산
        long createdAt = saleDataRequest.getCreatedAt()
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long expiredAt = saleDataRequest.getExpiredAt()
                .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long now = System.currentTimeMillis();
        long ttlSeconds = (expiredAt - now) / 1000;

        // 4. Redis DTO 구성
        OrderRedisDto orderRedisDto = OrderRedisDto.builder()
                .orderId(saleDataRequest.getId())
                .createdAt(createdAt)
                .expiredAt(expiredAt)
                .quantity(saleRequestDto.getDataAmount())
                .build();

        try {
            String json = objectMapper.writeValueAsString(orderRedisDto);

            // 5. Redis 저장 (List + ZSet)
            redisTemplate.opsForList().rightPush(listKey, json);
            redisTemplate.opsForZSet().add(zsetKey, listKey, price);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 직렬화 실패", e);
        }

        // 7. 응답 반환
        return ApiResponse.success("판매 대기 등록 완료", saleDataRequest.getId());
    }
}
