package com.ureka.team3.utong_backend.datatrade.messaging.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.messaging.message.OrderQueueMessage;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrdersQueueDto;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderQueueStatusSubscriber implements MessageListener {   // 평균 시세 그래프 집계 완료 시 호출 (1시간 마다)

    private final @Qualifier("orderQueueStatusSseHandler")SseHandler<OrdersQueueDto> sseHandler;
    private final ObjectMapper objectMapper;
    private final DataTradePolicy dataTradePolicy;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("집계 완료 메시지 수신: {}", message);

            OrderQueueMessage orderQueueMessage = getOrderQueueStatusMessage(message);
            if ("SUCCESS".equals(orderQueueMessage.getStatus())) {
//                requestBroadCast(orderQueueMessage);
                requestAllBroadCast(orderQueueMessage);
            } else {
                log.warn("집계 실패 알림 수신: {}", orderQueueMessage.getMessage());
            }
        } catch (Exception e) {
            log.error("집계 완료 메시지 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private OrderQueueMessage getOrderQueueStatusMessage(Message message) throws JsonProcessingException {
        String payload = new String(message.getBody());
        return objectMapper.readValue(payload, OrderQueueMessage.class);
    }

    private void requestBroadCast(OrderQueueMessage message) {
        log.info("집계 완료 처리 성공: {}", message.getPublishedAt());
        Map<String, OrdersQueueDto> dataMap = message.getDataMap();
        for (Code code : dataTradePolicy.getDataTypeCodeList()) {
            sseHandler.broadCast(code.getCode(), dataMap.get(code.getCode()));
        }
    }

    private void requestAllBroadCast(OrderQueueMessage message) {
        Map<String, OrdersQueueDto> dataMap = message.getDataMap();

        // 모든 데이터를 통합해서 브로드캐스트만
        List<OrdersQueueDto> allOrdersData = new ArrayList<>();
        for (Code code : dataTradePolicy.getDataTypeCodeList()) {
            OrdersQueueDto queueData = dataMap.get(code.getCode());
            if (queueData != null) {
                if (queueData.getDataCode() == null) {
                    queueData.setDataCode(code.getCode());
                }
                allOrdersData.add(queueData);
            }
        }

        sseHandler.broadCastAllData(allOrdersData);
    }

}
