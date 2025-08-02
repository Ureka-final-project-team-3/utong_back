package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.TradeHistoryRequestDto;
import com.ureka.team3.utong_backend.datatrade.dto.chart.WeeklyChartDto;
import com.ureka.team3.utong_backend.datatrade.facade.DataTradeCancelFacade;
import com.ureka.team3.utong_backend.datatrade.facade.DataTradeFacade;
import com.ureka.team3.utong_backend.datatrade.service.chart.weekly.WeeklyPriceService;
import com.ureka.team3.utong_backend.datatrade.service.query.TradeQueryService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "데이터 거래 API", description = "데이터 구매/판매 및 거래 내역 조회, 대기 취소 관련 API를 제공합니다.")
@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataTradeController {

    private final DataTradeFacade dataTradeFacade;
    private final DataTradeCancelFacade dataTradeCancelFacade;
    private final TradeQueryService tradeQueryService;
    private final WeeklyPriceService weeklyPriceService;
    private final QueueStatusService queueStatusService;

    @Operation(summary = "내 구매내역 조회", description = "로그인한 사용자의 데이터 구매 내역을 조건에 따라 조회합니다.")
    @GetMapping("/purchase")
    public ResponseEntity<?> getMyPurchases(
            @AuthenticationPrincipal Account account, @ModelAttribute TradeHistoryRequestDto requestDto) {
        return ResponseEntity.ok(tradeQueryService.getMyPurchases(account, requestDto));
    }

    @Operation(summary = "데이터 구매 요청", description = "지정한 데이터 코드와 수량으로 데이터 구매를 요청합니다.")
    @PostMapping("/purchase")
    public ResponseEntity getPurchaseHistory(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.DataTradeRequestDto buyRequestDto) {
        return ResponseEntity.ok(dataTradeFacade.requestBuy(account, buyRequestDto));
    }

    @Operation(summary = "구매 대기 취소", description = "사용자의 대기 상태인 구매 요청을 취소합니다.")
    @DeleteMapping("/purchase")
    public ResponseEntity cancelBuyWaiting(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.CancelWaitingTradeRequestDto requestDto) {
        return ResponseEntity.ok(dataTradeCancelFacade.cancelBuyWaiting(account, requestDto));
    }

    @Operation(summary = "내 판매내역 조회", description = "로그인한 사용자의 데이터 판매 내역을 조건에 따라 조회합니다.")
    @GetMapping("/sale")
    public ResponseEntity<?> getMySales(
            @AuthenticationPrincipal Account account, TradeHistoryRequestDto requestDto) {
        return ResponseEntity.ok(tradeQueryService.getMySales(account, requestDto));
    }

    @Operation(summary = "데이터 판매 요청", description = "지정한 데이터 코드와 수량으로 데이터를 판매 요청합니다.")
    @PostMapping("/sale")
    public ResponseEntity requestSale(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.DataTradeRequestDto saleRequestDto) {
        log.info("로그인한 id : {}", account.getId());
        return ResponseEntity.ok(dataTradeFacade.requestSale(account, saleRequestDto));
    }

    @Operation(summary = "판매 대기 취소", description = "사용자의 대기 상태인 판매 요청을 취소합니다.")
    @DeleteMapping("/sale")
    public ResponseEntity cancelSaleWaiting(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.CancelWaitingTradeRequestDto requestDto) {
        log.info("로그인한 id : {}", account.getId());
        return ResponseEntity.ok(dataTradeCancelFacade.cancelSaleWaiting(account, requestDto));
    }

    @GetMapping("/weekly-prices/{dataCode}")
    public ResponseEntity<ApiResponse<WeeklyChartDto>> getWeeklyPrices(@PathVariable("dataCode") String dataCode) {
        return ResponseEntity.ok(weeklyPriceService.getWeeklyPrice(dataCode));
    }

    @Operation(
            summary = "현재 전체 주문 대기열 상태 조회",
            description = "현재 등록된 모든 데이터 코드에 대한 주문 대기열 상태를 조회합니다."
    )
    @GetMapping("/api/data/order-queue")
    public ResponseEntity<ApiResponse> getAllOrderQueue() {
        return ResponseEntity.ok(queueStatusService.getCurrentAllQueue());
    }
}
