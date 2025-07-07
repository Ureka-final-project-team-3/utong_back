package com.ureka.team3.utong_backend.common.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ureka.team3.utong_backend.auth.dto.CustomOAuth2UserDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.RedisTokenService;
import com.ureka.team3.utong_backend.auth.util.JwtProperties;
import com.ureka.team3.utong_backend.auth.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtUtil jwtUtil;
    private final RedisTokenService redisTokenService;
    private final JwtProperties jwtProperties;
    
    public OAuth2SuccessHandler(JwtUtil jwtUtil, 
                               RedisTokenService redisTokenService,
                               JwtProperties jwtProperties) {
        this.jwtUtil = jwtUtil;
        this.redisTokenService = redisTokenService;
        this.jwtProperties = jwtProperties;
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                      Authentication authentication) throws IOException {
        
        
        if (response.isCommitted()) {
            return;
        }
        
        CustomOAuth2UserDto oAuth2User = (CustomOAuth2UserDto) authentication.getPrincipal();
        Account account = oAuth2User.getAccount();
        
        
        String accessToken = jwtUtil.generateAccessToken(account.getId(), account.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(account.getId());
        
        
        redisTokenService.saveRefreshToken(account.getId(), refreshToken);
        
        Cookie refreshTokenCookie = createRefreshTokenCookie(refreshToken);
        response.addCookie(refreshTokenCookie);
        
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:8080/oauth2/success")
                .queryParam("accessToken", accessToken)
                .queryParam("tokenType", "Bearer")
                .queryParam("expiresIn", jwtProperties.getAccessTokenExpiration())
                .build().toUriString();
        
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private Cookie createRefreshTokenCookie(String refreshToken) {
        Cookie cookie = new Cookie("refresh_token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtProperties.getRefreshTokenExpiration() / 1000));
        return cookie;
    }
}