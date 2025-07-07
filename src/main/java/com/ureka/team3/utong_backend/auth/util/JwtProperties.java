package com.ureka.team3.utong_backend.auth.util;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    private String secret = "mySecretKey1234567890123456789012345678901234567890";
    private Long accessTokenExpiration = 3600000L; // 1시간
    private Long refreshTokenExpiration = 604800000L; // 7일
}