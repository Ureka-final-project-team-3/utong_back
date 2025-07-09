package com.ureka.team3.utong_backend.mypage.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeRequestDto;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeResponseDto;

public interface MypagePointService {
    PointChargeResponseDto chargePoints(Account account, PointChargeRequestDto requestDto);
}
