package com.ureka.team3.utong_backend.auth.controller;

import com.ureka.team3.utong_backend.auth.dto.MailSettingDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.MailSettingService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/mail-settings")
@RequiredArgsConstructor
public class MailSettingController {

    private final MailSettingService mailSettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<MailSettingDto.MailSettingResponse>> getMailSetting(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(mailSettingService.getMailSetting(account.getId()));
    }

    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<MailSettingDto.MailSettingResponse>> toggleMailSetting(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(mailSettingService.toggleMailSetting(account.getId()));
    }
}