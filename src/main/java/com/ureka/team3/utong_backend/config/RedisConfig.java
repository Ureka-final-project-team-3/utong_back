package com.ureka.team3.utong_backend.config;

import com.ureka.team3.utong_backend.subscriber.ChartStatusSubscriber;
import com.ureka.team3.utong_backend.subscriber.OrderQueueStatusSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final ChartStatusSubscriber chartStatusSubscriber;
    private final OrderQueueStatusSubscriber orderQueueStatusSubscriber;
    private static final String CONTRACT_AGGREGATION_STATUS_CHANNEL = "contract:aggregation:status";
    private static final String QUEUE_AGGREGATION_STATUS_CHANNEL = "queue:aggregation:status";
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(
                chartStatusSubscriber,
                new ChannelTopic(CONTRACT_AGGREGATION_STATUS_CHANNEL)
        );

        container.addMessageListener(
                orderQueueStatusSubscriber,
                new ChannelTopic(QUEUE_AGGREGATION_STATUS_CHANNEL)
        );

        return container;
    }
}
