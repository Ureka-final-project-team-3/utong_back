package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.OrdersQueueDto;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import com.ureka.team3.utong_backend.datatrade.service.queue.OrderQueueStatusService;
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
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataTradeQueueStatusController {
    private final @Qualifier("orderQueueStatusSseHandler") SseHandler<OrdersQueueDto> sseHandler;
    private final OrderQueueStatusService orderQueueStatusService;

    @GetMapping(value = "/order-queue/stream/{dataCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentPrices(@PathVariable("dataCode") String dataCode) {
        return sseHandler.connect(dataCode);
    }

    @GetMapping("/order-queue")
    public ResponseEntity<ApiResponse> getAllOrderQueue() {
        return ResponseEntity.ok(orderQueueStatusService.getCurrentAllQueue());
    }
}
