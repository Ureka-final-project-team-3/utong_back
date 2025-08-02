package com.ureka.team3.utong_backend.datatrade.repository.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.chart.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ContractHourlyAvgPriceRedisRepositoryTest {

    private ContractHourlyAvgPriceRedisRepository repository;
    private StringRedisTemplate redisTemplate;
    private ListOperations<String, String> listOperations;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        listOperations = mock(ListOperations.class);
        objectMapper = mock(ObjectMapper.class);

        when(redisTemplate.opsForList()).thenReturn(listOperations);

        repository = new ContractHourlyAvgPriceRedisRepository(redisTemplate, objectMapper);
    }

    @Test
    void 평균가_데이터가_존재하면_JSON을_객체로_변환하여_리스트로_반환한다() throws Exception {
        // given
        String dataCode = "DATA001";
        String key = RedisKeyUtil.buildCurrentPriceListKey(dataCode);

        String json1 = "{\"dataCode\":\"DATA001\", \"avgPrice\":1000, \"aggregatedAt\":\"2025-08-01T10:00:00\"}";
        String json2 = "{\"dataCode\":\"DATA001\", \"avgPrice\":2000, \"aggregatedAt\":\"2025-08-01T11:00:00\"}";
        List<String> redisData = List.of(json1, json2);

        AvgPerHour obj1 = AvgPerHour.builder()
                .dataCode("DATA001")
                .avgPrice(1000L)
                .aggregatedAt(LocalDateTime.of(2025, 8, 1, 10, 0))
                .build();

        AvgPerHour obj2 = AvgPerHour.builder()
                .dataCode("DATA001")
                .avgPrice(2000L)
                .aggregatedAt(LocalDateTime.of(2025, 8, 1, 11, 0))
                .build();

        when(listOperations.range(key, 0, -1)).thenReturn(redisData);
        when(objectMapper.readValue(json1, AvgPerHour.class)).thenReturn(obj1);
        when(objectMapper.readValue(json2, AvgPerHour.class)).thenReturn(obj2);

        // when
        List<AvgPerHour> result = repository.getAllData(dataCode);

        // then
        assertThat(result).containsExactly(obj1, obj2);
    }

    @Test
    void 평균가_데이터가_없으면_빈리스트를_반환한다() {
        // given
        String dataCode = "EMPTY_CODE";
        String key = RedisKeyUtil.buildCurrentPriceListKey(dataCode);
        when(listOperations.range(key, 0, -1)).thenReturn(null);

        // when
        List<AvgPerHour> result = repository.getAllData(dataCode);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void JSON역직렬화_실패한_데이터는_결과에서_제외된다() throws Exception {
        // given
        String dataCode = "PARTIAL_FAIL";
        String key = RedisKeyUtil.buildCurrentPriceListKey(dataCode);

        String validJson = "{\"dataCode\":\"DATA001\", \"avgPrice\":1000, \"aggregatedAt\":\"2025-08-01T10:00:00\"}";
        String invalidJson = "{invalid json}";
        List<String> redisData = List.of(validJson, invalidJson);

        AvgPerHour validObj = AvgPerHour.builder()
                .dataCode("DATA001")
                .avgPrice(1000L)
                .aggregatedAt(LocalDateTime.of(2025, 8, 1, 10, 0))
                .build();

        when(listOperations.range(key, 0, -1)).thenReturn(redisData);
        when(objectMapper.readValue(validJson, AvgPerHour.class)).thenReturn(validObj);
        when(objectMapper.readValue(invalidJson, AvgPerHour.class)).thenThrow(new RuntimeException("역직렬화 실패"));

        // when
        List<AvgPerHour> result = repository.getAllData(dataCode);

        // then
        assertThat(result).containsExactly(validObj);
    }
}
