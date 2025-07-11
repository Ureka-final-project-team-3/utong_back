package com.ureka.team3.utong_backend.gift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

// 기프티콘 요약 응답 DTO
@Data
@Builder
@AllArgsConstructor
public class MyGifticonResponseDto {
    private UUID id; // userGifticonId
    private String name; // 예: "베스킨라빈스"
    private String description; // 예: "베라 파인트"
    private Long price;  // 예: 15000
    private String imageUrl;
    private long daysRemaining; // D-30 계산
    private String status; // "사용 가능", "유효기간 만료", "사용 완료"
}
