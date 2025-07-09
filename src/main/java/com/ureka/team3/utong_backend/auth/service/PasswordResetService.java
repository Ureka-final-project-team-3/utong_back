package com.ureka.team3.utong_backend.auth.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ureka.team3.utong_backend.auth.dto.PasswordResetDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.entity.PasswordResetToken;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.LineRepository;
import com.ureka.team3.utong_backend.auth.repository.PasswordResetTokenRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.AccountNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.InvalidTokenException;

@Service
public class PasswordResetService {
    
    private final AccountRepository accountRepository;
    private final LineRepository lineRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    
    private static final int TOKEN_EXPIRY_MINUTES = 30;
    private static final SecureRandom secureRandom = new SecureRandom();
    
    public PasswordResetService(AccountRepository accountRepository,
                               LineRepository lineRepository,
                               PasswordResetTokenRepository passwordResetTokenRepository,
                               EmailService emailService,
                               PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.lineRepository = lineRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Transactional
    public ApiResponse<PasswordResetDto.PasswordResetResponse> requestPasswordReset(
            PasswordResetDto.PasswordResetRequest request) {
        
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AccountNotFoundException("해당 이메일로 등록된 계정을 찾을 수 없습니다"));
        
        String emailFromPhone = lineRepository.findEmailByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new AccountNotFoundException("해당 전화번호로 등록된 계정을 찾을 수 없습니다"));
        
        if (!account.getEmail().equals(emailFromPhone)) {
            throw new AccountNotFoundException("이메일과 전화번호가 일치하지 않습니다");
        }
        
        invalidateExistingTokens(account.getId());
        
        String token = generateSecureToken();
        LocalDateTime now = LocalDateTime.now();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .account(account)
                .createdAt(now)
                .expiredAt(now.plusMinutes(TOKEN_EXPIRY_MINUTES))
                .used(false)
                .build();
        
        passwordResetTokenRepository.save(resetToken);
        
        emailService.sendPasswordResetEmail(account.getEmail(), token);
        
        String maskedEmail = maskEmail(account.getEmail());
        PasswordResetDto.PasswordResetResponse response = 
                new PasswordResetDto.PasswordResetResponse("비밀번호 재설정 이메일이 전송되었습니다", maskedEmail);
        
        return ApiResponse.success("비밀번호 재설정 요청이 완료되었습니다", response);
    }
    
    public ApiResponse<PasswordResetDto.TokenValidationResponse> validateToken(String token) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다"));
        
        if (resetToken.isExpired()) {
            throw new InvalidTokenException("토큰이 만료되었습니다");
        }
        
        if (resetToken.isUsed()) {
            throw new InvalidTokenException("이미 사용된 토큰입니다");
        }
        
        PasswordResetDto.TokenValidationResponse response = 
                new PasswordResetDto.TokenValidationResponse(true, resetToken.getAccount().getEmail());
        
        return ApiResponse.success("토큰이 유효합니다", response);
    }
    
    @Transactional
    public ApiResponse<Void> resetPassword(PasswordResetDto.NewPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidTokenException("비밀번호와 비밀번호 확인이 일치하지 않습니다");
        }
        
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new InvalidTokenException("유효하지 않은 토큰입니다"));
        
        if (resetToken.isExpired()) {
            throw new InvalidTokenException("토큰이 만료되었습니다");
        }
        
        if (resetToken.isUsed()) {
            throw new InvalidTokenException("이미 사용된 토큰입니다");
        }
        
        Account account = resetToken.getAccount();
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        
        Account updatedAccount = Account.builder()
                .id(account.getId())
                .email(account.getEmail())
                .password(encodedPassword)
                .nickname(account.getNickname())
                .provider(account.getProvider())
                .providerId(account.getProviderId())
                .mileage(account.getMileage())
                .user(account.getUser())
                .build();
        
        accountRepository.save(updatedAccount);
        
        resetToken.markAsUsed();
        passwordResetTokenRepository.save(resetToken);
        
        invalidateExistingTokens(account.getId());
        
        return ApiResponse.success("비밀번호가 성공적으로 변경되었습니다", null);
    }
    
    @Transactional
    public void cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
    
    private void invalidateExistingTokens(String accountId) {
        List<PasswordResetToken> existingTokens = passwordResetTokenRepository
                .findValidTokensByAccountId(accountId, LocalDateTime.now());
        
        existingTokens.forEach(PasswordResetToken::markAsUsed);
        passwordResetTokenRepository.saveAll(existingTokens);
    }
    
    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
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