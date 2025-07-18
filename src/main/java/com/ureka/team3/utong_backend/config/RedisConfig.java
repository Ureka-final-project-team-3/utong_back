package com.ureka.team3.utong_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ureka.team3.utong_backend.subscriber.AggregationStatusSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final AggregationStatusSubscriber aggregationStatusSubscriber;
    private final String CONTRACT_AGGREGATION_STATUS_CHANNEL = "contract:aggregation:status";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(
                aggregationStatusSubscriber,
                new ChannelTopic(CONTRACT_AGGREGATION_STATUS_CHANNEL)
        );

        return container;
    }
}
