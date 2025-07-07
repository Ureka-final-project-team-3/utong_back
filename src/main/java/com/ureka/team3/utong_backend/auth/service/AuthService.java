package com.ureka.team3.utong_backend.auth.service;

import java.util.Arrays;
import java.util.UUID;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.InvalidPasswordException;
import com.ureka.team3.utong_backend.common.exception.business.InvalidTokenException;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
    public ApiResponse<Void> signUp(AuthDto.SignUpRequest request) {
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new InvalidPasswordException("이미 존재하는 이메일입니다");
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
        
        return ApiResponse.success("회원가입이 완료되었습니다", null);
    }
    
    public ApiResponse<AuthDto.LoginResponse> login(AuthDto.LoginRequest request, HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            Account account = (Account) authentication.getPrincipal();
            User user = userRepository.findByAccountId(account.getId()).orElse(null);
            
            String accessToken = jwtUtil.generateAccessToken(account.getId(), account.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(account.getId());
            
            redisTokenService.saveRefreshToken(account.getId(), refreshToken);
            
            Cookie refreshTokenCookie = createRefreshTokenCookie("refresh_token", refreshToken);
            response.addCookie(refreshTokenCookie);
            
            AuthDto.UserInfo userInfo = new AuthDto.UserInfo(
                    account.getId(),
                    account.getEmail(),
                    account.getNickname(),
                    user != null ? user.getName() : null,
                    user != null ? user.getBirthDate() : null,
                    account.getMileage()
            );
            
            AuthDto.LoginResponse loginResponse = new AuthDto.LoginResponse(
                    accessToken, 
                    jwtProperties.getAccessTokenExpiration(), 
                    userInfo
            );
            
            return ApiResponse.success("로그인이 성공적으로 완료되었습니다", loginResponse);
            
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new InvalidPasswordException("이메일 또는 비밀번호가 올바르지 않습니다");
        }
    }
    
    public ApiResponse<AuthDto.TokenResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = getRefreshTokenFromCookie(request);
        if (refreshToken == null) {
            throw new InvalidTokenException("리프레시 토큰이 없습니다");
        }
        
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다");
        }
        
        String accountId = jwtUtil.extractAccountId(refreshToken);
        String storedRefreshToken = redisTokenService.getRefreshToken(accountId);
        
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다");
        }
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다"));
        
        String newAccessToken = jwtUtil.generateAccessToken(account.getId(), account.getEmail());
        
        AuthDto.TokenResponse tokenResponse = new AuthDto.TokenResponse(
                newAccessToken, 
                jwtProperties.getAccessTokenExpiration()
        );
        
        return ApiResponse.success("토큰이 성공적으로 갱신되었습니다", tokenResponse);
    }
    
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Account account) {
            
            String accessToken = getAccessTokenFromRequest(request);
            if (accessToken != null) {
                redisTokenService.deleteRefreshToken(account.getId());
                
                long remainingTime = jwtUtil.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
                if (remainingTime > 0) {
                    redisTokenService.blacklistAccessToken(accessToken, remainingTime);
                }
            }
            
            Cookie refreshTokenCookie = createRefreshTokenCookie("refresh_token", "");
            refreshTokenCookie.setMaxAge(0);
            response.addCookie(refreshTokenCookie);
        }
        
        return ApiResponse.success("로그아웃이 완료되었습니다", null);
    }
    
    public ApiResponse<AuthDto.UserInfo> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Account account)) {
            throw new InvalidTokenException("인증되지 않은 사용자입니다");
        }
        
        AuthDto.UserInfo userInfo = new AuthDto.UserInfo(
                account.getId(),
                account.getEmail(),
                account.getNickname(),
                account.getUser() != null ? account.getUser().getName() : null,
                account.getUser() != null ? account.getUser().getBirthDate() : null,
                account.getMileage()
        );
        
        return ApiResponse.success("사용자 정보를 성공적으로 조회했습니다", userInfo);
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