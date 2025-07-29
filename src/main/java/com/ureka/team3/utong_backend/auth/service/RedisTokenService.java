package com.ureka.team3.utong_backend.auth.service;


import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ureka.team3.utong_backend.auth.util.JwtProperties;

@Service
public class RedisTokenService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;
    
    public RedisTokenService(RedisTemplate<String, String> redisTemplate, JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
    }
    
    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "blacklist:access:";
    private static final String REFRESH_TOKEN_BLACKLIST_PREFIX = "blacklist:refresh:";
    
    public void saveRefreshToken(String accountId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        redisTemplate.opsForValue().set(key, refreshToken, jwtProperties.getRefreshTokenExpiration(), TimeUnit.MILLISECONDS);
    }
    
    public String getRefreshToken(String accountId) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        return redisTemplate.opsForValue().get(key);
    }
    
    public void deleteRefreshToken(String accountId) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        redisTemplate.delete(key);
    }
    
    public boolean existsRefreshToken(String accountId) {
        String key = REFRESH_TOKEN_PREFIX + accountId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    public void blacklistAccessToken(String token, long expiration) {
        String key = ACCESS_TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expiration, TimeUnit.MILLISECONDS);
    }
    
    public void blacklistRefreshToken(String token, long expiration) {
        String key = REFRESH_TOKEN_BLACKLIST_PREFIX + token;
        redisTemplate.opsForValue().set(key, "blacklisted", expiration, TimeUnit.MILLISECONDS);
    }
    
    public boolean isTokenBlacklisted(String token) {
        String accessKey = ACCESS_TOKEN_BLACKLIST_PREFIX + token;
        String refreshKey = REFRESH_TOKEN_BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(accessKey)) || 
               Boolean.TRUE.equals(redisTemplate.hasKey(refreshKey));
    }
}