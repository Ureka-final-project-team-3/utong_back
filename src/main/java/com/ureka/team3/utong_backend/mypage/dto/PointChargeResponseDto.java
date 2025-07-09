package com.ureka.team3.utong_backend.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 포인트 충전 입력
@Getter
@Builder
@AllArgsConstructor
public class PointChargeResponseDto {
    private Long chargedAmount; // 유저가 입력한 충전 금액
    private Long feeAmount; // 수수료 금액
    private Long finalAmount; // 총 포인트 금액 유저 입력 금액 - 수수료 금액
    private Long updatedMileage; // 충전 이후 총 보유 포인트
}
