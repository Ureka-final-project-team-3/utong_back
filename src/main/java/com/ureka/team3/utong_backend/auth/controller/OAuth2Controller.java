package com.ureka.team3.utong_backend.auth.controller;

import com.ureka.team3.utong_backend.auth.service.OAuth2SuccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OAuth2 인증 API", description = "OAuth2 로그인 성공 후 처리 및 디버깅용 API입니다.")
@RestController
@RequestMapping("/api/oauth")
public class OAuth2Controller {
    
    private final OAuth2SuccessService oAuth2SuccessService;
    
    public OAuth2Controller(OAuth2SuccessService oAuth2SuccessService) {
        this.oAuth2SuccessService = oAuth2SuccessService;
    }

    @Operation(
            summary = "OAuth2 로그인 성공 콜백",
            description = "소셜 로그인(OAuth2) 성공 후 redirect되는 엔드포인트입니다. 쿼리 파라미터로 accessToken, tokenType, expiresIn 정보를 전달받아 처리합니다."
    )
    @GetMapping("/success")
    public ResponseEntity<String> oauth2Success(@RequestParam(value = "accessToken", required = false) String accessToken, 
                                               @RequestParam(value = "tokenType", required = false) String tokenType, 
                                               @RequestParam(value = "expiresIn", required = false) String expiresIn, 
                                               HttpServletRequest request) {
        return ResponseEntity.ok(oAuth2SuccessService.processSuccess(accessToken, tokenType, expiresIn, request));
    }

    @Operation(summary = "OAuth2 테스트 메시지", description = "OAuth 연동 확인을 위한 테스트용 메시지를 반환합니다.")
    @GetMapping("/test")
    public ResponseEntity<String> testOAuth() {
        return ResponseEntity.ok(oAuth2SuccessService.getTestMessage());
    }

    @Operation(summary = "OAuth2 디버깅 정보", description = "현재 요청에 대한 디버깅 정보를 반환합니다.")
    @GetMapping("/debug")
    public ResponseEntity<String> debugOAuth(HttpServletRequest request) {
        return ResponseEntity.ok(oAuth2SuccessService.getDebugInfo(request));
    }
}