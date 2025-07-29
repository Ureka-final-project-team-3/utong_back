package com.ureka.team3.utong_backend.point.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.point.dto.MyPointDto;
import com.ureka.team3.utong_backend.point.dto.PointChargeRequestDto;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.point.service.PointService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "포인트 조회 API", description = "본인 포인트 조회 API 입니다.")
@RestController
@RequestMapping("/api/user/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    // 단순 포인트 조회
    @GetMapping
    @Operation(summary = "포인트 조회", description = "본인 포인트를 조회할 수 있습니다.")
    public ResponseEntity<ApiResponse<MyPointDto>> getMyPoint(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(new MyPointDto(account.getMileage())));
    }


    // 포인트 충전
    @PostMapping("/charge")
    @Operation(summary = "사용하지 않음", description = "포인트 충전은 /api/payments/confirm 을 사용해주세요. ")
    public ResponseEntity<ApiResponse<PointChargeResponseDto>> chargePoints(@AuthenticationPrincipal Account account,
            @RequestBody PointChargeRequestDto requestDto) {

        PointChargeResponseDto response = pointService.chargePoints(account, requestDto);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
