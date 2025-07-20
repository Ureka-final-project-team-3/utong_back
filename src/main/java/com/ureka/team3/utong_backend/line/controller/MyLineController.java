package com.ureka.team3.utong_backend.line.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.line.dto.MyLineRequestDto;
import com.ureka.team3.utong_backend.line.dto.MyLineResponseDto;
import com.ureka.team3.utong_backend.line.service.MyLineService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lines")
@RequiredArgsConstructor
public class MyLineController {

    private final MyLineService mypageLineService;

    // 내 전화번호 목록 조회 + 기본 회선 표시
    @GetMapping
    @Operation(summary = "본인 전화번호 목록 조회 + 기본 회선 조회", description = "본인 전화번호가 여러개일 경우 모두 조회가능합니다. 또한, 번호마다 기본 회선 여부를 확인할 수 있습니다.")
    public ResponseEntity<ApiResponse<List<MyLineResponseDto>>> getMyLines(@AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(ApiResponse.success(mypageLineService.getMyLines(account.getId(), account.getDefaultLine())));
    }

    // 기본 회선 설정
    @PatchMapping
    @Operation(summary = "기본 회선 설정 가능", description = "본인의 기본 회선을 설정 및 수정 가능합니다.")
    public ResponseEntity<ApiResponse<List<MyLineResponseDto>>> setDefaultLine(@AuthenticationPrincipal Account account, @RequestBody MyLineRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(mypageLineService.setDefaultLine(account.getId(), requestDto.getLineId())));
    }
}

