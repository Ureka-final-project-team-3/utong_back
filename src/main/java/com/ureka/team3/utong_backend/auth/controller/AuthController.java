package com.ureka.team3.utong_backend.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ureka.team3.utong_backend.auth.dto.AuthDto;
import com.ureka.team3.utong_backend.auth.dto.FindAccountDto;
import com.ureka.team3.utong_backend.auth.service.AuthService;
import com.ureka.team3.utong_backend.auth.service.FindAccountService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    private final FindAccountService findAccountService;
    
    public AuthController(AuthService authService, FindAccountService findAccountService) {
        this.authService = authService;
        this.findAccountService = findAccountService;
    }
    
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signUp(@Valid @RequestBody AuthDto.SignUpRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(@Valid @RequestBody AuthDto.LoginRequest request, 
                                                                   HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> refreshToken(HttpServletRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request, 
                                                   HttpServletResponse response) {
        return ResponseEntity.ok(authService.logout(request, response));
    }
    
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthDto.UserInfo>> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }
    
    @PostMapping("/find-account")
    public ResponseEntity<ApiResponse<FindAccountDto.FindAccountResponse>> findAccount(
            @Valid @RequestBody FindAccountDto.FindAccountRequest request) {
        return ResponseEntity.ok(findAccountService.findAccountByPhoneNumber(request));
    }
}