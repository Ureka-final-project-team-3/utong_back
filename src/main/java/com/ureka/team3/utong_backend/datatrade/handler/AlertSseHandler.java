package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.auth.entity.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy.SSE_TIMEOUT;

@Component
@Slf4j
public class AlertSseHandler {

    private static final String TOPIC = "alert";
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter connect(String userId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));
        return emitter;
    }

    public boolean sendIfConnected(String userId, String alertJson) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event().name(TOPIC).data(alertJson));
                return true;
            } catch (IOException e) {
                emitters.remove(userId);
            }
        }
        return false;
    }

    public void sendInitialAlerts(String userId, List<String> alertJsonList) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter != null) {
            for (String alert : alertJsonList) {
                try {
                    emitter.send(SseEmitter.event().name(TOPIC).data(alert));
                } catch (IOException e) {
                    emitters.remove(userId);
                    break;
                }
            }
        }
    }
}
