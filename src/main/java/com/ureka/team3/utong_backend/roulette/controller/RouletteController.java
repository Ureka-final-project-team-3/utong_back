package com.ureka.team3.utong_backend.roulette.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.roulette.dto.RouletteDto;
import com.ureka.team3.utong_backend.roulette.service.RouletteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "룰렛 이벤트 API", description = "룰렛 이벤트 조회 및 참여 관련 API입니다.")
@RestController
@RequestMapping("/api/roulette")
@RequiredArgsConstructor
public class RouletteController {
    
    private final RouletteService rouletteService;

    @Operation(summary = "현재 진행 중인 룰렛 이벤트 조회", description = "로그인된 사용자가 참여 가능한 룰렛 이벤트 정보를 조회합니다.")
    @GetMapping("/event")
    public ResponseEntity<ApiResponse<RouletteDto.EventInfoResponse>> getActiveEvent(@AuthenticationPrincipal Account account) {
    	System.out.println(account.toString());
        return ResponseEntity.ok(rouletteService.getActiveEventInfo(account));
    }

    @Operation(summary = "룰렛 이벤트 참여", description = "로그인된 사용자가 룰렛 이벤트에 참여합니다.")
    @PostMapping("/participate")
    public ResponseEntity<ApiResponse<RouletteDto.ParticipateResponse>> participate(
            @RequestBody RouletteDto.ParticipateRequest request,
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(rouletteService.participate(request.getEventId(), account));
    }
}