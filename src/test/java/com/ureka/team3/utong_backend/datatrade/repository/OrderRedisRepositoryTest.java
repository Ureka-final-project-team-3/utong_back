package com.ureka.team3.utong_backend.datatrade.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.OrderRedisDto;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil.buildSellListKey;
import static com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil.buildSellZSetKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderRedisRepositoryTest {

    @Autowired
    private OrderRedisRepository orderRedisRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private String dataCode = "001";
    private long price = 1000L;

    @BeforeEach
    void clearRedis() {
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    void savePurchaseOrder_정상동작_및_Redis저장확인() throws Exception {
        // given
        OrderRedisDto dto = OrderRedisDto.builder()
                .orderId("100L")
                .price(8900L)
                .quantity(5L)
                .dataCode(dataCode)
                .createdAt(System.currentTimeMillis())
                .expiredAt(System.currentTimeMillis() + 3600_000) // +1시간
                .build();

        // when
        orderRedisRepository.savePurchaseOrder(dto);

        // then
        String listKey = RedisKeyUtil.buildBuyListKey(dataCode, dto.getPrice());
        String zsetKey = RedisKeyUtil.buildBuyZSetKey(dataCode);

        String redisJson = redisTemplate.opsForList().leftPop(listKey);
        assertNotNull(redisJson);

        OrderRedisDto redisDto = objectMapper.readValue(redisJson, OrderRedisDto.class);
        assertEquals(dto.getOrderId(), redisDto.getOrderId());
        assertEquals(dto.getQuantity(), redisDto.getQuantity());

        Set<String> zset = redisTemplate.opsForZSet().range(zsetKey, 0, -1);
        assertTrue(zset.contains(listKey));
    }

    @Test
    void 판매요청_Redis_저장_성공() {
        // given
        long now = Instant.now().toEpochMilli();
        long expiredAt = Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli();

        OrderRedisDto dto = OrderRedisDto.builder()
                .orderId("1")
                .dataCode(dataCode)
                .price(price)
                .quantity(500L)
                .createdAt(now)
                .expiredAt(expiredAt)
                .build();

        // when
        orderRedisRepository.saveSellOrder(dto);

        // then
        String listKey = buildSellListKey(dataCode,price);
        String zsetKey = buildSellZSetKey(dataCode);

        List<String> listValues = redisTemplate.opsForList().range(listKey, 0, -1);
        Double score = redisTemplate.opsForZSet().score(zsetKey, listKey);

        assertThat(listValues).isNotNull();
        assertThat(listValues).hasSize(1);
        assertThat(score).isEqualTo((double) price);

        OrderRedisDto storedDto = null;
        try {
            storedDto = objectMapper.readValue(listValues.get(0), OrderRedisDto.class);
        } catch (Exception e) {
            throw new RuntimeException("역직렬화 실패", e);
        }

        assertThat(storedDto.getOrderId()).isEqualTo(dto.getOrderId());
        assertThat(storedDto.getDataCode()).isEqualTo(dto.getDataCode());
        assertThat(storedDto.getPrice()).isEqualTo(dto.getPrice());
    }
}
