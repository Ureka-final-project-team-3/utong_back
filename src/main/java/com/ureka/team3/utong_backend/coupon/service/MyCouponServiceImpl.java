package com.ureka.team3.utong_backend.coupon.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.*;
import com.ureka.team3.utong_backend.coupon.dto.CouponUseResponseDto;
import com.ureka.team3.utong_backend.coupon.dto.MyCouponResponseDto;
import com.ureka.team3.utong_backend.coupon.entity.Coupon;
import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.gift.entity.Gifticon;
import com.ureka.team3.utong_backend.user.dto.MyInfoResponseDto;
import com.ureka.team3.utong_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyCouponServiceImpl implements MyCouponService {

    private final MyCouponRepository myCouponRepository;
    private final UserService userService;

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

    @Override
    @Transactional
    public ApiResponse<CouponUseResponseDto> useCoupon(String userId, String userCouponId) {
        UserCoupon userCoupon = myCouponRepository.findById(userCouponId)
                .orElseThrow(CouponNotFoundException::new);

        // 본인 소유 쿠폰인지 검증
        if (!userCoupon.getUser().getId().equals(userId)) {
            throw new NotMyCouponException();
        }

        // 사용 가능 상태인지 확인
        if (!"002".equals(userCoupon.getStatus())) {
            throw new InvalidCouponStatusException();
        }

        // 쿠폰 코드 확인 (데이터 쿠폰 코드만 사용 가능)
        if (!"003".equals(userCoupon.getCoupon().getCouponCode())) {
            throw new NotDataCouponException();
        }

        // 쿠폰 유효기간 검증
        if (userCoupon.getExpiredAt() != null && userCoupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException();  // 새로 만든 예외 클래스 필요
        }

        // 상태 업데이트
//        log.info("사용 전 상태: {}", userCoupon.getStatus());
        userCoupon.markAsUsed();
//        log.info("사용 후 상태: {}", userCoupon.getStatus());

        Account account = userCoupon.getUser().getAccount();
        MyInfoResponseDto info = userService.getMyInfo(account);

//        return ApiResponse.success("쿠폰 사용 완료");
        return ApiResponse.success(new CouponUseResponseDto(info.getRemainingData()));



    }

}
