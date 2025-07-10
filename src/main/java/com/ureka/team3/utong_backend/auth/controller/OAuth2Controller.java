package com.ureka.team3.utong_backend.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ureka.team3.utong_backend.auth.service.OAuth2SuccessService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class OAuth2Controller {
    
    private final OAuth2SuccessService oAuth2SuccessService;
    
    public OAuth2Controller(OAuth2SuccessService oAuth2SuccessService) {
        this.oAuth2SuccessService = oAuth2SuccessService;
    }
    
    @GetMapping("/api/oauth2/success")
    public ResponseEntity<String> oauth2Success(@RequestParam(value = "accessToken", required = false) String accessToken, 
                                               @RequestParam(value = "tokenType", required = false) String tokenType, 
                                               @RequestParam(value = "expiresIn", required = false) String expiresIn, 
                                               HttpServletRequest request) {
        return ResponseEntity.ok(oAuth2SuccessService.processSuccess(accessToken, tokenType, expiresIn, request));
    }
    
    @GetMapping("/api/oauth2/test")
    public ResponseEntity<String> testOAuth() {
        return ResponseEntity.ok(oAuth2SuccessService.getTestMessage());
    }
    
    @GetMapping("/api/oauth2/debug")
    public ResponseEntity<String> debugOAuth(HttpServletRequest request) {
        return ResponseEntity.ok(oAuth2SuccessService.getDebugInfo(request));
    }
}