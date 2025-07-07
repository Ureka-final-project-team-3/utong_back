package com.ureka.team3.utong_backend.auth.service;

import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ureka.team3.utong_backend.auth.dto.AuthDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;
import com.ureka.team3.utong_backend.auth.util.JwtProperties;
import com.ureka.team3.utong_backend.auth.util.JwtUtil;

@Service
public class AuthService {
    
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final JwtProperties jwtProperties;
    
    public AuthService(AccountRepository accountRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      AuthenticationManager authenticationManager,
                      JwtUtil jwtUtil,
                      RedisTokenService redisTokenService,
                      JwtProperties jwtProperties) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
        this.jwtProperties = jwtProperties;
    }
    
    @Transactional
    public void signUp(AuthDto.SignUpRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다");
        }
        
        String accountId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        
        Account account = Account.builder()
                .id(accountId)
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .mileage(0L)
                .build();
        
        accountRepository.save(account);
        
        User user = User.builder()
                .id(userId)
                .name(request.getName())
                .birthDate(request.getBirthDate())
                .account(account)
                .build();
        
        userRepository.save(user);
    }
    
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        Account account = (Account) authentication.getPrincipal();
        User user = userRepository.findByAccountId(account.getId()).orElse(null);
        
        String accessToken = jwtUtil.generateAccessToken(account.getId(), account.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(account.getId());
        
        redisTokenService.saveRefreshToken(account.getId(), refreshToken);
        
        AuthDto.UserInfo userInfo = new AuthDto.UserInfo(
                account.getId(),
                account.getEmail(),
                account.getNickname(),
                user != null ? user.getName() : null,
                user != null ? user.getBirthDate() : null,
                account.getMileage()
        );
        
        return new AuthDto.LoginResponse(accessToken, jwtProperties.getAccessTokenExpiration(), userInfo);
    }
    
    public AuthDto.TokenResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
        }
        
        String accountId = jwtUtil.extractAccountId(refreshToken);
        String storedRefreshToken = redisTokenService.getRefreshToken(accountId);
        
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다");
        }
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다"));
        
        String newAccessToken = jwtUtil.generateAccessToken(account.getId(), account.getEmail());
        
        return new AuthDto.TokenResponse(newAccessToken, jwtProperties.getAccessTokenExpiration());
    }
    
    public void logout(String accountId, String accessToken) {
        redisTokenService.deleteRefreshToken(accountId);
        
        long remainingTime = jwtUtil.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
        if (remainingTime > 0) {
            redisTokenService.blacklistAccessToken(accessToken, remainingTime);
        }
    }
}