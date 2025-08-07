package com.ureka.team3.utong_backend.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RedissonConfig {

    @Value("${spring.data.redis.host}") private String host;
    @Value("${spring.data.redis.port}") private int port;
    @Value("${spring.data.redis.password:}") private String password; // 빈 값이면 null 처리
    @Value("${spring.data.redis.ssl:true}") private boolean useSsl;   // ✅ 기본 true

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        String address = (useSsl ? "rediss://" : "redis://") + host + ":" + port;
        log.info("[Redisson] Connecting to {} (SSL: {})", address, useSsl);

        try {
            Config cfg = new Config();
            var single = cfg.useSingleServer()
                    .setAddress(address)
                    .setPassword((password == null || password.isBlank()) ? null : password)
                    .setConnectTimeout(5000)
                    .setTimeout(3000)
                    .setRetryAttempts(2)
                    .setRetryInterval(1000)
                    .setKeepAlive(true)
                    .setPingConnectionInterval(10000)
                    .setDnsMonitoringInterval(5000)
                    .setClientName("utong-backend");

            if (useSsl) {
                // 공인 인증서 사용 환경이면 true 유지가 정석
                single.setSslEnableEndpointIdentification(true);
                // ⚠ 만약 SSLHandshakeException(PKIX) 나면 JDK truststore에 Amazon Root CA 추가 필요
                // (아래 '만약에' 참고)
            }

            RedissonClient client = Redisson.create(cfg);
            client.getNodesGroup().pingAll(); // 즉시 연결 검증
            log.info("[Redisson] Connection established ✅");
            return client;

        } catch (Exception e) {
            log.error("[Redisson] ❌ Failed to connect {}:{} (SSL:{}): {}",
                    host, port, useSsl, e.getMessage(), e);
            throw e;
        }
    }
}
