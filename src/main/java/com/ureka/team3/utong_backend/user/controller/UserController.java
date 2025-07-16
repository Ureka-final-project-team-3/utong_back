package com.ureka.team3.utong_backend.user.controller;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.user.dto.MyInfoResponseDto;
import com.ureka.team3.utong_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회(이름, 이메일, 포인트, 핸드폰, 남은 데이터 용량)
    @GetMapping("/info")
    public ResponseEntity<ApiResponse<MyInfoResponseDto>> getMyInfo(@AuthenticationPrincipal Account account){
        return ResponseEntity.ok(ApiResponse.success(userService.getMyInfo(account)));
    }
}
