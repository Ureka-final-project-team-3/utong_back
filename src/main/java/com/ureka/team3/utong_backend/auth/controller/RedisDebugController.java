package com.ureka.team3.utong_backend.auth.controller;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/debug/redis")
public class RedisDebugController {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public RedisDebugController(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @GetMapping("/keys")
    public Map<String, Object> getAllKeys() {
        Set<String> keys = redisTemplate.keys("*");
        return Map.of(
            "count", keys != null ? keys.size() : 0,
            "keys", keys != null ? keys : Set.of()
        );
    }
    
    @GetMapping("/keys/detailed")
    public Map<String, Object> getDetailedKeys() {
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
        
        return Map.of(
            "totalKeys", allKeys != null ? allKeys.size() : 0,
            "details", detailedInfo
        );
    }
    
    @GetMapping("/refresh-tokens")
    public Set<String> getRefreshTokenKeys() {
        return redisTemplate.keys("refresh_token:*");
    }
    
    @GetMapping("/blacklist")
    public Set<String> getBlacklistKeys() {
        return redisTemplate.keys("blacklist:*");
    }
    
    @GetMapping("/get/{key}")
    public Map<String, Object> getKeyInfo(@PathVariable String key) {
        String value = redisTemplate.opsForValue().get(key);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        
        return Map.of(
            "key", key,
            "value", value != null ? value : "null",
            "ttl", ttl != null ? ttl + " seconds" : "no expiration"
        );
    }
    
    @GetMapping("/refresh-token/{accountId}")
    public Map<String, Object> getRefreshToken(@PathVariable String accountId) {
        String key = "refresh_token:" + accountId;
        return getKeyInfo(key);
    }
    
    @DeleteMapping("/delete/{key}")
    public Map<String, Object> deleteKey(@PathVariable String key) {
        Boolean deleted = redisTemplate.delete(key);
        return Map.of(
            "key", key,
            "deleted", deleted != null ? deleted : false,
            "message", deleted != null && deleted ? "키가 삭제되었습니다" : "키 삭제 실패"
        );
    }
}