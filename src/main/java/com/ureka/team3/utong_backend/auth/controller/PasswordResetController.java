package com.ureka.team3.utong_backend.auth.controller;

import com.ureka.team3.utong_backend.auth.dto.PasswordResetDto;
import com.ureka.team3.utong_backend.auth.service.PasswordResetService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "비밀번호 재설정 API", description = "비밀번호 찾기, 토큰 검증, 새 비밀번호 설정을 위한 API입니다.")
@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @Operation(
            summary = "비밀번호 초기화 요청",
            description = "사용자의 이메일 주소를 입력받아 비밀번호 재설정 메일을 발송합니다."
    )
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordResetDto.PasswordResetResponse>> requestPasswordReset(
            @Valid @RequestBody PasswordResetDto.PasswordResetRequest request) {
        return ResponseEntity.ok(passwordResetService.requestPasswordReset(request));
    }

    @Operation(
            summary = "비밀번호 재설정 토큰 유효성 검증",
            description = "메일로 전달받은 토큰의 유효성을 확인합니다."
    )
    @PostMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<PasswordResetDto.TokenValidationResponse>> validateResetToken(
            @Valid @RequestBody PasswordResetDto.TokenValidationRequest request) {
        return ResponseEntity.ok(passwordResetService.validateToken(request.getToken()));
    }

    @Operation(
            summary = "비밀번호 재설정",
            description = "유효한 토큰과 새 비밀번호를 입력받아 비밀번호를 재설정합니다."
    )
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetDto.NewPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }
}