package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.service.chart.CurrentPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy.SSE_TIMEOUT;

@Service
@RequiredArgsConstructor
@Slf4j
@Qualifier("chartSseHandler")
public class SseHandlerImpl implements SseHandler {

    private final CurrentPriceService currentPriceService;
    private final ConcurrentHashMap<String, Set<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    // SSE 연결 및 초기 데이터 전송
    @Override
    public SseEmitter connect(String dataCode, String topic) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitterMap.putIfAbsent(dataCode, ConcurrentHashMap.newKeySet());
        emitterMap.get(dataCode).add(emitter);

        log.info("SSE 연결됨 [dataCode: {}] 현재 연결 수: {}", dataCode, emitterMap.get(dataCode).size());

        emitter.onCompletion(() -> removeEmitter(dataCode, emitter));
        emitter.onTimeout(() -> removeEmitter(dataCode, emitter));
        emitter.onError((e) -> removeEmitter(dataCode, emitter));

        sendInitialData(dataCode, emitter, topic);
        return emitter;
    }

    private void sendInitialData(String dataCode, SseEmitter emitter, String topic) {
        try {
            List<AvgPerHour> data = currentPriceService.getInitData(dataCode);
            emitter.send(SseEmitter.event()
                    .name(topic)
                    .data(data));
            log.info("초기 데이터 전송 완료 [dataCode: {}] 개수: {}", dataCode, data.size());
        } catch (Exception e) {
            log.warn("초기 데이터 전송 실패 [dataCode: {}]: {}", dataCode, e.getMessage(), e);
            emitter.completeWithError(e);
        }
    }

    @Override
    public void broadCast(String dataCode, List<AvgPerHour> allData) {
        Set<SseEmitter> emitters = emitterMap.getOrDefault(dataCode, ConcurrentHashMap.newKeySet());
        Set<SseEmitter> deadEmitters = ConcurrentHashMap.newKeySet();
        String eventName = buildEventName(dataCode);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(allData));
            } catch (IOException e) {
                log.warn("SSE 전송 실패 [dataCode: {}] → 제거 예정: {}", dataCode, e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
        log.info("📡 SSE 전송 완료 [dataCode: {}] - 성공: {}, 실패 제거: {}", dataCode, emitters.size(), deadEmitters.size());
    }

    private void removeEmitter(String dataCode, SseEmitter emitter) {
        Set<SseEmitter> emitters = emitterMap.getOrDefault(dataCode, ConcurrentHashMap.newKeySet());
        emitters.remove(emitter);
        log.info(" SSE 연결 해제됨 [dataCode: {}], 현재 연결 수: {}", dataCode, emitters.size());
    }

    private String buildEventName(String dataCode) {
        return dataCode + "-hourly-update";
    }
}
