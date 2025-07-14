package com.ureka.team3.utong_backend.coupon.service;

import com.ureka.team3.utong_backend.coupon.dto.MyCouponResponseDto;

import java.util.List;

public interface MyCouponService {
    List<MyCouponResponseDto> getMyCoupons(String userId);
}
