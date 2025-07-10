package com.ureka.team3.utong_backend.auth.service;

import org.springframework.stereotype.Service;

import com.ureka.team3.utong_backend.auth.dto.FindAccountDto;
import com.ureka.team3.utong_backend.auth.repository.LineRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.AccountNotFoundException;

@Service
public class FindAccountService {
    
    private final LineRepository lineRepository;
    
    public FindAccountService(LineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }
    
    public ApiResponse<FindAccountDto.FindAccountResponse> findAccountByPhoneNumber(
            FindAccountDto.FindAccountRequest request) {
        
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        
        String email = lineRepository.findEmailByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new AccountNotFoundException("해당 전화번호로 등록된 계정을 찾을 수 없습니다"));
        
        String maskedEmail = maskEmail(email);
        
        FindAccountDto.FindAccountResponse response = new FindAccountDto.FindAccountResponse(maskedEmail);
        
        return ApiResponse.success("계정을 찾았습니다", response);
    }
    
    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber;
    }
    
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "*@" + domain;
        }
        
        if (localPart.length() <= 4) {
            return localPart.substring(0, 2) + "*".repeat(localPart.length() - 2) + "@" + domain;
        }
        
        if (localPart.length() == 5) {
            return localPart.substring(0, 2) + "***" + "@" + domain;
        }
        
        int frontChars = 2;
        int backChars = 3;
        int maskChars = Math.min(3, localPart.length() - frontChars - backChars);
        
        String front = localPart.substring(0, frontChars);
        String back = localPart.substring(localPart.length() - backChars);
        String mask = "*".repeat(maskChars);
        
        return front + mask + back + "@" + domain;
    }
}