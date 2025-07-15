package com.ureka.team3.utong_backend.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyCouponResponseDto {
    private String couponId;
    private String couponCode; // 030 공통코드 (예: "001" = 수수료 면제, "002" = 기프티콘)
    private String description; // 기프티콘이면 gifticon의 description, 아니면 null
    private String name; // 기프티콘 이름
    private String imageUrl; // 기프티콘 이미지
    private Long price; // 기프티콘 가격
//    private Boolean isActive; // 사용 여부
    private LocalDateTime expiredAt; // 만기
//    private String status; // 사용 가능, 사용 완료, 유효기간 만료
    private String statusCode;   // ex) "001"
    private String statusName;   // ex) "유효기간 만료"

}
