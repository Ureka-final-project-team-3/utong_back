package com.ureka.team3.utong_backend.datatrade.repository.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.trade.ContractDto;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ContractQueueRepositoryImplTest {

    private ContractQueueRepositoryImpl repository;
    private StringRedisTemplate redisTemplate;
    private ListOperations<String, String> listOperations;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        listOperations = mock(ListOperations.class);
        objectMapper = mock(ObjectMapper.class);

        when(redisTemplate.opsForList()).thenReturn(listOperations);

        repository = new ContractQueueRepositoryImpl(redisTemplate, objectMapper);
    }

    @Test
    void 계약리스트가_존재하면_JSON을_객체로_변환하여_모두_반환한다() throws Exception {
        // given
        String dataCode = "TEST001";
        String key = RedisKeyUtil.buildContractListKey(dataCode);

        String json1 = "{\"price\":1000}";
        String json2 = "{\"price\":2000}";

        ContractDto dto1 = new ContractDto("buy1", "sell1", "acc1", "acc2", 1000L, 10L, dataCode, LocalDateTime.now());
        ContractDto dto2 = new ContractDto("buy2", "sell2", "acc3", "acc4", 2000L, 5L, dataCode, LocalDateTime.now());

        when(listOperations.range(key, 0, -1)).thenReturn(List.of(json1, json2));
        when(objectMapper.readValue(json1, ContractDto.class)).thenReturn(dto1);
        when(objectMapper.readValue(json2, ContractDto.class)).thenReturn(dto2);

        // when
        List<ContractDto> result = repository.getAllCachedContracts(dataCode);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPrice()).isEqualTo(1000L);
        assertThat(result.get(1).getPrice()).isEqualTo(2000L);
    }

    @Test
    void 계약리스트가_없으면_빈리스트를_반환한다() {
        // given
        String dataCode = "EMPTY001";
        String key = RedisKeyUtil.buildContractListKey(dataCode);

        when(listOperations.range(key, 0, -1)).thenReturn(null);

        // when
        List<ContractDto> result = repository.getAllCachedContracts(dataCode);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 최근계약이_존재하면_첫번째_데이터를_객체로_변환하여_반환한다() throws Exception {
        // given
        String dataCode = "TEST001";
        String key = RedisKeyUtil.buildContractListKey(dataCode);

        String json = "{\"price\":999}";
        ContractDto dto = new ContractDto("buyX", "sellX", "accX", "accY", 999L, 2L, dataCode, LocalDateTime.now());

        when(listOperations.range(key, 0, 0)).thenReturn(List.of(json));
        when(objectMapper.readValue(json, ContractDto.class)).thenReturn(dto);

        // when
        ContractDto result = repository.getRecentContract(dataCode);

        // then
        assertThat(result.getPrice()).isEqualTo(999L);
        assertThat(result.getPurchaseOrderId()).isEqualTo("buyX");
    }

    @Test
    void 최근계약이_없으면_null을_반환한다() {
        // given
        String dataCode = "EMPTY002";
        String key = RedisKeyUtil.buildContractListKey(dataCode);

        when(listOperations.range(key, 0, 0)).thenReturn(null);

        // when
        ContractDto result = repository.getRecentContract(dataCode);

        // then
        assertThat(result).isNull();
    }

    @Test
    void JSON역직렬화가_실패하면_해당데이터는_무시된다() throws Exception {
        // given
        String dataCode = "PARTIAL_FAIL";
        String key = RedisKeyUtil.buildContractListKey(dataCode);

        String invalidJson = "{invalid json}";
        String validJson = "{\"price\":5000}";

        ContractDto validDto = new ContractDto("buy5", "sell5", "acc5", "acc6", 5000L, 3L, dataCode, LocalDateTime.now());

        when(listOperations.range(key, 0, -1)).thenReturn(List.of(invalidJson, validJson));
        when(objectMapper.readValue(validJson, ContractDto.class)).thenReturn(validDto);
        when(objectMapper.readValue(invalidJson, ContractDto.class))
                .thenThrow(new JsonProcessingException("역직렬화 실패") {});

        // when
        List<ContractDto> result = repository.getAllCachedContracts(dataCode);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isEqualTo(5000L);
    }

}
