package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.OrdersQueueDto;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import com.ureka.team3.utong_backend.datatrade.service.queue.OrderQueueStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "데이터 주문 대기열 API", description = "데이터 거래의 주문 대기열 상태를 실시간 또는 요청 시 조회하는 API입니다.")
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataTradeQueueStatusController {

    private final @Qualifier("orderQueueStatusSseHandler") SseHandler<OrdersQueueDto> sseHandler;
    private final OrderQueueStatusService orderQueueStatusService;

//    @GetMapping(value = "/order-queue/stream/{dataCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamOrderQueue(@PathVariable("dataCode") String dataCode) {
//        return sseHandler.connect(dataCode);
//    }

    @Operation(
            summary = "실시간 주문 대기열 스트리밍",
            description = "SSE(Server-Sent Events)를 통해 모든 데이터 코드의 주문 대기열 상태를 실시간으로 스트리밍합니다."
    )
    @GetMapping(value = "/order-queue/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllOrderQueue() {
        return sseHandler.connect("ALL_DATA");
    }

    @Operation(
            summary = "현재 전체 주문 대기열 상태 조회",
            description = "현재 등록된 모든 데이터 코드에 대한 주문 대기열 상태를 조회합니다."
    )
    @GetMapping("/order-queue")
    public ResponseEntity<ApiResponse> getAllOrderQueue() {
        return ResponseEntity.ok(orderQueueStatusService.getCurrentAllQueue());
    }
}
