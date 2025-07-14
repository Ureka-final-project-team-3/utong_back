package com.ureka.team3.utong_backend.roulette.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.roulette.dto.RouletteDto;
import com.ureka.team3.utong_backend.roulette.service.RouletteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/roulette")
@RequiredArgsConstructor
public class RouletteController {
    
    private final RouletteService rouletteService;
    
    @GetMapping("/event")
    public ResponseEntity<ApiResponse<RouletteDto.EventInfoResponse>> getActiveEvent(
            @AuthenticationPrincipal Account account) {
        
        RouletteDto.EventInfoResponse eventInfo = rouletteService.getActiveEventInfo(account);
        return ResponseEntity.ok(ApiResponse.success("이벤트정보 조회", eventInfo));
    }
    
    @PostMapping("/participate")
    public ResponseEntity<ApiResponse<RouletteDto.ParticipateResponse>> participate(
            @RequestBody RouletteDto.ParticipateRequest request,
            @AuthenticationPrincipal Account account) {
        
        RouletteDto.ParticipateResponse result = rouletteService.participate(
                request.getEventId(), account);
        
        return ResponseEntity.ok(ApiResponse.success("룰렛 참여 완료", result));
    }
}