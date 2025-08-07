package com.ureka.team3.utong_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;

@Configuration
public class LockConfig {

    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory connectionFactory) {
        return new RedisLockRegistry(connectionFactory, "utong:lock", 10_000); // 10초 보유
    }
}
