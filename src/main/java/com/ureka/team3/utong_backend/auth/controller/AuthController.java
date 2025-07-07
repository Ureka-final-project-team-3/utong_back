package com.ureka.team3.utong_backend.auth.controller;


import java.util.Arrays;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ureka.team3.utong_backend.auth.dto.AuthDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.AuthService;
import com.ureka.team3.utong_backend.auth.util.JwtProperties;
import com.ureka.team3.utong_backend.auth.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    
    public AuthController(AuthService authService, JwtUtil jwtUtil, JwtProperties jwtProperties) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
        this.jwtProperties = jwtProperties;
    }
    
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signUp(@Valid @RequestBody AuthDto.SignUpRequest request) {
        try {
            authService.signUp(request);
            return ResponseEntity.ok(Map.of("message", "회원가입이 완료되었습니다"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest request, 
                                  HttpServletResponse response) {
        try {
            AuthDto.LoginResponse loginResponse = authService.login(request);
            
            Cookie refreshTokenCookie = createRefreshTokenCookie("refresh_token", 
                    jwtUtil.generateRefreshToken(loginResponse.getUserInfo().getAccountId()));
            response.addCookie(refreshTokenCookie);
            
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "이메일 또는 비밀번호가 올바르지 않습니다"));
        }
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        try {
            String refreshToken = getRefreshTokenFromCookie(request);
            if (refreshToken == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "리프레시 토큰이 없습니다"));
            }
            
            AuthDto.TokenResponse tokenResponse = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(tokenResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, 
                                                     HttpServletResponse response) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof 
                Account account) {
                
                String accessToken = getAccessTokenFromRequest(request);
                authService.logout(account.getId(), accessToken);
                
                Cookie refreshTokenCookie = createRefreshTokenCookie("refresh_token", "");
                refreshTokenCookie.setMaxAge(0);
                response.addCookie(refreshTokenCookie);
            }
            
            return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof 
                Account account) {
                
                AuthDto.UserInfo userInfo = new AuthDto.UserInfo(
                        account.getId(),
                        account.getEmail(),
                        account.getNickname(),
                        account.getUser() != null ? account.getUser().getName() : null,
                        account.getUser() != null ? account.getUser().getBirthDate() : null,
                        account.getMileage()
                );
                
                return ResponseEntity.ok(userInfo);
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "인증되지 않은 사용자입니다"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    private Cookie createRefreshTokenCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
        return cookie;
    }
    
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "refresh_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
    
    private String getAccessTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}