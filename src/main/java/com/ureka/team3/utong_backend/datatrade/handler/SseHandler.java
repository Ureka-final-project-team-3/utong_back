package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseHandler {

    SseEmitter connect(String dataCode, String topic);

    void broadCast(String dataCode,List<AvgPerHour> allData);
}
