package com.ureka.team3.utong_backend.payment.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.payment.dto.PaymentConfirmRequestDto;
import com.ureka.team3.utong_backend.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
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
    // 결제 승인
    @PostMapping("/confirm")
    @Operation(summary = "포인트 충전 결제 연동", description = "포인트 충전 시 결제 api 연동으로 결제 가능합니다. 수수료 면제 쿠폰이 있는 경우 적용할 수 있습니다.")
    public ResponseEntity<ApiResponse<PointChargeResponseDto>> confirmPayment(@AuthenticationPrincipal Account account, @RequestBody PaymentConfirmRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.confirmAndCharge(account, requestDto)));
    }
}
