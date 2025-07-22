package com.ureka.team3.utong_backend.payment.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.payment.dto.PaymentConfirmRequestDto;

public interface PaymentService {
    // 토스 결제 승인
    PointChargeResponseDto confirmAndCharge(Account account, PaymentConfirmRequestDto requestDto);
}
