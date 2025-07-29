package com.ureka.team3.utong_backend.gift.config;

import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.common.enums.GroupCode;
import com.ureka.team3.utong_backend.common.repository.CodeRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Getter
public class GifticonPolicy {

    private final CodeRepository codeRepository;
    private List<Code> gifticonStatusList;

    @PostConstruct
    public void init() {
        gifticonStatusList = codeRepository.findByGroupCode(GroupCode.AVAILABILITY.getCode());
    }

    /**
     * 코드명으로 Code 객체 조회
     */
    public Code getStatusCodeByName(String codeName) {
        return gifticonStatusList.stream()
                .filter(code -> code.getCodeName().equals(codeName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 코드명이 존재하지 않습니다: " + codeName));
    }

    /**
     * 코드 값으로 Code 객체 조회
     */
    public Code getStatusCodeByCode(String code) {
        return gifticonStatusList.stream()
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("해당 코드값이 존재하지 않습니다: " + code));
    }
}
