package com.ureka.team3.utong_backend.datatrade.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderRedisDto;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataTradeServiceTest {

    @Autowired
    private DataTradeService dataTradeService;

    @Autowired
    private SaleDataRequestRepository saleDataRequestRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final String dataCode = "001";

    @BeforeEach
    void setup() {
        // 테스트용 계정 생성
        Account account = Account.builder()
                .id("test-user")
                .email("test@test.com")
                .build();
        accountRepository.save(account);
    }

    @AfterEach
    void cleanup() {
        // Redis 삭제
        Set<String> keys = redisTemplate.keys("order_queue:sell:*");
        if (keys != null) redisTemplate.delete(keys);
        keys = redisTemplate.keys("orderbook:sell:*");
        if (keys != null) redisTemplate.delete(keys);
    }

    @Test
    void requestSale_정상동작_그리고_Redis_저장됨() throws Exception {
        // given
        DataTradeDto.SaleDataRequestDto dto = DataTradeDto.SaleDataRequestDto.builder()
                .price(8900L)
                .dataAmount(10L)
                .dataCode(dataCode).
                build();

        // when
        ApiResponse response = dataTradeService.requestSale("test-user", dto);

        // then
        String listKey = "order_queue:sell:" + dataCode + ":" + dto.getPrice();
        String zsetKey = "orderbook:sell:" + dataCode;

        // Redis List에 저장된 JSON
        String redisJson = redisTemplate.opsForList().leftPop(listKey);
        assertNotNull(redisJson, "Redis에 저장된 주문이 없음");

        OrderRedisDto order = objectMapper.readValue(redisJson, OrderRedisDto.class);

        assertEquals(dto.getDataAmount(), order.getQuantity());
        assertTrue(order.getCreatedAt() > 0);
        assertTrue(order.getExpiredAt() > order.getCreatedAt());

        // ZSet에 listKey가 저장되어 있는지 확인
        Set<String> zset = redisTemplate.opsForZSet().range(zsetKey, 0, -1);
        assertTrue(zset.contains(listKey));
    }
}
