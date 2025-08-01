package com.ureka.team3.utong_backend.datatrade.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AlertRepository {

    private final StringRedisTemplate redisTemplate;

    public void save(String userId, String alertJson) {
        redisTemplate.opsForList().rightPush(buildKey(userId), alertJson);
    }

    public List<String> findAllAndDelete(String userId) {
        String key = buildKey(userId);
        List<String> alerts = redisTemplate.opsForList().range(key, 0, -1);
        redisTemplate.delete(key);
        return alerts != null ? alerts : List.of();
    }

    private String buildKey(String userId) {
        return "notifications:" + userId;
    }
}
