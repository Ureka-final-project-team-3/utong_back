package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.TradeHistoryRequestDto;
import com.ureka.team3.utong_backend.datatrade.facade.DataTradeFacade;
import com.ureka.team3.utong_backend.datatrade.service.TradeQueryService;
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
    private final TradeQueryService tradeQueryService;

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
}
