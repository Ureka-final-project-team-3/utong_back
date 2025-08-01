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
    private static final String TRADE_EXECUTED_CHANNEL = "trade:executed";
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(TradeExecutedMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            stringRedisTemplate.convertAndSend(TRADE_EXECUTED_CHANNEL, jsonMessage);
            log.info("거래 요청 완료 메세지 전송");
        } catch (JsonProcessingException e) {
            log.error("거래 요청 완료 메세지 전송 실패");
        }
    }
}
