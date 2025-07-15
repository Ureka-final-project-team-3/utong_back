package com.ureka.team3.utong_backend.line.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class MyLineResponseDto {
    private String lineId;
    private String phoneNumber;
    private boolean isDefault;
}
