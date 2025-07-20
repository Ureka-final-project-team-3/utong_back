package com.ureka.team3.utong_backend.datatrade.service.chart;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseServiceImpl implements SseService {

    private final Set<SseEmitter> clients = ConcurrentHashMap.newKeySet();
    private final ContractHourlyAvgPriceRedisRepository redisRepository;
    private static final List<String> DATA_CODES = List.of("001", "002");

    // SSE 연결 및 초기 데이터 전송
    @Override
    public SseEmitter connectForCurrentPrice(String dataCode) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        clients.add(emitter);
        log.info("SSE 연결됨. 현재 연결 수: {}", clients.size());
        emitter.onCompletion(() -> removeClient(emitter));
        emitter.onTimeout(() -> removeClient(emitter));
        emitter.onError(e -> removeClient(emitter));
        // :흰색_확인_표시: 초기 데이터 전송
        try {
            List<AvgPerHour> data = redisRepository.getAllData(dataCode);
            emitter.send(SseEmitter.event()
                    .name("initial-data")
                    .data(data));

            log.info("dataCode {} 초기 데이터 전송 완료 - 개수: {}", dataCode, data.size());
        } catch (Exception e) {
            log.warn("초기 데이터 전송 실패 - dataCode: {}", dataCode, e);
            emitter.completeWithError(e);
        }
        return emitter;
    }

    @Override
    public void broadcastForCurrentPrice(List<AvgPerHour> allData) {
        Set<SseEmitter> deadEmitters = ConcurrentHashMap.newKeySet();
        for (SseEmitter emitter : clients) {
            try {
                emitter.send(SseEmitter.event()
                        .name("hourly-update")
                        .data(allData));
            } catch (IOException e) {
                log.warn("SSE 전송 실패 → 제거 대상: {}", e.getMessage());
                deadEmitters.add(emitter);
            }
        }
        clients.removeAll(deadEmitters);
        log.info("SSE 전송 완료 - 성공: {}, 실패 제거: {}", clients.size(), deadEmitters.size());
    }

    private void removeClient(SseEmitter emitter) {
        clients.remove(emitter);
        log.info("SSE 연결 해제됨. 현재 연결 수: {}", clients.size());
    }
}
