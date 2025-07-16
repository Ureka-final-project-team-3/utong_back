package com.ureka.team3.utong_backend.toss.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.toss.dto.TossPaymentConfirmRequestDto;

public interface TossPaymentService {

//    // 토스 걸제
//    TossPaymentResponseDto startPayment(Account account, TossPaymentRequestDto requestDto);

    // 토스 결제 승인
    PointChargeResponseDto confirmAndCharge(Account account, TossPaymentConfirmRequestDto requestDto);
}
