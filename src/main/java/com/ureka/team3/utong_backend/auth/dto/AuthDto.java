package com.ureka.team3.utong_backend.auth.dto;


import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class AuthDto {
    
    @Data
    public static class SignUpRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
        
        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String password;
        
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(max = 50, message = "닉네임은 50자 이하여야 합니다")
        private String nickname;
        
        private String name;
        private LocalDate birthDate;
    }
    
    @Data
    public static class LoginRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
        
        @NotBlank(message = "비밀번호는 필수입니다")
        private String password;
    }
    
    @Data
    public static class LoginResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        private Long expiresIn;
        private UserInfo userInfo;
        
        public LoginResponse(String accessToken, Long expiresIn, UserInfo userInfo) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
            this.userInfo = userInfo;
        }
    }
    
    @Data
    public static class UserInfo {
        private String accountId;
        private String email;
        private String nickname;
        private String name;
        private LocalDate birthDate;
        private Long mileage;
        
        public UserInfo(String accountId, String email, String nickname, String name, 
                       LocalDate birthDate, Long mileage) {
            this.accountId = accountId;
            this.email = email;
            this.nickname = nickname;
            this.name = name;
            this.birthDate = birthDate;
            this.mileage = mileage;
        }
    }
    
    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        private String refreshToken;
    }
    
    @Data
    public static class TokenResponse {
        private String accessToken;
        private String tokenType = "Bearer";
        private Long expiresIn;
        
        public TokenResponse(String accessToken, Long expiresIn) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
        }
    }
}