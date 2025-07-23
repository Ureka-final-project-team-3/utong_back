package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.ChartDataDto;
import com.ureka.team3.utong_backend.datatrade.dto.WeeklyChartDto;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import com.ureka.team3.utong_backend.datatrade.service.chart.weekly.WeeklyPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data")
public class DataTradeChartController {

    private final @Qualifier("chartSseHandler")SseHandler<ChartDataDto> sseHandler;
    private final WeeklyPriceService weeklyPriceService;

//    @GetMapping(value = "/current-prices/stream/{dataCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamCurrentPrices(@PathVariable("dataCode") String dataCode) {
//        return sseHandler.connect(dataCode);
//    }

    @GetMapping(value = "/current-prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllCurrentPrices() {
        return sseHandler.connect("ALL_DATA");
    }

    @GetMapping("/weekly-prices/{dataCode}")
    public ResponseEntity<ApiResponse<WeeklyChartDto>> getWeeklyPrices(@PathVariable("dataCode") String dataCode) {
        return ResponseEntity.ok(weeklyPriceService.getWeeklyPrice(dataCode));
    }

}
