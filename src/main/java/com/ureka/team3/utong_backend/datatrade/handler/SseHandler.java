package com.ureka.team3.utong_backend.datatrade.handler;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SseHandler<T> {

    SseEmitter connect(String dataCode);

    void broadCast(String dataCode,T Data);

    void broadCastAllData(List<T> allData);
}
