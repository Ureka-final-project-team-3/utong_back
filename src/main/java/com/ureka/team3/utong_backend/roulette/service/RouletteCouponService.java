package com.ureka.team3.utong_backend.roulette.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.coupon.entity.Coupon;
import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.roulette.entity.RouletteEvent;
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
    
    
    @Transactional
    public void issueWinnerCoupon(Account account, RouletteEvent event) {
        if (event.getRewardCoupon() == null) {
            log.warn("이벤트에 보상 쿠폰이 설정되지 않음 - EventId: {}", event.getId());
            return;
        }
        
        User user = userRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
        
        Coupon rewardCoupon = event.getRewardCoupon();
        
        UserCoupon userCoupon = UserCoupon.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .coupon(rewardCoupon)
                .createdAt(LocalDateTime.now())
                .expiredAt(rewardCoupon.getExpiredAt()) 
                .build();
        
        myCouponRepository.save(userCoupon);
        
        log.info("룰렛 당첨 쿠폰 발급 완료 - UserId: {}, CouponId: {}, EventId: {}", 
                user.getId(), rewardCoupon.getId(), event.getId());
    }
}