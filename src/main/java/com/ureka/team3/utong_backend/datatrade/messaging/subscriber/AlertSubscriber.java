package com.ureka.team3.utong_backend.datatrade.messaging.subscriber;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.service.alert.AlertServiceImpl;
import com.ureka.team3.utong_backend.datatrade.dto.alert.ContractAlertDto;
import com.ureka.team3.utong_backend.datatrade.messaging.message.AlertMessage;
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
public class AlertSubscriber implements MessageListener {   // 평균 시세 그래프 집계 완료 시 호출 (1시간 마다)

    private final AlertServiceImpl alertServiceImpl;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("알림 전송 메시지 수신: {}", message);

            AlertMessage alertMessage = getAlertMessage(message);
            Map<String, List<ContractAlertDto>> dataMap = alertMessage.getDataMap();
            for (Map.Entry<String, List<ContractAlertDto>> entry : dataMap.entrySet()) {
                String userId = entry.getKey();
                for (ContractAlertDto contractAlertDto : entry.getValue()) {
                    alertServiceImpl.send(userId, contractAlertDto);
                }
            }


        } catch (Exception e) {
            log.error("알림 전송 메시지 처리 중 오류: {}", e.getMessage(), e);
        }
    }

    private AlertMessage getAlertMessage(Message message) throws JsonProcessingException {
        String payload = new String(message.getBody());
        return convertFromJson(payload);
    }

    private AlertMessage convertFromJson(String payload) throws JsonProcessingException {
        return objectMapper.readValue(payload, AlertMessage.class);
    }
}
