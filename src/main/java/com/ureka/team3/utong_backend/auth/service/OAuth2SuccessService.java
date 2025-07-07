package com.ureka.team3.utong_backend.auth.service;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.ureka.team3.utong_backend.auth.util.oauth.OAuth2HtmlGenerator;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class OAuth2SuccessService {
    
    private final OAuth2HtmlGenerator htmlGenerator;
    
    public OAuth2SuccessService(OAuth2HtmlGenerator htmlGenerator) {
        this.htmlGenerator = htmlGenerator;
    }
    
    public ResponseEntity<String> processSuccess(String accessToken, String tokenType, String expiresIn, HttpServletRequest request) {
        System.out.println("✅ OAuth2 성공 - Token: " + (accessToken != null ? accessToken.substring(0, 50) + "..." : "null"));
        String html = htmlGenerator.generateSuccessHtml(accessToken, tokenType, expiresIn);
        return ResponseEntity.ok().header("Content-Type", "text/html; charset=UTF-8").body(html);
    }
}
