package com.ureka.team3.utong_backend.config;

import com.ureka.team3.utong_backend.datatrade.subscriber.AlertSubscriber;
import com.ureka.team3.utong_backend.datatrade.subscriber.ChartStatusSubscriber;
import com.ureka.team3.utong_backend.datatrade.subscriber.OrderQueueStatusSubscriber;
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
    private final AlertSubscriber alertSubscriber;

    private static final String CONTRACT_AGGREGATION_STATUS_CHANNEL = "contract:aggregation:status";
    private static final String QUEUE_AGGREGATION_STATUS_CHANNEL = "queue:aggregation:status";
    private static final String CONTRACT_ALERT_CHANNEL = "contract:alert";


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

        container.addMessageListener(
            alertSubscriber,
                new ChannelTopic(CONTRACT_ALERT_CHANNEL)
        );

        return container;
    }
}
