package com.ureka.team3.utong_backend.coupon.config;


import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.common.enums.GroupCode;
import com.ureka.team3.utong_backend.common.repository.CodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CouponPolicyTest {

    private CodeRepository codeRepository;
    private CouponPolicy couponPolicy;

    @BeforeEach
    void setUp() {
        codeRepository = mock(CodeRepository.class);

        List<Code> mockCouponStatusList = List.of(
                new Code("AVAIL", "001", "AVAILABLE", "사용 가능", 1),
                new Code("AVAIL", "002", "USED", "사용됨", 2)
        );

        when(codeRepository.findByGroupCode(GroupCode.AVAILABILITY.getCode()))
                .thenReturn(mockCouponStatusList);

        couponPolicy = new CouponPolicy(codeRepository);
        couponPolicy.init(); // 수동 초기화
    }

    @Test
    void 코드값으로_간단한_코드명을_정상적으로_조회할_수_있다() {
        String codeNameBrief = couponPolicy.getCodeNameBriefByCode("002");
        assertThat(codeNameBrief).isEqualTo("사용됨");
    }

    @Test
    void 존재하지_않는_코드값이면_알_수_없음_반환해야_한다() {
        String codeNameBrief = couponPolicy.getCodeNameBriefByCode("999");
        assertThat(codeNameBrief).isEqualTo("알 수 없음");
    }

    @Test
    void 코드리스트가_정상적으로_초기화되어야_한다() {
        assertThat(couponPolicy.getCouponStatusList()).hasSize(2);
        verify(codeRepository).findByGroupCode(GroupCode.AVAILABILITY.getCode());
    }
}
