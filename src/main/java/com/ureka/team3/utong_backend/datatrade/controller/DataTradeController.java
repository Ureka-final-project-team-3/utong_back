package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.TradeHistoryRequestDto;
import com.ureka.team3.utong_backend.datatrade.facade.DataTradeFacade;
import com.ureka.team3.utong_backend.datatrade.service.chart.SseService;
import com.ureka.team3.utong_backend.datatrade.service.query.TradeQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataTradeController {

    private final DataTradeFacade dataTradeFacade;
    private final TradeQueryService tradeQueryService;
    private final SseService sseService;

    @PostMapping("/purchase")
    public ResponseEntity getPurchaseHistory(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.BuyDataRequestDto buyRequestDto) {
        return ResponseEntity.ok(dataTradeFacade.requestBuy(account, buyRequestDto));
    }

    @GetMapping("/purchase")
    public ResponseEntity<?> getMyPurchases(
            @AuthenticationPrincipal Account account, @ModelAttribute TradeHistoryRequestDto requestDto) {
        return ResponseEntity.ok(tradeQueryService.getMyPurchases(account, requestDto));
    }

    @GetMapping("/sale")
    public ResponseEntity<?> getMySales(
            @AuthenticationPrincipal Account account, TradeHistoryRequestDto requestDto) {
        return ResponseEntity.ok(tradeQueryService.getMySales(account, requestDto));
    }

    @PostMapping("/sale")
    public ResponseEntity requestSale(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.SaleDataRequestDto saleRequestDto) {
        log.info("로그인한 id : {}", account.getId());
        return ResponseEntity.ok(dataTradeFacade.requestSale(account, saleRequestDto));
    }

    @GetMapping(value = "/current-prices/stream/{dataCode}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamCurrentPrices(@PathVariable String dataCode) {
        return sseService.connectForCurrentPrice(dataCode);
    }
}
