package com.ureka.team3.utong_backend.datatrade.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
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
    public OrderDto popValidSellOrder(String dataCode, long priceLimit) {
        String zsetKey = buildSellZSetKey(dataCode); // 예: sell:zset:002

        // 가격 오름차순: 0 ~ priceLimit 범위 조회
        Set<String> queueKeys = stringRedisTemplate.opsForZSet()
                .rangeByScore(zsetKey, 0, priceLimit);

        if (queueKeys == null || queueKeys.isEmpty()) return null;

        for (String queueKey : queueKeys) {
            // 예: "order_queue:sell:002:5200" → 여기서 price 추출
            String[] parts = queueKey.split(":");
            if (parts.length < 4) continue;

            Long price = Long.parseLong(parts[3]); // 5200

            // 주문 꺼내기
            String json = stringRedisTemplate.opsForList().leftPop(queueKey);

            if (json != null) {
                try {
                    OrderDto order = objectMapper.readValue(json, OrderDto.class);
                    order.setPrice(price); // 필드가 있다면 설정

                    // 리스트가 비면 ZSet에서 해당 key 제거
                    Long size = stringRedisTemplate.opsForList().size(queueKey);
                    if (size == null || size == 0) {
                        stringRedisTemplate.opsForZSet().remove(zsetKey, queueKey);
                    }

                    return order;
                } catch (JsonProcessingException e) {
                    log.error("Redis 주문 역직렬화 실패 - key: {}, json: {}", queueKey, json, e);
                }
            } else {
                // 리스트 비었을 경우 ZSet에서 정리
                stringRedisTemplate.opsForZSet().remove(zsetKey, queueKey);
            }
        }

        return null;
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
    public OrderDto popValidBuyOrder(String dataCode, Long priceLimit) {
        String zsetKey = buildBuyZSetKey(dataCode); // e.g. "order_book:buy:002"

        // 내림차순으로 priceLimit 이상만 가져오기 (score: max → priceLimit)
        Set<String> queueKeys = stringRedisTemplate.opsForZSet()
                .reverseRangeByScore(zsetKey, priceLimit, Double.MAX_VALUE);

        if (queueKeys == null || queueKeys.isEmpty()) return null;

        for (String queueKey : queueKeys) {
            // "order_queue:buy:{dataCode}:{price}" → 가격 추출
            String[] parts = queueKey.split(":");
            if (parts.length < 4) continue;

            Long price = Long.parseLong(parts[3]);

            String json = stringRedisTemplate.opsForList().leftPop(queueKey);
            if (json != null) {
                try {
                    OrderDto order = objectMapper.readValue(json, OrderDto.class);
                    order.setPrice(price); // 필드가 있다면 설정

                    // 리스트가 비면 ZSet에서도 제거
                    Long size = stringRedisTemplate.opsForList().size(queueKey);
                    if (size == null || size == 0) {
                        stringRedisTemplate.opsForZSet().remove(zsetKey, queueKey);
                    }

                    return order;
                } catch (JsonProcessingException e) {
                    log.error("구매 주문 역직렬화 실패 - key: {}, json: {}", queueKey, json, e);
                }
            } else {
                // 리스트 비었으면 ZSet에서도 정리
                stringRedisTemplate.opsForZSet().remove(zsetKey, queueKey);
            }
        }

        return null;
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

    @Override
    public void removeFromBuyQueue(BuyDataRequest buyOrderById) {
        String listKey = buildBuyListKey(buyOrderById.getDataCode(), buyOrderById.getPrice());
        // 전체 리스트 가져오기
        List<String> queue = stringRedisTemplate.opsForList().range(listKey, 0, -1);
        if (queue == null) return;

        for (String item : queue) {
            try {
                // JSON 파싱
                JsonNode jsonNode = new ObjectMapper().readTree(item);
                String itemOrderId = jsonNode.get("orderId").asText();

                // 일치하는 항목 찾으면 제거
                if (buyOrderById.getId().equals(itemOrderId)) {
                    stringRedisTemplate.opsForList().remove(listKey, 1, item);
                    break;
                }

            } catch (JsonProcessingException e) {
                // JSON 파싱 오류 시 로그 남기기
                log.warn("Invalid JSON in Redis queue: {}", item);
            }
        }
    }

    @Override
    public void removeFromSaleQueue(SaleDataRequest saleOrderById) {
        String listKey = buildSellListKey(saleOrderById.getDataCode(), saleOrderById.getPrice());
        // 전체 리스트 가져오기
        List<String> queue = stringRedisTemplate.opsForList().range(listKey, 0, -1);
        if (queue == null) return;

        for (String item : queue) {
            try {
                // JSON 파싱
                JsonNode jsonNode = new ObjectMapper().readTree(item);
                String itemOrderId = jsonNode.get("orderId").asText();

                // 일치하는 항목 찾으면 제거
                if (saleOrderById.getId().equals(itemOrderId)) {
                    stringRedisTemplate.opsForList().remove(listKey, 1, item);
                    break;
                }

            } catch (JsonProcessingException e) {
                // JSON 파싱 오류 시 로그 남기기
                log.warn("Invalid JSON in Redis queue: {}", item);
            }
        }
    }

    @Override
    public Map<Long, List<OrderDto>> findAllSellOrders(String dataCode) {
        return findAllOrdersByPattern(RedisKeyUtil.buildCommonSellListKey(dataCode));
    }

    @Override
    public Map<Long, List<OrderDto>> findAllBuyOrders(String dataCode) {
        return findAllOrdersByPattern(RedisKeyUtil.buildCommonBuyListKey(dataCode));
    }

    private Map<Long, List<OrderDto>> findAllOrdersByPattern(String keyPattern) {
        Set<String> keys = stringRedisTemplate.keys(keyPattern);
        if (keys == null || keys.isEmpty()) return Map.of();

        Map<Long, List<OrderDto>> result = new HashMap<>();

        for (String listKey : keys) {
            List<String> rawOrders = stringRedisTemplate.opsForList().range(listKey, 0, -1);
            if (rawOrders == null || rawOrders.isEmpty()) continue;

            Optional<Long> priceOpt = extractPriceFromKey(listKey);
            if (priceOpt.isEmpty()) continue;

            List<OrderDto> orders = parseOrderList(rawOrders);
            result.put(priceOpt.get(), orders);
        }

        return result;
    }

    private Optional<Long> extractPriceFromKey(String listKey) {
        String[] parts = listKey.split(":");
        if (parts.length != 4) return Optional.empty();

        try {
            return Optional.of(Long.parseLong(parts[3]));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private List<OrderDto> parseOrderList(List<String> rawOrders) {
        List<OrderDto> orders = new ArrayList<>();
        for (String json : rawOrders) {
            try {
                orders.add(objectMapper.readValue(json, OrderDto.class));
            } catch (JsonProcessingException e) {
                throw new RuntimeException("JSON 파싱 실패: " + json, e);
            }
        }
        return orders;
    }


    private Map<Long, Long> getOrderNumbers(String key) {
        Map<Object, Object> raw = stringRedisTemplate.opsForHash().entries(key);
        if (raw == null || raw.isEmpty()) return Map.of();

        Map<Long, Long> result = new TreeMap<>();
        for (Map.Entry<Object, Object> entry : raw.entrySet()) {
            try {
                Long price = Long.parseLong(entry.getKey().toString());
                Long quantity = Long.parseLong(entry.getValue().toString());
                if(quantity>0L){
                    result.put(price, quantity);
                }
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
