package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.service.DataTradeService;
import jakarta.persistence.Access;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DataTradeController {

    private final DataTradeService dataTradeService;

    @PostMapping("/data/purchase")
    public ResponseEntity requestBuy(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.BuyDataRequestDto buyRequestDto){
        return ResponseEntity.ok(dataTradeService.requestBuy(account,buyRequestDto));
    }

    @PostMapping("/data/sale")
    public ResponseEntity requestSale(@AuthenticationPrincipal Account account, @RequestBody DataTradeDto.SaleDataRequestDto saleRequestDto) {
        log.info("로그인한 id : {}",account.getId());
        return ResponseEntity.ok(dataTradeService.requestSale(account, saleRequestDto));
    }
}
