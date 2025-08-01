package com.ureka.team3.utong_backend.datatrade.repository.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.chart.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ContractHourlyAvgPriceRedisRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public List<AvgPerHour> getAllData(String dataCode) {
        try {
            String key = RedisKeyUtil.buildCurrentPriceListKey(dataCode);
            List<String> jsonList = stringRedisTemplate.opsForList().range(key, 0, -1);

            if (jsonList == null || jsonList.isEmpty()) {
                return new ArrayList<>();
            }

            return jsonList.stream()
                    .map(this::convertFromJson)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Redis 평균가 데이터 조회 실패 - dataCode: {}, error: {}", dataCode, e.getMessage());
            return new ArrayList<>();
        }
    }

    private AvgPerHour convertFromJson(String json) {
        try {
            return objectMapper.readValue(json, AvgPerHour.class);
        } catch (Exception e) {
            log.error("JSON 역직렬화 실패: {}", json, e);
            return null;
        }
    }
}
