package com.ureka.team3.utong_backend.datatrade.repository.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.TestPropertySource;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataRedisTest
@TestPropertySource(properties = {
        "spring.data.redis.host=43.201.171.29",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=MyR3d1sP@ssw0rd2024!StrongAndLong"
})
@Import({OrderRepositoryImpl.class, OrderRepositoryImplTest.TestConfig.class})
class OrderRepositoryImplTest {

    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @org.springframework.beans.factory.annotation.Autowired
    private OrderRepositoryImpl orderRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private StringRedisTemplate redisTemplate;

    private static final String DATA_CODE = "TEST123";

    @BeforeEach
    void setup() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    private OrderDto 주문생성(long price, long quantity) {
        return OrderDto.builder()
                .orderId(UUID.randomUUID().toString())
                .dataCode(DATA_CODE)
                .price(price)
                .quantity(quantity)
                .build();
    }

    @Test
    @DisplayName("판매 주문 저장 후 동일 가격으로 조회된다")
    void saveSellOrder_then_findSellOrdersByPrice() {
        orderRepository.saveSellOrder(주문생성(5000L, 1L));
        List<OrderDto> result = orderRepository.findSellOrdersByPrice(DATA_CODE, 5000L);
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("구매 주문 저장 후 popValidBuyOrder 가능")
    void savePurchaseOrder_then_popValidBuyOrder() {
        orderRepository.savePurchaseOrder(주문생성(6000L, 1L));
        OrderDto result = orderRepository.popValidBuyOrder(DATA_CODE, 5000L);
        assertThat(result).isNotNull();
        result = orderRepository.popValidBuyOrder(DATA_CODE, 7000L);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("유효한 판매 주문 pop 이후 리스트 비면 ZSet도 제거된다")
    void popValidSellOrder_정상처리() {
        orderRepository.saveSellOrder(주문생성(5100L, 1L));
        OrderDto result = orderRepository.popValidSellOrder(DATA_CODE, 5200L);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("판매 주문 잔여량 재등록 테스트")
    void requeuePartialSellOrder_정상처리() {
        OrderDto dto = 주문생성(5200L, 3L);
        orderRepository.requeuePartialSellOrder(dto);
        List<OrderDto> list = orderRepository.findSellOrdersByPrice(DATA_CODE, 5200L);
        assertThat(list).hasSize(1);
    }

    @Test
    @DisplayName("구매 주문 잔여량 재등록 테스트")
    void requeuePartialBuyOrder_정상처리() {
        OrderDto dto = 주문생성(7000L, 2L);
        orderRepository.requeuePartialBuyOrder(dto);
        OrderDto result = orderRepository.popValidBuyOrder(DATA_CODE, 7000L);
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("최저 판매 가격 조회")
    void getLowestSellPrice_정상처리() {
        orderRepository.saveSellOrder(주문생성(5300L, 1L));
        orderRepository.saveSellOrder(주문생성(5400L, 1L));
        assertThat(orderRepository.getLowestSellPrice(DATA_CODE)).isEqualTo(5300L);
    }

    @Test
    @DisplayName("최고 구매 가격 조회")
    void getHighestBuyPrice_정상처리() {
        orderRepository.savePurchaseOrder(주문생성(5600L, 1L));
        orderRepository.savePurchaseOrder(주문생성(5700L, 1L));
        assertThat(orderRepository.getHighestBuyPrice(DATA_CODE)).isEqualTo(5700L);
    }

    @Test
    @DisplayName("판매/구매 주문 수량 조회 (0 초과만)")
    void getAllSellBuyOrderNumbers() {
        redisTemplate.opsForHash().put("sell:numbers:" + DATA_CODE, "5000", "2");
        redisTemplate.opsForHash().put("sell:numbers:" + DATA_CODE, "5100", "0");
        redisTemplate.opsForHash().put("buy:numbers:" + DATA_CODE, "6000", "1");
        Map<Long, Long> sell = orderRepository.getAllSellOrderNumbers(DATA_CODE);
        Map<Long, Long> buy = orderRepository.getAllBuyOrderNumbers(DATA_CODE);
        assertThat(sell).containsOnlyKeys(5000L);
        assertThat(buy).containsOnlyKeys(6000L);
    }

    @Test
    @DisplayName("리스트에서 판매/구매 주문 제거")
    void removeFromQueue_정상제거() {
        OrderDto dto = 주문생성(5800L, 1L);
        orderRepository.saveSellOrder(dto);
        orderRepository.savePurchaseOrder(dto);
        orderRepository.removeFromBuyQueue(BuyDataRequest.builder()
                .id(dto.getOrderId())
                .dataCode(dto.getDataCode())
                .price(dto.getPrice())
                .build());
        orderRepository.removeFromSaleQueue(SaleDataRequest.builder()
                .id(dto.getOrderId())
                .dataCode(dto.getDataCode())
                .price(dto.getPrice())
                .build());
        assertThat(orderRepository.findSellOrdersByPrice(DATA_CODE, 5800L)).isEmpty();
    }

    @Test
    @DisplayName("판매/구매 주문 리스트에서 하나 꺼내기")
    void popFirstOrderFromList_정상처리() {
        OrderDto dto = 주문생성(5900L, 1L);
        orderRepository.saveSellOrder(dto);
        orderRepository.savePurchaseOrder(dto);
        assertThat(orderRepository.popFirstSellOrderFromList(DATA_CODE, 5900L)).isNotNull();
        assertThat(orderRepository.popFirstBuyOrderFromList(DATA_CODE, 5900L)).isNotNull();
    }

    @Test
    @DisplayName("전체 판매/구매 주문 조회")
    void findAllSellBuyOrders_정상조회() {
        orderRepository.saveSellOrder(주문생성(6100L, 1L));
        orderRepository.savePurchaseOrder(주문생성(6200L, 1L));
        Map<Long, List<OrderDto>> sellMap = orderRepository.findAllSellOrders(DATA_CODE);
        Map<Long, List<OrderDto>> buyMap = orderRepository.findAllBuyOrders(DATA_CODE);
        assertThat(sellMap).containsKey(6100L);
        assertThat(buyMap).containsKey(6200L);
    }

    @Test
    @DisplayName("빈 리스트 또는 키에 대한 예외 케이스 커버")
    void 빈리스트_혹은_잘못된형식_예외처리() {
        redisTemplate.opsForList().rightPush("order_queue:sell:" + DATA_CODE + ":x", "not-a-json");
        redisTemplate.opsForZSet().add("sell:zset:" + DATA_CODE, "order_queue:sell:" + DATA_CODE + ":x", 9999);
        OrderDto result = orderRepository.popValidSellOrder(DATA_CODE, 10000L);
        assertThat(result).isNull();
    }
}
