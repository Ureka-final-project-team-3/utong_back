package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.dto.OrdersQueueDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseHandler<T> {

    SseEmitter connect(String dataCode);

    void broadCast(String dataCode,T Data);

    void broadCastAllData(List<T> allData);
}
