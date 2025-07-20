package com.ureka.team3.utong_backend.datatrade.service.chart;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseService {

    SseEmitter connectForCurrentPrice(String dataCode);

    void broadcastForCurrentPrice(List<AvgPerHour> allData);
}
