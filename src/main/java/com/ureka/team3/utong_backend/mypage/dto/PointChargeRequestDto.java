package com.ureka.team3.utong_backend.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// 포인트 충전 입력
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointChargeRequestDto {
    private Long chargedAmount; // 유저가 입력한 충전 금액
}
