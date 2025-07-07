package com.ureka.team3.utong_backend.auth.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.ureka.team3.utong_backend.auth.service.OAuth2SuccessService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class OAuth2Controller {
    
    private final OAuth2SuccessService oAuth2SuccessService;
    
    public OAuth2Controller(OAuth2SuccessService oAuth2SuccessService) {
        this.oAuth2SuccessService = oAuth2SuccessService;
    }
    
    @GetMapping("/oauth2/success")
    public ResponseEntity<String> oauth2Success(@RequestParam(value = "accessToken", required = false) String accessToken, @RequestParam(value = "tokenType", required = false) String tokenType, @RequestParam(value = "expiresIn", required = false) String expiresIn, HttpServletRequest request) {
        return oAuth2SuccessService.processSuccess(accessToken, tokenType, expiresIn, request);
    }
    
    @GetMapping("/oauth2/test")
    @ResponseBody
    public String testOAuth() {
        return "OAuth2 설정이 활성화되어 있습니다. /oauth2/authorization/google 로 접속해보세요.";
    }
    
    @GetMapping("/oauth2/debug")
    @ResponseBody
    public String debugOAuth(HttpServletRequest request) {
        return "OAuth2 디버그: " + request.getRequestURL() + "?" + request.getQueryString();
    }
}