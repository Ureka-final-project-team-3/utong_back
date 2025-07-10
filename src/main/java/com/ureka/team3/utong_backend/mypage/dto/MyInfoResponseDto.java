package com.ureka.team3.utong_backend.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// 내 정보 조회
@Data
@Builder
@AllArgsConstructor
public class MyInfoResponseDto {
    private String name;
    private String email;
    private Long mileage;
    private String phoneNumber;
    private Long remainingData;
}
