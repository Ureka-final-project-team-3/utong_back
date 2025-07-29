package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.datatrade.dto.ChartDataDto;
import com.ureka.team3.utong_backend.datatrade.service.chart.current.CurrentPriceService;
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
public class ChartSseHandler implements SseHandler<ChartDataDto> {

    private final CurrentPriceService currentPriceService;
    private final ConcurrentHashMap<String, Set<SseEmitter>> emitterMap = new ConcurrentHashMap<>();

    private static final String TOPIC ="chart-initial-data";
    private static final String ALL_DATA_TOPIC = "all-chart-initial-data";
    private static final String ALL_DATA_KEY = "ALL_DATA";

    // SSE 연결 및 초기 데이터 전송
    @Override
    public SseEmitter connect(String dataCode) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitterMap.putIfAbsent(dataCode, ConcurrentHashMap.newKeySet());
        emitterMap.get(dataCode).add(emitter);

        log.info("SSE 연결됨 [dataCode: {}] 현재 연결 수: {}", dataCode, emitterMap.get(dataCode).size());

        emitter.onCompletion(() -> removeEmitter(dataCode, emitter));
        emitter.onTimeout(() -> removeEmitter(dataCode, emitter));
        emitter.onError((e) -> removeEmitter(dataCode, emitter));

        sendInitialData(dataCode, emitter);
        return emitter;
    }

    private void sendInitialData(String dataCode, SseEmitter emitter) {
        try {
            if(ALL_DATA_KEY.equals(dataCode)) {
                List<ChartDataDto> allInitData = currentPriceService.getAllInitData();

                emitter.send(SseEmitter.event()
                        .name(ALL_DATA_TOPIC)
                        .data(allInitData));
            } else {
                ChartDataDto data = currentPriceService.getInitData(dataCode);
                emitter.send(SseEmitter.event()
                        .name(TOPIC)
                        .data(data));
            }

        } catch (Exception e) {
            log.warn("초기 데이터 전송 실패 [dataCode: {}]: {}", dataCode, e.getMessage(), e);
            emitter.completeWithError(e);
        }
    }

    @Override
    public void broadCast(String dataCode, ChartDataDto data) {
        Set<SseEmitter> emitters = emitterMap.getOrDefault(dataCode, ConcurrentHashMap.newKeySet());
        Set<SseEmitter> deadEmitters = ConcurrentHashMap.newKeySet();
        String eventName = buildEventName(dataCode);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.warn("SSE 전송 실패 [dataCode: {}] → 제거 예정: {}", dataCode, e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
        log.info("📡 SSE 전송 완료 [dataCode: {}] - 성공: {}, 실패 제거: {}", dataCode, emitters.size(), deadEmitters.size());
    }

    @Override
    public void broadCastAllData(List<ChartDataDto> allData) {
        Set<SseEmitter> emitters = emitterMap.getOrDefault(ALL_DATA_KEY,  ConcurrentHashMap.newKeySet());
        Set<SseEmitter> deadEmitters = ConcurrentHashMap.newKeySet();
        String eventName = buildEventName(ALL_DATA_KEY);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(allData));
            } catch (IOException e) {
                log.warn("전체 큐 SSE 전송 실패 → 제거 예정: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }

        emitters.removeAll(deadEmitters);
    }

    private void removeEmitter(String dataCode, SseEmitter emitter) {
        Set<SseEmitter> emitters = emitterMap.getOrDefault(dataCode, ConcurrentHashMap.newKeySet());
        emitters.remove(emitter);
        log.info(" SSE 연결 해제됨 [dataCode: {}], 현재 연결 수: {}", dataCode, emitters.size());
    }

    private String buildEventName(String dataCode) {
        if(ALL_DATA_KEY.equals(dataCode)) {
            return "all-chart-hourly-update";
        }

        return dataCode + "-chart-hourly-update";
    }
}
