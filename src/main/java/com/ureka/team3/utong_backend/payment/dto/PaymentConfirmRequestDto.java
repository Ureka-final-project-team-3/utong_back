package com.ureka.team3.utong_backend.payment.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmRequestDto {
    private String paymentKey;
    private String orderId;
    private Long amount;
    private String userCouponId; // 쿠폰 ID
}
