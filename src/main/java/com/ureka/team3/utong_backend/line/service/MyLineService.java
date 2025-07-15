package com.ureka.team3.utong_backend.line.service;

import com.ureka.team3.utong_backend.line.dto.MyLineResponseDto;

import java.util.List;

public interface MyLineService {
    List<MyLineResponseDto> getMyLines(String accountId, String defaultLineId);
    void setDefaultLine(String accountId, String lineId);
}
