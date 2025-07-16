package com.ureka.team3.utong_backend.coupon.service;

import com.ureka.team3.utong_backend.common.exception.business.CouponNotFoundException;
import com.ureka.team3.utong_backend.coupon.dto.MyCouponResponseDto;
import com.ureka.team3.utong_backend.coupon.entity.Coupon;
import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.gift.entity.Gifticon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MyCouponServiceImpl implements MyCouponService {

    private final MyCouponRepository myCouponRepository;

    @Override
    public List<MyCouponResponseDto> getMyCoupons(String userId) {
        List<UserCoupon> userCoupons = myCouponRepository.findAllByUserId(userId);

        // 쿠폰이 없는 경우 예외 발생
        if (userCoupons.isEmpty()) {
            throw new CouponNotFoundException(); // 커스텀 예외 던지기
        }

        LocalDateTime now = LocalDateTime.now();

        return userCoupons.stream().map(userCoupon -> {
            Coupon coupon = userCoupon.getCoupon();
            Gifticon gifticon = coupon.getGifticon();

            String statusCode = userCoupon.getStatus(); // ex: "001"
            String statusName = getStatusName(statusCode);



//            String status;
//            if (Boolean.TRUE.equals(coupon.getIsActive())) {
//                status = "사용 완료";
//            } else if (userCoupon.getExpiredAt() != null && userCoupon.getExpiredAt().isBefore(now)) {
//                status = "유효기간 만료";
//            } else {
//                status = "사용 가능";
//            }


            return MyCouponResponseDto.builder()
                    .userCouponId(userCoupon.getId())
                    .couponId(coupon.getId())
                    .couponCode(coupon.getCouponCode())
                    .description(gifticon != null ? gifticon.getDescription() : null)
                    .name(gifticon != null ? gifticon.getName() : null)
                    .imageUrl(gifticon != null ? gifticon.getImageUrl() : null)
                    .price(gifticon != null ? gifticon.getPrice() : null)
                    .expiredAt(userCoupon.getExpiredAt())
                    .statusCode(statusCode)
                    .statusName(statusName)
                    .build();
        }).collect(Collectors.toList());
    }

    private String getStatusName(String statusCode) {
        return switch (statusCode) {
            case "001" -> "유효기간 만료";
            case "002" -> "사용 가능";
            case "003" -> "사용 완료";
            default -> "알 수 없음";
        };
    }
}
