package com.ureka.team3.utong_backend.payment.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.payment.dto.PaymentConfirmRequestDto;
import com.ureka.team3.utong_backend.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

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
    public ResponseEntity<ApiResponse<PointChargeResponseDto>> confirmPayment(@AuthenticationPrincipal Account account, @RequestBody PaymentConfirmRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.confirmAndCharge(account, requestDto)));
    }
}
