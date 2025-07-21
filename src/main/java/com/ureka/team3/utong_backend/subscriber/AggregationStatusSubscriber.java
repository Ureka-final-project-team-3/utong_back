package com.ureka.team3.utong_backend.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.AggregationStatusMessage;
import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AggregationStatusSubscriber implements MessageListener {   // 평균 시세 그래프 집계 완료 시 호출 (1시간 마다)

    private final SseHandler sseHandler;
    private final ObjectMapper objectMapper;
    private final DataTradePolicy dataTradePolicy;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("집계 완료 메시지 수신: {}", message);

            AggregationStatusMessage aggregationStatusMessage = getAggregationStatusMessage(message);
            if ("SUCCESS".equals(aggregationStatusMessage.getStatus())) {
                requestBroadCast(aggregationStatusMessage);
            } else {
                log.warn("집계 실패 알림 수신: {}", aggregationStatusMessage.getMessage());
            }
        } catch (Exception e) {
            log.error("집계 완료 메시지 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private AggregationStatusMessage getAggregationStatusMessage(Message message) throws JsonProcessingException {
        String payload = new String(message.getBody());
        AggregationStatusMessage aggregationStatusMessage =
                objectMapper.readValue(payload, AggregationStatusMessage.class);
        return aggregationStatusMessage;
    }

    private void requestBroadCast(AggregationStatusMessage aggregationStatusMessage) {
        log.info("집계 완료 처리 성공: {}", aggregationStatusMessage.getAggregatedAt());
        Map<String, List<AvgPerHour>> dataMap = aggregationStatusMessage.getDataMap();
        for (Code code : dataTradePolicy.getDataTypeCodeList()) {
            sseHandler.broadCast(code.getCode(), dataMap.get(code.getCode()));
        }
    }
}
