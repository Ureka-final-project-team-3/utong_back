package com.ureka.team3.utong_backend.coupon.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.coupon.dto.MyCouponResponseDto;
import com.ureka.team3.utong_backend.coupon.service.MyCouponService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/coupons")
@RequiredArgsConstructor
public class MyCouponController {

    private final MyCouponService myCouponService;

    @GetMapping
    @Operation(summary = "내 쿠폰 목록 조회", description = "로그인된 사용자의 보유 쿠폰 목록을 반환합니다.")
    public ResponseEntity<ApiResponse<List<MyCouponResponseDto>>> getMyCoupons(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(myCouponService.getMyCoupons(account.getUser().getId())));
    }
}
