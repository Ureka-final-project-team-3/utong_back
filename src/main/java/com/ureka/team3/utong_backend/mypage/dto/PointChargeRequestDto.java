package com.ureka.team3.utong_backend.mypage.dto;

import lombok.Getter;

// 포인트 충전 입력
@Getter
public class PointChargeRequestDto {
    private Long chargedAmount; // 유저가 입력한 충전 금액
}
