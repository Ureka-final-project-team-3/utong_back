package com.ureka.team3.utong_backend.coupon.repository;

import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MyCouponRepository extends JpaRepository<UserCoupon, String> {
    List<UserCoupon> findAllByUserId(String userId);
}
