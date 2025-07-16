package com.ureka.team3.utong_backend.point.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.point.dto.MyPointDto;
import com.ureka.team3.utong_backend.point.dto.PointChargeRequestDto;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // 단순 포인트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<MyPointDto>> getMyPoint(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(new MyPointDto(account.getMileage())));
    }


    // 포인트 충전
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<PointChargeResponseDto>> chargePoints(@AuthenticationPrincipal Account account,
            @RequestBody PointChargeRequestDto requestDto) {

        PointChargeResponseDto response = pointService.chargePoints(account, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
