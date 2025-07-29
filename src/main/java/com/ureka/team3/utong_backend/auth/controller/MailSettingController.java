package com.ureka.team3.utong_backend.auth.controller;

import com.ureka.team3.utong_backend.auth.dto.MailSettingDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.MailSettingService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "메일 수신 설정 API", description = "사용자의 메일 수신 설정을 조회하고 변경할 수 있는 API입니다.")
@RestController
@RequestMapping("/api/auth/mail-settings")
@RequiredArgsConstructor
public class MailSettingController {

    private final MailSettingService mailSettingService;

    @Operation(summary = "메일 수신 설정 조회", description = "로그인된 사용자의 메일 수신 여부 설정을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<MailSettingDto.MailSettingResponse>> getMailSetting(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(mailSettingService.getMailSetting(account.getId()));
    }

    @Operation(summary = "메일 수신 설정 토글", description = "현재 메일 수신 설정을 ON/OFF로 전환합니다.")
    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<MailSettingDto.MailSettingResponse>> toggleMailSetting(
            @AuthenticationPrincipal Account account) {
        return ResponseEntity.ok(mailSettingService.toggleMailSetting(account.getId()));
    }
}