package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.TradeHistoryRequestDto;
import com.ureka.team3.utong_backend.datatrade.facade.DataTradeCancelFacade;
import com.ureka.team3.utong_backend.datatrade.facade.DataTradeFacade;
import com.ureka.team3.utong_backend.datatrade.handler.SseHandler;
import com.ureka.team3.utong_backend.datatrade.service.query.TradeQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class DataTradeController {

    private final DataTradeFacade dataTradeFacade;
    private final DataTradeCancelFacade dataTradeCancelFacade;
    private final TradeQueryService tradeQueryService;

    @GetMapping("/purchase")
    public ResponseEntity<?> getMyPurchases(
            @AuthenticationPrincipal Account account, @ModelAttribute TradeHistoryRequestDto requestDto) {
        return ResponseEntity.ok(tradeQueryService.getMyPurchases(account, requestDto));
    }

    @PostMapping("/purchase")
    public ResponseEntity getPurchaseHistory(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.BuyDataRequestDto buyRequestDto) {
        return ResponseEntity.ok(dataTradeFacade.requestBuy(account, buyRequestDto));
    }

    @DeleteMapping("/purchase")
    public ResponseEntity cancelBuyWaiting(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.CancelWaitingTradeRequestDto requestDto) {
        return ResponseEntity.ok(dataTradeCancelFacade.cancelBuyWaiting(account, requestDto));
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

    @DeleteMapping("/sale")
    public ResponseEntity cancelSaleWaiting(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.CancelWaitingTradeRequestDto requestDto) {
        log.info("로그인한 id : {}", account.getId());
        return ResponseEntity.ok(dataTradeCancelFacade.cancelSaleWaiting(account, requestDto));
    }
}
