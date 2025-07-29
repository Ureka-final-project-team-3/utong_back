package com.ureka.team3.utong_backend.coupon.repository;

import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MyCouponRepository extends JpaRepository<UserCoupon, String> {
    List<UserCoupon> findAllByUserId(String userId);

    // 유저 정보 조회 시 데이터 쿠폰 금액 합산(만약 데이터 쿠폰을 사용했을 경우)
    @Query("SELECT COALESCE(SUM(uc.amount), 0) FROM UserCoupon uc " +
            "WHERE uc.user.id = :userId " +
            "AND uc.status = '003' " +  // 사용 완료
            "AND uc.coupon.couponCode = '003'" + // 데이터 쿠폰
            "AND uc.expiredAt > CURRENT_TIMESTAMP") // 데이터 쿠폰 만기일 안 지난 것만

    Long sumUsedDataCouponAmountByUserId(@Param("userId") String userId);

}
