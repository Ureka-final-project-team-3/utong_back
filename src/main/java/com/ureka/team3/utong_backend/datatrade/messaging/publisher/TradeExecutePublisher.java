package com.ureka.team3.utong_backend.datatrade.messaging.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.messaging.message.TradeExecutedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeExecutePublisher{
    private final ObjectMapper objectMapper;
    private final TradeExecutedRabbitPublisher tradeExecutedRabbitPublisher;

    public void publish(TradeExecutedMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
//            stringRedisTemplate.convertAndSend(TRADE_EXECUTED_CHANNEL, jsonMessage); // Redis Pub/Sub
            tradeExecutedRabbitPublisher.publishTradeExecuted(message); // RabbitMQ
            tradeExecutedRabbitPublisher.publishEmailNotification(message); // RabbitMQ
            log.info("거래 요청 완료 메세지 전송");
        } catch (JsonProcessingException e) {
            log.error("거래 요청 완료 메세지 전송 실패");
        }
    }
}
