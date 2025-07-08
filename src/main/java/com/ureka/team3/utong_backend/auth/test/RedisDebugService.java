package com.ureka.team3.utong_backend.auth.test;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;

@Service
public class RedisDebugService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public RedisDebugService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public ApiResponse<Map<String, Object>> getAllKeys() {
        Set<String> keys = redisTemplate.keys("*");
        Map<String, Object> result = Map.of(
            "count", keys != null ? keys.size() : 0,
            "keys", keys != null ? keys : Set.of()
        );
        return ApiResponse.success("모든 Redis 키 조회 완료", result);
    }
    
    public ApiResponse<Map<String, Object>> getDetailedKeys() {
        Set<String> allKeys = redisTemplate.keys("*");
        Map<String, Object> detailedInfo = new java.util.HashMap<>();
        
        if (allKeys != null) {
            for (String key : allKeys) {
                String value = redisTemplate.opsForValue().get(key);
                Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                String type = redisTemplate.type(key).code();
                
                detailedInfo.put(key, Map.of(
                    "value", value != null ? value : "null",
                    "ttl", ttl != null ? ttl + "s" : "no expiration",
                    "type", type,
                    "length", key.length()
                ));
            }
        }
        
        Map<String, Object> result = Map.of(
            "totalKeys", allKeys != null ? allKeys.size() : 0,
            "details", detailedInfo
        );
        
        return ApiResponse.success("상세 Redis 키 정보 조회 완료", result);
    }
    
    public ApiResponse<Set<String>> getRefreshTokenKeys() {
        Set<String> keys = redisTemplate.keys("refresh_token:*");
        return ApiResponse.success("Refresh Token 키 조회 완료", keys);
    }
    
    public ApiResponse<Set<String>> getBlacklistKeys() {
        Set<String> keys = redisTemplate.keys("blacklist:*");
        return ApiResponse.success("Blacklist 키 조회 완료", keys);
    }
    
    public ApiResponse<Map<String, Object>> getKeyInfo(String key) {
        String value = redisTemplate.opsForValue().get(key);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        
        Map<String, Object> result = Map.of(
            "key", key,
            "value", value != null ? value : "null",
            "ttl", ttl != null ? ttl + " seconds" : "no expiration"
        );
        
        return ApiResponse.success("키 정보 조회 완료", result);
    }
    
    public ApiResponse<Map<String, Object>> getRefreshToken(String accountId) {
        String key = "refresh_token:" + accountId;
        String value = redisTemplate.opsForValue().get(key);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        
        Map<String, Object> result = Map.of(
            "key", key,
            "value", value != null ? value : "null",
            "ttl", ttl != null ? ttl + " seconds" : "no expiration"
        );
        
        return ApiResponse.success("Refresh Token 정보 조회 완료", result);
    }
    
    public ApiResponse<Map<String, Object>> deleteKey(String key) {
        Boolean deleted = redisTemplate.delete(key);
        Map<String, Object> result = Map.of(
            "key", key,
            "deleted", deleted != null ? deleted : false,
            "message", deleted != null && deleted ? "키가 삭제되었습니다" : "키 삭제 실패"
        );
        
        return ApiResponse.success("키 삭제 작업 완료", result);
    }
}