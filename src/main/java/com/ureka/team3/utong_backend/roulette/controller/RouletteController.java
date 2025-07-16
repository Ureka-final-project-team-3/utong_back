package com.ureka.team3.utong_backend.roulette.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.roulette.dto.RouletteDto;
import com.ureka.team3.utong_backend.roulette.service.RouletteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/roulette")
@RequiredArgsConstructor
public class RouletteController {
    
    private final RouletteService rouletteService;
    
    @GetMapping("/event")
    public ResponseEntity<ApiResponse<RouletteDto.EventInfoResponse>> getActiveEvent(@AuthenticationPrincipal Account account) {
    	System.out.println(account.toString());
        return ResponseEntity.ok(rouletteService.getActiveEventInfo(account));
    }
    
    @PostMapping("/participate")
    public ResponseEntity<ApiResponse<RouletteDto.ParticipateResponse>> participate(
            @RequestBody RouletteDto.ParticipateRequest request,
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(rouletteService.participate(request.getEventId(), account));
    }
}