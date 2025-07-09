package com.ureka.team3.utong_backend.datatrade.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.OrderRedisDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import static com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil.*;

@Repository
@RequiredArgsConstructor
public class OrderRedisRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void savePurchaseOrder(OrderRedisDto dto) {
        String listKey = buildBuyListKey(dto.getDataCode(), dto.getPrice());
        String zsetKey = buildBuyZSetKey(dto.getDataCode());

        String json = toJson(dto);
        stringRedisTemplate.opsForList().rightPush(listKey, json);
        stringRedisTemplate.opsForZSet().add(zsetKey, listKey, dto.getPrice());
    }

    public void saveSellOrder(OrderRedisDto dto) {
        String listKey = buildSellListKey(dto.getDataCode(), dto.getPrice());
        String zsetKey = buildSellZSetKey(dto.getDataCode());

        String json = toJson(dto);
        stringRedisTemplate.opsForList().rightPush(listKey, json);
        stringRedisTemplate.opsForZSet().add(zsetKey, listKey, dto.getPrice());
    }

    private long calculateTTL(long expiredAtMillis) {
        long now = System.currentTimeMillis();
        return Math.max(0, (expiredAtMillis - now) / 1000);
    }

    private String toJson(OrderRedisDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 직렬화 실패", e);
        }
    }
}
