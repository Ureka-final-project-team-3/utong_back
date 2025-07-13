package com.ureka.team3.utong_backend.common.handler;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ureka.team3.utong_backend.auth.dto.CustomOAuth2UserDto;
import com.ureka.team3.utong_backend.auth.dto.OAuth2UserInfoDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;
import com.ureka.team3.utong_backend.auth.service.RedisTokenService;
import com.ureka.team3.utong_backend.auth.util.JwtProperties;
import com.ureka.team3.utong_backend.auth.util.JwtUtil;

import java.util.UUID;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final JwtProperties jwtProperties;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;
    
    public OAuth2SuccessHandler(JwtUtil jwtUtil, 
                               RedisTokenService redisTokenService,
                               JwtProperties jwtProperties,
                               AccountRepository accountRepository,
                               UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
        this.jwtProperties = jwtProperties;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
        try {
            Account account;
            
            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomOAuth2UserDto) {
                CustomOAuth2UserDto customUser = (CustomOAuth2UserDto) principal;
                account = customUser.getAccount();
                
            } else if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser) {
                org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser = 
                    (org.springframework.security.oauth2.core.oidc.user.OidcUser) principal;
                account = createOrUpdateAccountFromOidcUser(oidcUser);
                
            } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
                org.springframework.security.oauth2.core.user.OAuth2User oAuth2User = 
                    (org.springframework.security.oauth2.core.user.OAuth2User) principal;
                account = createOrUpdateAccountFromOAuth2User(oAuth2User);
                
            } else {
                throw new RuntimeException("지원하지 않는 사용자 타입: " + principal.getClass().getName());
            }
            
            String accessToken = jwtUtil.generateAccessToken(account.getId(), account.getEmail());
            String refreshToken = jwtUtil.generateRefreshToken(account.getId());
            
            redisTokenService.saveRefreshToken(account.getId(), refreshToken);
            
            Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
            response.addCookie(refreshTokenCookie);
            
            // 프론트엔드 URL로 리다이렉트
            String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .queryParam("accessToken", accessToken)
                    .queryParam("tokenType", "Bearer")
                    .queryParam("expiresIn", jwtProperties.getAccessTokenExpiration())
                    .queryParam("oauth", "success")
                    .build().toUriString();
            
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
            
        } catch (Exception e) {
            response.sendRedirect(frontendUrl + "?error=oauth_failed");
        }
    }
    
    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
        return cookie;
    }
    
    // 기존 createOrUpdateAccountFromOidcUser, createOrUpdateAccountFromOAuth2User 메서드들은 그대로 유지
    private Account createOrUpdateAccountFromOidcUser(org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
        String email = oidcUser.getEmail();
        String providerId = oidcUser.getSubject();
        String name = oidcUser.getFullName();
        
        Account existingAccount = accountRepository.findByProviderAndProviderId("google", providerId);
        if (existingAccount != null) {
            return existingAccount;
        }
        
        Account emailAccount = accountRepository.findByEmail(email).orElse(null);
        if (emailAccount != null && emailAccount.getProvider() == null) {
            Account linkedAccount = Account.builder()
                    .id(emailAccount.getId())
                    .email(emailAccount.getEmail())
                    .nickname(emailAccount.getNickname())
                    .password(emailAccount.getPassword())
                    .provider("google")
                    .providerId(providerId)
                    .mileage(emailAccount.getMileage())
                    .user(emailAccount.getUser())
                    .build();
            
            Account saved = accountRepository.save(linkedAccount);
            return saved;
        }
        
        String accountId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        
        Account account = Account.builder()
                .id(accountId)
                .email(email)
                .nickname(name != null ? name : email.split("@")[0])
                .provider("google")
                .providerId(providerId)
                .mileage(0L)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        
        User user = User.builder()
                .id(userId)
                .name(name)
                .birthDate(null) 
                .account(savedAccount)
                .build();
        
        userRepository.save(user);
        
        return savedAccount;
    }
    
    private Account createOrUpdateAccountFromOAuth2User(org.springframework.security.oauth2.core.user.OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String providerId = oAuth2User.getAttribute("sub");
        String name = oAuth2User.getAttribute("name");
        
        OAuth2UserInfoDto userInfo = OAuth2UserInfoDto.of("google", oAuth2User.getAttributes());
        
        Account existingAccount = accountRepository.findByProviderAndProviderId(userInfo.getProvider(), userInfo.getProviderId());
        if (existingAccount != null) {
            return existingAccount;
        }
        
        Account emailAccount = accountRepository.findByEmail(userInfo.getEmail()).orElse(null);
        if (emailAccount != null && emailAccount.getProvider() == null) {
            Account linkedAccount = Account.builder()
                    .id(emailAccount.getId())
                    .email(emailAccount.getEmail())
                    .nickname(emailAccount.getNickname())
                    .password(emailAccount.getPassword())
                    .provider(userInfo.getProvider())
                    .providerId(userInfo.getProviderId())
                    .mileage(emailAccount.getMileage())
                    .user(emailAccount.getUser())
                    .build();
            
            Account saved = accountRepository.save(linkedAccount);
            return saved;
        }
        
        String accountId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        
        Account account = Account.builder()
                .id(accountId)
                .email(userInfo.getEmail())
                .nickname(userInfo.getName())
                .provider(userInfo.getProvider())
                .providerId(userInfo.getProviderId())
                .mileage(0L)
                .build();
        
        Account savedAccount = accountRepository.save(account);
        
        User user = User.builder()
                .id(userId)
                .name(userInfo.getName())
                .birthDate(null) 
                .account(savedAccount)
                .build();
        
        userRepository.save(user);
        
        return savedAccount;
    }
}