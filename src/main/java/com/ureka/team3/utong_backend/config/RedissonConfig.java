package com.ureka.team3.utong_backend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig { // 레디슨 설정

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        String redisUrl = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer()
              .setAddress(redisUrl)
              .setPassword(redisPassword.isBlank() ? null : redisPassword);

        return Redisson.create(config);
    }
}
