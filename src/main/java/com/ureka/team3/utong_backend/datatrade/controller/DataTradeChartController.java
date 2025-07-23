package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.dto.ChartDataDto;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataTradeChartController {

    private final @Qualifier("chartSseHandler")SseHandler<ChartDataDto> sseHandler;

//    @GetMapping(value = "/current-prices/stream/{dataCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamCurrentPrices(@PathVariable("dataCode") String dataCode) {
//        return sseHandler.connect(dataCode);
//    }

    @GetMapping(value = "/current-prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllCurrentPrices() {
        return sseHandler.connect("ALL_DATA");
    }

}
