package com.ureka.team3.utong_backend.chart.controller;

import com.ureka.team3.utong_backend.chart.service.RealTimePriceService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@RestController
@RequestMapping("/api/trade")
@RequiredArgsConstructor
public class TradePriceController {
    
    private final RealTimePriceService realTimePriceService;
    
    @GetMapping(value = "/price-stream/{dataType}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToPriceUpdates(@PathVariable("dataType") String dataType) {
        return realTimePriceService.subscribeToPriceUpdates(dataType);
    }
    
    @GetMapping("/connections")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getConnectionCounts() {
        return ResponseEntity.ok(ApiResponse.success("현재 연결 상태", realTimePriceService.getConnectionCounts()));
    }
}