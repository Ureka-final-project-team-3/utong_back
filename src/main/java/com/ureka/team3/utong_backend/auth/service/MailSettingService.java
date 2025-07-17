package com.ureka.team3.utong_backend.auth.service;

import com.ureka.team3.utong_backend.auth.dto.MailSettingDto;
import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MailSettingService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public ApiResponse<MailSettingDto.MailSettingResponse> getMailSetting(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        MailSettingDto.MailSettingResponse response = MailSettingDto.MailSettingResponse.builder()
                .isMail(account.getIsMail())
                .message("메일 설정을 조회했습니다")
                .build();

        return ApiResponse.success("메일 설정 조회 성공", response);
    }

    @Transactional
    public ApiResponse<MailSettingDto.MailSettingResponse> toggleMailSetting(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        Boolean newSetting = !account.getIsMail();
        account.updateMailSetting(newSetting);
        accountRepository.save(account);

        String statusMessage = newSetting ? "메일 수신이 활성화되었습니다" : "메일 수신이 비활성화되었습니다";

        MailSettingDto.MailSettingResponse response = MailSettingDto.MailSettingResponse.builder()
                .isMail(account.getIsMail())
                .message(statusMessage)
                .build();

        return ApiResponse.success("메일 설정 변경 완료", response);
    }
}