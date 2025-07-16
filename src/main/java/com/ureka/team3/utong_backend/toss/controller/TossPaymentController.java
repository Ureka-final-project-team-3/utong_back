package com.ureka.team3.utong_backend.toss.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.toss.dto.TossPaymentConfirmRequestDto;
import com.ureka.team3.utong_backend.toss.service.TossPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

//    // 결제 시작
//    @PostMapping("/pay")
//    public ResponseEntity<ApiResponse<TossPaymentResponseDto>> startPayment(
//            @AuthenticationPrincipal Account account,
//            @RequestBody TossPaymentRequestDto requestDto) {
//
//        TossPaymentResponseDto response = tossPaymentService.startPayment(account, requestDto);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }


    // 결제 승인
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<PointChargeResponseDto>> confirmPayment(
            @AuthenticationPrincipal Account account,
            @RequestBody TossPaymentConfirmRequestDto requestDto) {

        PointChargeResponseDto response = tossPaymentService.confirmAndCharge(account, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
