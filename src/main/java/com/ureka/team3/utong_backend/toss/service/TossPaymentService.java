package com.ureka.team3.utong_backend.toss.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.toss.dto.TossPaymentConfirmRequestDto;

public interface TossPaymentService {
    PointChargeResponseDto confirmAndCharge(Account account, TossPaymentConfirmRequestDto requestDto);
}
