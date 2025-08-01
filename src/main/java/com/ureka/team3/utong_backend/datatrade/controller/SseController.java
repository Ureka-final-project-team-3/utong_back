package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.service.AlertService;
import com.ureka.team3.utong_backend.datatrade.dto.chart.ChartDataDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrdersQueueDto;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sse")
public class SseController {
    private final AlertService alertService;
    private final @Qualifier("chartSseHandler") SseHandler<ChartDataDto> chartSseHandler;
        private final @Qualifier("orderQueueStatusSseHandler") SseHandler<OrdersQueueDto> queueSseHandler;
    @Operation(
            summary = "알림 전송 SSE 연결",
            description = "해당 사용자의 거래 체결 정보 알림을 전송하기 위한 SSE 연결"
    )
    @GetMapping(value = "/alert", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getWeeklyPrices(@AuthenticationPrincipal Account account) {
        return alertService.connect(account);
    }

    @Operation(
            summary = "전체 데이터 실시간 가격 스트리밍",
            description = "SSE(Server-Sent Events)를 통해 전체 데이터 코드의 실시간 가격 정보를 스트리밍합니다."
    )
    @GetMapping(value = "/chart", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllCurrentPrices() {
        return chartSseHandler.connect("ALL_DATA");
    }

    @Operation(
            summary = "실시간 주문 대기열 스트리밍",
            description = "SSE(Server-Sent Events)를 통해 모든 데이터 코드의 주문 대기열 상태를 실시간으로 스트리밍합니다."
    )
    @GetMapping(value = "/queue", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamAllOrderQueue() {
        return queueSseHandler.connect("ALL_DATA");
    }

}
