//package com.ureka.team3.utong_backend.datatrade.controller;
//
//import com.ureka.team3.utong_backend.auth.entity.Account;
//import com.ureka.team3.utong_backend.common.dto.ApiResponse;
//import com.ureka.team3.utong_backend.datatrade.dto.MyTradeHistoryRequestDto;
//import com.ureka.team3.utong_backend.datatrade.service.MyTradeService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/mypage/trade")
//public class MyTradeController {
//
//    private final MyTradeService myTradeService;
//
//    @PostMapping("/purchases")
//    public ResponseEntity<?> getMyPurchases(@AuthenticationPrincipal Account account, @RequestBody(required = false) MyTradeHistoryRequestDto request) {
//        return ResponseEntity.ok(ApiResponse.success(myTradeService.getMyPurchases(account, request != null ? request.getFromDate() : null)));
//    }
//
//    @PostMapping("/sales")
//    public ResponseEntity<?> getMySales(@AuthenticationPrincipal Account account, @RequestBody(required = false) MyTradeHistoryRequestDto request) {
//        return ResponseEntity.ok(ApiResponse.success(myTradeService.getMySales(account, request != null ? request.getFromDate() : null)));
//    }
//}

package com.ureka.team3.utong_backend.datatrade.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.MyTradeHistoryRequestDto;
import com.ureka.team3.utong_backend.datatrade.service.MyTradeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/mypage/trade")
public class MyTradeController {

    private final MyTradeService myTradeService;

    @PostMapping("/purchases")
    public ResponseEntity<?> getMyPurchases(
            @AuthenticationPrincipal Account account,
            @RequestBody(required = false) MyTradeHistoryRequestDto request
    ) {
        String range = request != null ? request.getRange() : null;
        return ResponseEntity.ok(ApiResponse.success(myTradeService.getMyPurchases(account, range)));
    }

    @PostMapping("/sales")
    public ResponseEntity<?> getMySales(
            @AuthenticationPrincipal Account account,
            @RequestBody(required = false) MyTradeHistoryRequestDto request
    ) {
        String range = request != null ? request.getRange() : null;
        return ResponseEntity.ok(ApiResponse.success(myTradeService.getMySales(account, range)));
    }
}
