package com.ureka.team3.utong_backend.coupon.service;

import com.ureka.team3.utong_backend.coupon.dto.MyCouponResponseDto;
import com.ureka.team3.utong_backend.coupon.entity.Coupon;
import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.gift.entity.Gifticon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class MyCouponServiceTest {

    @Mock
    private MyCouponRepository myCouponRepository;

    @InjectMocks
    private MyCouponServiceImpl myCouponService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMyCoupons_shouldReturnCorrectDtoList() {
        // given
        String userId = "user-123";
        Gifticon gifticon = Gifticon.builder()
                .name("기프티콘")
                .description("설명")
                .imageUrl("http://image.com")
                .price(1500L)
                .build();

        Coupon coupon = Coupon.builder()
                .id("coupon-123")
                .gifticon(gifticon)
                .couponCode("002")
                .isActive(false)
                .expiredAt(LocalDateTime.now().plusDays(5))
                .build();

        UserCoupon userCoupon = UserCoupon.builder()
                .id("user-coupon-123")
                .coupon(coupon)
                .expiredAt(LocalDateTime.now().plusDays(5))
                .build();

        when(myCouponRepository.findAllByUserId(userId))
                .thenReturn(Collections.singletonList(userCoupon));

        // when
        List<MyCouponResponseDto> result = myCouponService.getMyCoupons(userId);

        // then
        assertThat(result).hasSize(1);
        MyCouponResponseDto dto = result.get(0);
        assertThat(dto.getCouponId()).isEqualTo("coupon-123");
        assertThat(dto.getName()).isEqualTo("기프티콘");
        assertThat(dto.getStatus()).isEqualTo("사용 가능");
    }
}
