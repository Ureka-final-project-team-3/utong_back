package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.service.DataTradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DataTradeController {

    private final DataTradeService dataTradeService;

    @PostMapping("/data/purchase")
    public ResponseEntity requestBuy(@AuthenticationPrincipal UserDetails userDetails, @RequestBody DataTradeDto.BuyDataRequestDto purchaseRequestDto){
        return ResponseEntity.ok(dataTradeService.requestBuy(userDetails.getUsername(),purchaseRequestDto));
    }

    @PostMapping("/data/sale")
    public ResponseEntity requestSale(@AuthenticationPrincipal UserDetails userDetails, @RequestBody DataTradeDto.SaleDataRequestDto saleRequestDto) {
        return ResponseEntity.ok(dataTradeService.requestSale(userDetails.getUsername(), saleRequestDto));
    }
}
