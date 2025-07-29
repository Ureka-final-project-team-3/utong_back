package com.ureka.team3.utong_backend.coupon.config;

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
public class CouponPolicy {

    private final CodeRepository codeRepository;
    private List<Code> couponStatusList;

    @PostConstruct
    public void init() {
        couponStatusList = codeRepository.findByGroupCode(GroupCode.AVAILABILITY.getCode());
    }

    /**
     * code 번호로  Code 객체 조회
     */
    public String getCodeNameBriefByCode(String code) {
        return couponStatusList.stream()
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .map(Code::getCodeNameBrief)
                .orElse("알 수 없음");
    }
}
