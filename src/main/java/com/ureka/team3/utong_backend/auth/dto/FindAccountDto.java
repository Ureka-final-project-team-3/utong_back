package com.ureka.team3.utong_backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class FindAccountDto {
    
    @Data
    public static class FindAccountRequest {
        @NotBlank(message = "전화번호는 필수입니다")
        @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다 (예: 010-1234-5678)")
        private String phoneNumber;
    }
    
    @Data
    public static class FindAccountResponse {
        private String maskedEmail;
        
        public FindAccountResponse(String maskedEmail) {
            this.maskedEmail = maskedEmail;
        }
    }
}