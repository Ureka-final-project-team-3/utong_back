package com.ureka.team3.utong_backend.datatrade.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.*;

import static com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void savePurchaseOrder(OrderDto dto) {
        String listKey = buildBuyListKey(dto.getDataCode(), dto.getPrice());
        String zsetKey = buildBuyZSetKey(dto.getDataCode());

        String json = toJson(dto);
        stringRedisTemplate.opsForList().rightPush(listKey, json);
        stringRedisTemplate.opsForZSet().add(zsetKey, listKey, dto.getPrice());
    }

    @Override
    public void saveSellOrder(OrderDto dto) {
        String listKey = buildSellListKey(dto.getDataCode(), dto.getPrice());
        String zsetKey = buildSellZSetKey(dto.getDataCode());

        String json = toJson(dto);
        stringRedisTemplate.opsForList().rightPush(listKey, json);
        stringRedisTemplate.opsForZSet().add(zsetKey, listKey, dto.getPrice());
    }

    @Override
    public List<OrderDto> findSellOrdersByPrice(String dataCode, long price) {
        String listKey = buildSellListKey(dataCode, price);
        List<String> rawOrders = stringRedisTemplate.opsForList().range(listKey, 0, -1);

        if (rawOrders == null || rawOrders.isEmpty()) return List.of();

        List<OrderDto> orderDtoList = new ArrayList<>();
        for (String order : rawOrders) {
            try {
                orderDtoList.add(objectMapper.readValue(order, OrderDto.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 파싱 실패: " + order, e);
            }
        }

        return orderDtoList;
    }

    @Override
    public Long getLowestSellPrice(String dataCode) {
        String zsetKey = RedisKeyUtil.buildSellZSetKey(dataCode);

        while (true) {
            // 가격 낮은 순으로 하나 가져오기
            Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet().rangeWithScores(zsetKey, 0, 0);

            if (result == null || result.isEmpty()) {
                return null; // 더 이상 ZSet에 유효한 키 없음
            }

            ZSetOperations.TypedTuple<String> tuple = result.iterator().next();
            String listKey = tuple.getValue();
            Double price = tuple.getScore();

            // 리스트가 비었는지 확인
            Long size = stringRedisTemplate.opsForList().size(listKey);
            if (size == null || size == 0) {
                // 리스트가 비었으면 ZSet에서도 제거
                stringRedisTemplate.opsForZSet().remove(zsetKey, listKey);
                stringRedisTemplate.delete(listKey); // 필요 시 리스트도 삭제
                continue; // 다음 최저 가격 확인
            }

            return price != null ? price.longValue() : null;
        }
    }


    @Override
    public OrderDto popValidSellOrder(String dataCode, long price) {
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
                OrderDto order = objectMapper.readValue(rawOrder, OrderDto.class);
                if (order.getExpiredAt() >= System.currentTimeMillis()) {
                    // 유효한 주문이면 반환
                    return order;
                }
            } catch (JsonProcessingException e) {
            }
        }
    }


    @Override
    public void requeuePartialSellOrder(OrderDto order) {
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

        while (true) {
            // 가격 높은 순으로 1개 가져오기
            Set<ZSetOperations.TypedTuple<String>> result =
                    stringRedisTemplate.opsForZSet().reverseRangeWithScores(zsetKey, 0, 0);

            if (result == null || result.isEmpty()) {
                return null; // ZSet에 유효한 키 없음
            }

            ZSetOperations.TypedTuple<String> tuple = result.iterator().next();
            String listKey = tuple.getValue();
            Double price = tuple.getScore();

            // 리스트가 비었는지 확인
            Long size = stringRedisTemplate.opsForList().size(listKey);
            if (size == null || size == 0) {
                stringRedisTemplate.opsForZSet().remove(zsetKey, listKey);
                stringRedisTemplate.delete(listKey); // 필요 시 리스트도 삭제
                continue; // 다음으로 높은 가격 확인
            }

            return price != null ? price.longValue() : null;
        }
    }


    @Override
    public OrderDto popValidBuyOrder(String dataCode, Long highestBuyPrice) {
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
                OrderDto order = objectMapper.readValue(rawOrder, OrderDto.class);
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
    public void requeuePartialBuyOrder(OrderDto buyOrder) {
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

    @Override
    public OrderDto popFirstSellOrderFromList(String dataCode, long price) {
        String listKey = RedisKeyUtil.buildSellListKey(dataCode, price);
        String rawOrder = stringRedisTemplate.opsForList().leftPop(listKey);

        if (rawOrder == null) return null;

        try {
            return objectMapper.readValue(rawOrder, OrderDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패: " + rawOrder, e);
        }
    }

    @Override
    public OrderDto popFirstBuyOrderFromList(String dataCode, long price) {
        String listKey = RedisKeyUtil.buildBuyListKey(dataCode, price);
        String rawOrder = stringRedisTemplate.opsForList().leftPop(listKey);

        if (rawOrder == null) return null;

        try {
            return objectMapper.readValue(rawOrder, OrderDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패: " + rawOrder, e);
        }
    }

    @Override
    public Map<Long, Long> getAllSellOrderNumbers(String dataCode) {
        return getOrderNumbers("sell:numbers:" + dataCode);
    }

    @Override
    public Map<Long, Long> getAllBuyOrderNumbers(String dataCode) {
        return getOrderNumbers("buy:numbers:" + dataCode);
    }

    private Map<Long, Long> getOrderNumbers(String key) {
        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(key);
        if (raw == null || raw.isEmpty()) return Map.of();

        Map<Long, Long> result = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            try {
                Long price = Long.parseLong(entry.getKey().toString());
                Long quantity = Long.parseLong(entry.getValue().toString());
                result.put(price, quantity);
            } catch (NumberFormatException e) {
                log.warn("Redis 값 파싱 오류: key={}, value={}", entry.getKey(), entry.getValue());
            }
        }
        return result;
    }


    private long calculateTTL(long expiredAtMillis) {
        long now = System.currentTimeMillis();
        return Math.max(0, (expiredAtMillis - now) / 1000);
    }

    private String toJson(OrderDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Redis 직렬화 실패", e);
        }
    }
}
