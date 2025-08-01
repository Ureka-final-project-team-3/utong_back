package com.ureka.team3.utong_backend.datatrade.repository.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AlertRepositoryTest {

    private AlertRepository alertRepository;
    private StringRedisTemplate redisTemplate;
    private ListOperations<String, String> listOperations;

    @BeforeEach
    void setUp() {
        redisTemplate = mock(StringRedisTemplate.class);
        listOperations = mock(ListOperations.class);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        alertRepository = new AlertRepository(redisTemplate);
    }

    @Test
    void 알림을_저장하면_redis리스트에_추가된다() {
        // given
        String userId = "user123";
        String alertJson = "{\"message\":\"test alert\"}";

        // when
        alertRepository.save(userId, alertJson);

        // then
        verify(listOperations, times(1)).rightPush("notifications:user123", alertJson);
    }

    @Test
    void 모든알림을_조회하고_삭제하면_알림리스트를_반환하고_key도_삭제된다() {
        // given
        String userId = "user123";
        String key = "notifications:user123";
        List<String> expectedAlerts = List.of("alert1", "alert2");

        when(listOperations.range(key, 0, -1)).thenReturn(expectedAlerts);

        // when
        List<String> actualAlerts = alertRepository.findAllAndDelete(userId);

        // then
        assertThat(actualAlerts).isEqualTo(expectedAlerts);
        verify(redisTemplate).delete(key);
    }

    @Test
    void 알림이_없는경우_빈리스트를_반환하고_key를_삭제한다() {
        // given
        String userId = "user456";
        String key = "notifications:user456";

        when(listOperations.range(key, 0, -1)).thenReturn(null);

        // when
        List<String> actualAlerts = alertRepository.findAllAndDelete(userId);

        // then
        assertThat(actualAlerts).isEmpty();
        verify(redisTemplate).delete(key);
    }
}
