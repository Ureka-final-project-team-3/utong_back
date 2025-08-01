//package com.ureka.team3.utong_backend.datatrade.controller;
//
//import com.ureka.team3.utong_backend.common.dto.ApiResponse;
//import com.ureka.team3.utong_backend.datatrade.dto.ChartDataDto;
//import com.ureka.team3.utong_backend.datatrade.dto.WeeklyChartDto;
//import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
//import com.ureka.team3.utong_backend.datatrade.service.chart.weekly.WeeklyPriceService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
//
//@Tag(name = "데이터 거래 차트 API", description = "실시간 가격 스트리밍 및 주간 데이터 차트를 제공합니다.")
//@RestController
//@RequiredArgsConstructor
////@RequestMapping("/api/data")
//public class DataTradeChartController {
//
//    private final @Qualifier("chartSseHandler")SseHandler<ChartDataDto> sseHandler;
//    private final WeeklyPriceService weeklyPriceService;
//
//    @GetMapping(value = "/current-prices/stream/{dataCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter streamCurrentPrices(@PathVariable("dataCode") String dataCode) {
//        return sseHandler.connect(dataCode);
//    }
//
//    @Operation(
//            summary = "전체 데이터 실시간 가격 스트리밍",
//            description = "SSE(Server-Sent Events)를 통해 전체 데이터 코드의 실시간 가격 정보를 스트리밍합니다."
//    )
//    @GetMapping(value = "/sse/data/current-prices/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE) // todo : /chart/hourly 로 변경
//    public SseEmitter streamAllCurrentPrices() {
//        return sseHandler.connect("ALL_DATA");
//    }
//
//    @Operation(
//            summary = "주간 데이터 가격 조회",
//            description = "특정 데이터 코드에 대한 주간 가격 정보를 조회합니다."
//    )
//    @GetMapping("/api/data/weekly-prices/{dataCode}")    // todo : /chart/weekly로 변경
//    public ResponseEntity<ApiResponse<WeeklyChartDto>> getWeeklyPrices(@PathVariable("dataCode") String dataCode) {
//        return ResponseEntity.ok(weeklyPriceService.getWeeklyPrice(dataCode));
//    }
//
//}
