package com.ureka.team3.utong_backend.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ureka.team3.utong_backend.auth.dto.PasswordResetDto;
import com.ureka.team3.utong_backend.auth.service.PasswordResetService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {
    
    private final PasswordResetService passwordResetService;
    
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<PasswordResetDto.PasswordResetResponse>> requestPasswordReset(
            @Valid @RequestBody PasswordResetDto.PasswordResetRequest request) {
        return ResponseEntity.ok(passwordResetService.requestPasswordReset(request));
    }
    
    @GetMapping("/validate-reset-token")
    public ResponseEntity<ApiResponse<PasswordResetDto.TokenValidationResponse>> validateResetToken(
            @RequestParam("token") String token) {
        return ResponseEntity.ok(passwordResetService.validateToken(token));
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetDto.NewPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }
}