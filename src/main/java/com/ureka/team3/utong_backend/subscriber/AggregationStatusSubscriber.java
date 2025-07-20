package com.ureka.team3.utong_backend.subscriber;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.AggregationStatusMessage;
import com.ureka.team3.utong_backend.datatrade.service.chart.CurrentPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationStatusSubscriber implements MessageListener {   // 평균 시세 그래프 집계 완료 시 호출 (1시간 마다)

    private final CurrentPriceService currentPriceService;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("집계 완료 메시지 수신: {}", message);

            String payload = new String(message.getBody());
            AggregationStatusMessage aggregationStatusMessage =
                    objectMapper.readValue(payload, AggregationStatusMessage.class);

            if ("SUCCESS".equals(aggregationStatusMessage.getStatus())) {
                log.info("집계 완료 처리 성공: {}", aggregationStatusMessage.getAggregatedAt());

                currentPriceService.updateRedisCache(aggregationStatusMessage.getAggregatedAt());
                currentPriceService.broadCastToSseClients();
            } else {
                log.warn("집계 실패 알림 수신: {}", aggregationStatusMessage.getMessage());
            }

        } catch (Exception e) {
            log.error("집계 완료 메시지 처리 중 오류: {}", e.getMessage(), e);
        }
    }
}
