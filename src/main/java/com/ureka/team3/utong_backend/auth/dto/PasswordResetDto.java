package com.ureka.team3.utong_backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

public class PasswordResetDto {
    
    @Data
    public static class PasswordResetRequest {
        @NotBlank(message = "이메일은 필수입니다")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;
        
        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        private String phoneNumber;
    }
    
    @Data
    public static class PasswordResetResponse {
        private String message;
        private String email;
        
        public PasswordResetResponse(String message, String email) {
            this.message = message;
            this.email = email;
        }
    }
    
    @Data
    public static class NewPasswordRequest {
        @NotBlank(message = "토큰은 필수입니다")
        private String token;
        
        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다")
        private String newPassword;
        
        @NotBlank(message = "비밀번호 확인은 필수입니다")
        private String confirmPassword;
    }
    
    @Data
    public static class TokenValidationResponse {
        private boolean valid;
        private String email;
        
        public TokenValidationResponse(boolean valid, String email) {
            this.valid = valid;
            this.email = email;
        }
    }
}