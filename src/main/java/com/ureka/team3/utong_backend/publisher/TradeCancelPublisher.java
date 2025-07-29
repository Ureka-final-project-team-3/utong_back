package com.ureka.team3.utong_backend.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.TradeCancelMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TradeCancelPublisher {
    private static final String TRADE_CANCELED_CHANNEL = "trade:canceled";
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(TradeCancelMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            stringRedisTemplate.convertAndSend(TRADE_CANCELED_CHANNEL, jsonMessage);
            log.info("거래 취소 완료 메세지 전송");
        } catch (JsonProcessingException e) {
            log.error("거래 취소 완료 메세지 전송 실패");
        }
    }
}
