package com.ureka.team3.utong_backend.mypage.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.mypage.dto.MyPointDto;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeRequestDto;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.mypage.service.MypagePointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage/points")
@RequiredArgsConstructor
public class MypagePointController {

    private final MypagePointService mypagePointService;

    // 단순 포인트 조회
    @GetMapping
    public ResponseEntity<ApiResponse<MyPointDto>> getMyPoint(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(new MyPointDto(account.getMileage())));
    }


    // 포인트 충전
    @PostMapping("/charge")
    public ResponseEntity<ApiResponse<PointChargeResponseDto>> chargePoints(@AuthenticationPrincipal Account account,
            @RequestBody PointChargeRequestDto requestDto) {

        PointChargeResponseDto response = mypagePointService.chargePoints(account, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
