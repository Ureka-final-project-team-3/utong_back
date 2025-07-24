package com.ureka.team3.utong_backend.auth.controller;

import com.ureka.team3.utong_backend.auth.dto.AuthDto;
import com.ureka.team3.utong_backend.auth.dto.FindAccountDto;
import com.ureka.team3.utong_backend.auth.service.AuthService;
import com.ureka.team3.utong_backend.auth.service.FindAccountService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증 API", description = "회원가입, 로그인, 토큰 갱신, 로그아웃, 계정 찾기 등의 인증 관련 API입니다.")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    private final FindAccountService findAccountService;
    
    public AuthController(AuthService authService, FindAccountService findAccountService) {
        this.authService = authService;
        this.findAccountService = findAccountService;
    }

    @Operation(summary = "회원가입", description = "회원 정보를 입력받아 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody AuthDto.SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인 후, 액세스/리프레시 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(@Valid @RequestBody AuthDto.LoginRequest request, 
                                                                   HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 기반으로 액세스 토큰을 재발급합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자의 인증 정보를 만료시킵니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, 
                                                   HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto.UserInfo>> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @Operation(summary = "계정 찾기", description = "전화번호를 기반으로 이메일 계정을 조회합니다.")
    @PostMapping("/find-account")
    public ResponseEntity<ApiResponse<FindAccountDto.FindAccountResponse>> findAccount(
            @Valid @RequestBody FindAccountDto.FindAccountRequest request) {
        return ResponseEntity.ok(findAccountService.findAccountByPhoneNumber(request));
    }
}