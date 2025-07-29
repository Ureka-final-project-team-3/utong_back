package com.ureka.team3.utong_backend.roulette.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.coupon.entity.Coupon;
import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.gift.entity.UserGifticon;
import com.ureka.team3.utong_backend.gift.repository.UserGifticonRepository;
import com.ureka.team3.utong_backend.roulette.entity.RouletteEvent;
import com.ureka.team3.utong_backend.user.entity.User;
import com.ureka.team3.utong_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouletteCouponService {
    private final MyCouponRepository myCouponRepository;
    private final UserRepository userRepository;
    private final UserGifticonRepository myGifticonRepository;
    @Transactional
    public ApiResponse<Void> issueWinnerCoupon(Account account, RouletteEvent event) {
        try {
            if (event.getRewardCoupon() == null) return ApiResponse.fail(ErrorCode.COUPON_NOT_FOUND);
            
            User user = userRepository.findByAccountId(account.getId())
                    .orElse(null);
            if (user == null) return ApiResponse.fail(ErrorCode.USER_NOT_FOUND);
            Coupon rewardCoupon = event.getRewardCoupon();
            if ("002".equals(rewardCoupon.getCouponCode())) 
            {
            	UserGifticon userGifticon = UserGifticon.builder()
//            							.id(UUID.randomUUID().toString())
            							.user(user)
            							.gifticon(rewardCoupon.getGifticon())
            							.isActive(true)
            							.createdAt(LocalDateTime.now())
            	                        .expiredAt(LocalDateTime.now().plusDays(3).withHour(23).withMinute(59).withSecond(59))
            							.build();
            	
            	myGifticonRepository.save(userGifticon);
                return ApiResponse.success("기프티콘이 성공적으로 발급되었습니다", null);
            }
            else
            {
                // 쿠폰 코드가 데이터 쿠폰("003")이면 amount 직접 지정
                Long amount = "003".equals(rewardCoupon.getCouponCode()) ? 1L : null; // 1GB 고정

            	UserCoupon userCoupon = UserCoupon.builder()
                        .id(UUID.randomUUID().toString())
                        .user(user)
                        .coupon(rewardCoupon)
                        .status("002")
                        .createdAt(LocalDateTime.now())
                        .expiredAt(LocalDateTime.now().plusDays(3).withHour(23).withMinute(59).withSecond(59))
                        .amount(amount)
                        .build();
                
                myCouponRepository.save(userCoupon);
                return ApiResponse.success("쿠폰이 성공적으로 발급되었습니다", null);
            }
            
            
        } catch (Exception e) {
            return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}