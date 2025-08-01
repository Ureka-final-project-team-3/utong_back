//package com.ureka.team3.utong_backend.datatrade.controller;
//
//import com.ureka.team3.utong_backend.auth.entity.Account;
//import com.ureka.team3.utong_backend.common.dto.ApiResponse;
//import com.ureka.team3.utong_backend.datatrade.alert.AlertService;
//import com.ureka.team3.utong_backend.datatrade.dto.WeeklyChartDto;
//import com.ureka.team3.utong_backend.datatrade.handler.AlertSseHandler;
//import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
//import io.swagger.v3.oas.annotations.Operation;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//@RestController@RequiredArgsConstructor
//@RequestMapping("/sse/data")
//public class AlertController {
//    private final AlertService alertService;
//    @Operation(
//            summary = "알림 전송 SSE 연결",
//            description = "해당 사용자의 거래 체결 정보 알림을 전송하기 위한 SSE 연결"
//    )
//    @GetMapping(value = "/alert", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter getWeeklyPrices(@AuthenticationPrincipal Account account) {
//        return alertService.connect(account);
//    }
//}
