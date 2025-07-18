package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseService {

    SseEmitter connect(String dataCode);

    void broadcast(List<AvgPerHour> allData);
}
