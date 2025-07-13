package com.ureka.team3.utong_backend.gift.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class MyGifticonDetailResponseDto {
    private String id;
    private String name;
    private String description;
    private Long price;
    private String imageUrl;
    private long daysRemaining;
    private String status;
    private String createdAt;
    private String expiredAt;
}
