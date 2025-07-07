package com.ureka.team3.utong_backend.auth.dto;


import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OAuth2UserInfoDto {
    private String providerId;
    private String provider;
    private String email;
    private String name;
    private String picture;
    
    public static OAuth2UserInfoDto of(String provider, Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> ofGoogle(attributes);
            case "kakao" -> ofKakao(attributes);
            case "naver" -> ofNaver(attributes);
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다: " + provider);
        };
    }
    
    private static OAuth2UserInfoDto ofGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfoDto.builder()
                .providerId(String.valueOf(attributes.get("sub")))
                .provider("google")
                .email(String.valueOf(attributes.get("email")))
                .name(String.valueOf(attributes.get("name")))
                .picture(String.valueOf(attributes.get("picture")))
                .build();
    }
    
    private static OAuth2UserInfoDto ofKakao(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        
        return OAuth2UserInfoDto.builder()
                .providerId(String.valueOf(attributes.get("id")))
                .provider("kakao")
                .email(String.valueOf(kakaoAccount.get("email")))
                .name(String.valueOf(profile.get("nickname")))
                .picture(String.valueOf(profile.get("profile_image_url")))
                .build();
    }
    
    private static OAuth2UserInfoDto ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        
        return OAuth2UserInfoDto.builder()
                .providerId(String.valueOf(response.get("id")))
                .provider("naver")
                .email(String.valueOf(response.get("email")))
                .name(String.valueOf(response.get("name")))
                .picture(String.valueOf(response.get("profile_image")))
                .build();
    }
}