package com.ureka.team3.utong_backend.datatrade.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.repository.AlertRepository;
import com.ureka.team3.utong_backend.datatrade.dto.alert.ContractAlertDto;
import com.ureka.team3.utong_backend.datatrade.handler.AlertSseHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertSseHandler alertSseHandler;
    private final AlertRepository alertRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void send(String userId, ContractAlertDto alertDto) {
        try {
            String json = objectMapper.writeValueAsString(alertDto);
            boolean sent = alertSseHandler.sendIfConnected(userId, json);
            log.info("[알림 전송] : {}", json);
            if (!sent) {
                alertRepository.save(userId, json);
            }
        } catch (JsonProcessingException e) {
            log.error("알림 직렬화 실패: {}", e.getMessage());
        }
    }

    @Override
    public SseEmitter connect(Account account) {
        String userId = account.getId();

        // 1. SSE 연결
        SseEmitter emitter = alertSseHandler.connect(userId);

        // 2. Redis에서 알림 꺼내기
        List<String> pendingAlerts = alertRepository.findAllAndDelete(userId);

        // 3. 초기 알림 전송
        if (!pendingAlerts.isEmpty()) {
            alertSseHandler.sendInitialAlerts(userId, pendingAlerts);
        }

        return emitter;
    }
}
