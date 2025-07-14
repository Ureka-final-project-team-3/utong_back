package com.ureka.team3.utong_backend.chart.controller;

import com.ureka.team3.utong_backend.chart.service.TradeEventPublisher;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trade/test")
@RequiredArgsConstructor
public class TradeTestController {
    
    private final TradeEventPublisher tradeEventPublisher;
    
    @PostMapping("/mock-trade/{dataType}")
    public ResponseEntity<ApiResponse<String>> publishMockTrade(@PathVariable("dataType") String dataType) {
        return ResponseEntity.ok(tradeEventPublisher.publishMockTradeEvent(dataType));
    }
    
    @PostMapping("/custom-trade")
    public ResponseEntity<ApiResponse<String>> publishCustomTrade(
            @RequestParam("dataType") String dataType,
            @RequestParam("price") Long price,
            @RequestParam("quantity") Long quantity) {
        return ResponseEntity.ok(tradeEventPublisher.publishCustomTradeEvent(dataType, price, quantity));
    }
}