package com.ureka.team3.utong_backend.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class MailSettingDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MailSettingResponse {
        private Boolean isMail;
        private String message;
    }
}