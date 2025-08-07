package com.ureka.team3.utong_backend.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.ssl:false}")
    private boolean useSsl;

    @Bean
    public RedissonClient redissonClient() {
        String protocol = useSsl ? "rediss://" : "redis://";
        String url = protocol + redisHost + ":" + redisPort;

        Config config = new Config();
        config.useSingleServer()
                .setAddress(url)
                .setPassword(redisPassword.isEmpty() ? null : redisPassword)
                .setConnectTimeout(10000)   // 연결 타임아웃
                .setTimeout(3000)           // 응답 타임아웃
                .setRetryAttempts(3)        // 재시도 횟수
                .setRetryInterval(1500);    // 재시도 간격

        if (useSsl) {
            config.useSingleServer().setSslEnableEndpointIdentification(false);
        }

        // 연결 시도 로그
        System.out.println("[Redisson] Trying to connect → " + url + " (SSL: " + useSsl + ")");

        return Redisson.create(config);
    }
}
