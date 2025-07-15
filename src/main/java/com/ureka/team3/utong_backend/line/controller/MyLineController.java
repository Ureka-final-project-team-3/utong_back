package com.ureka.team3.utong_backend.line.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.line.dto.MyLineRequestDto;
import com.ureka.team3.utong_backend.line.dto.MyLineResponseDto;
import com.ureka.team3.utong_backend.line.service.MyLineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage/lines")
@RequiredArgsConstructor
public class MyLineController {

    private final MyLineService mypageLineService;

    // 내 전화번호 목록 조회 + 기본 회선 표시
    @GetMapping
    public ResponseEntity<ApiResponse<List<MyLineResponseDto>>> getMyLines(@AuthenticationPrincipal Account account) {
        List<MyLineResponseDto> response = mypageLineService.getMyLines(account.getId(), account.getDefaultLine());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 기본 회선 설정
    @PatchMapping("/default")
    public ResponseEntity<ApiResponse<Void>> setDefaultLine(@AuthenticationPrincipal Account account,
                                                            @RequestBody MyLineRequestDto requestDto
    ) {
        mypageLineService.setDefaultLine(account.getId(), requestDto.getLineId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

