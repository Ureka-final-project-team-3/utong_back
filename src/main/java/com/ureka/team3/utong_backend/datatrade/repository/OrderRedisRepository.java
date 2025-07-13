package com.ureka.team3.utong_backend.datatrade.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.OrderMQDto;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRedisRepository implements OrderMQRepository{
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void savePurchaseOrder(OrderMQDto dto) {
        String listKey = buildBuyListKey(dto.getDataCode(), dto.getPrice());
        String zsetKey = buildBuyZSetKey(dto.getDataCode());

        String json = toJson(dto);
        stringRedisTemplate.opsForList().rightPush(listKey, json);
        stringRedisTemplate.opsForZSet().add(zsetKey, listKey, dto.getPrice());
    }

    @Override
    public void saveSellOrder(OrderMQDto dto) {
        String listKey = buildSellListKey(dto.getDataCode(), dto.getPrice());
        String zsetKey = buildSellZSetKey(dto.getDataCode());

        String json = toJson(dto);
        stringRedisTemplate.opsForList().rightPush(listKey, json);
        stringRedisTemplate.opsForZSet().add(zsetKey, listKey, dto.getPrice());
    }

    @Override
    public List<OrderMQDto> findSellOrdersByPrice(String dataCode, long price) {
        String listKey = buildSellListKey(dataCode, price);
        List<String> rawOrders = stringRedisTemplate.opsForList().range(listKey, 0, -1);

        if (rawOrders == null || rawOrders.isEmpty()) return List.of();

        List<OrderMQDto> orderMQDtoList = new ArrayList<>();
        for (String order : rawOrders) {
            try {
                orderMQDtoList.add(objectMapper.readValue(order, OrderMQDto.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 파싱 실패: " + order, e);
            }
        }

        return orderMQDtoList;
    }

    @Override
    public Long getLowestSellPrice(String dataCode) {
        String zsetKey = RedisKeyUtil.buildSellZSetKey(dataCode);

        // 가격이 낮은 순으로 1개 가져오기
        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet().rangeWithScores(zsetKey, 0, 0);

        if (result == null || result.isEmpty()) {
            return null;
        }

        ZSetOperations.TypedTuple<String> tuple = result.iterator().next();
        Double price = tuple.getScore();
        return price != null ? price.longValue() : null;
    }

    @Override
    public OrderMQDto popValidSellOrder(String dataCode, long price) {
        String listKey = buildSellListKey(dataCode, price);
        String zsetKey = buildSellZSetKey(dataCode);

        while (true) {
            String rawOrder = stringRedisTemplate.opsForList().leftPop(listKey);
            if (rawOrder == null) {
                // 리스트 완전히 비었으면 ZSet에서도 제거
                stringRedisTemplate.opsForZSet().remove(zsetKey, listKey);
                return null;
            }

            try {
                OrderMQDto order = objectMapper.readValue(rawOrder, OrderMQDto.class);
                if (order.getExpiredAt() >= System.currentTimeMillis()) {
                    // 유효한 주문이면 반환
                    return order;
                }
            } catch (JsonProcessingException e) {
            }
        }
    }


    @Override
    public void requeuePartialSellOrder(OrderMQDto order) {
        String listKey = buildSellListKey(order.getDataCode(), order.getPrice());
        String zsetKey = buildSellZSetKey(order.getDataCode());

        try {
            String updatedOrder = objectMapper.writeValueAsString(order);
            stringRedisTemplate.opsForList().leftPush(listKey, updatedOrder);
            stringRedisTemplate.opsForZSet().add(zsetKey, listKey, (double) order.getPrice());
        } catch (JsonProcessingException e) {
            // 로깅만 하고 무시 (데이터 유실 위험은 있지만 어쩔 수 없음)
            System.err.println("⚠️ 재직렬화 실패: " + e.getMessage());
        }
    }

    @Override
    public Long getHighestBuyPrice(String dataCode) {
        String zsetKey = RedisKeyUtil.buildBuyZSetKey(dataCode);

        // 가격이 높은 순으로 1개 가져오기
        Set<ZSetOperations.TypedTuple<String>> result =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(zsetKey, 0, 0);

        if (result == null || result.isEmpty()) {
            return null;
        }

        ZSetOperations.TypedTuple<String> tuple = result.iterator().next();
        Double price = tuple.getScore();
        return price != null ? price.longValue() : null;
    }

    @Override
    public OrderMQDto popValidBuyOrder(String dataCode, Long highestBuyPrice) {
        String listKey = buildBuyListKey(dataCode, highestBuyPrice);
        String zsetKey = buildBuyZSetKey(dataCode);

        while (true) {
            String rawOrder = stringRedisTemplate.opsForList().leftPop(listKey);
            if (rawOrder == null) {
                // 리스트 완전히 비었으면 ZSet에서도 제거
                stringRedisTemplate.opsForZSet().remove(zsetKey, listKey);
                return null;
            }

            try {
                OrderMQDto order = objectMapper.readValue(rawOrder, OrderMQDto.class);
                if (order.getExpiredAt() >= System.currentTimeMillis()) {
                    // 유효한 주문이면 반환
                    return order;
                }
            } catch (JsonProcessingException e) {
                log.error("JSON 파싱 실패: {}", rawOrder, e);
            }
        }
    }

    @Override
    public void requeuePartialBuyOrder(OrderMQDto buyOrder) {
        String listKey = buildBuyListKey(buyOrder.getDataCode(), buyOrder.getPrice());
        String zsetKey = buildBuyZSetKey(buyOrder.getDataCode());

        try {
            String updatedOrder = objectMapper.writeValueAsString(buyOrder);
            stringRedisTemplate.opsForList().leftPush(listKey, updatedOrder);
            stringRedisTemplate.opsForZSet().add(zsetKey, listKey, (double) buyOrder.getPrice());
        } catch (JsonProcessingException e) {
            // 로깅만 하고 무시 (데이터 유실 위험은 있지만 어쩔 수 없음)
            System.err.println("⚠️ 재직렬화 실패: " + e.getMessage());
        }
    }


    private long calculateTTL(long expiredAtMillis) {
        long now = System.currentTimeMillis();
        return Math.max(0, (expiredAtMillis - now) / 1000);
    }

    private String toJson(OrderMQDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 직렬화 실패", e);
        }
    }
}
