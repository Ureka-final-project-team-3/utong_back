package com.ureka.team3.utong_backend.auth.service;

import org.springframework.stereotype.Service;

import com.ureka.team3.utong_backend.auth.util.oauth.OAuth2HtmlGenerator;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class OAuth2SuccessService {
    
    private final OAuth2HtmlGenerator htmlGenerator;
    
    public OAuth2SuccessService(OAuth2HtmlGenerator htmlGenerator) {
        this.htmlGenerator = htmlGenerator;
    }
    
    public String processSuccess(String accessToken, String tokenType, String expiresIn, HttpServletRequest request) {
        return htmlGenerator.generateSuccessHtml(accessToken, tokenType, expiresIn);
    }
    
    public String getTestMessage() {
        return "OAuth2 정상작동 /oauth2/authorization/google ";
    }
    
    public String getDebugInfo(HttpServletRequest request) {
        return "url: " + request.getRequestURL() + "?" + request.getQueryString();
    }
}