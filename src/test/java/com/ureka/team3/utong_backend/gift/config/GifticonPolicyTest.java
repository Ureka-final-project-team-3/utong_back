package com.ureka.team3.utong_backend.gift.config;

import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.common.enums.GroupCode;
import com.ureka.team3.utong_backend.common.repository.CodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GifticonPolicyTest {

    private CodeRepository codeRepository;
    private GifticonPolicy gifticonPolicy;

    @BeforeEach
    void setUp() {
        codeRepository = mock(CodeRepository.class);

        List<Code> mockGifticonStatusList = List.of(
                new Code("AVAIL", "001", "AVAILABLE", "사용 가능", 1),
                new Code("AVAIL", "002", "USED", "사용됨", 2),
                new Code("AVAIL", "003", "EXPIRED", "만료됨", 3)
        );

        when(codeRepository.findByGroupCode(GroupCode.AVAILABILITY.getCode()))
                .thenReturn(mockGifticonStatusList);

        gifticonPolicy = new GifticonPolicy(codeRepository);
        gifticonPolicy.init(); // 수동 초기화
    }

    @Test
    void 코드값으로_Code_객체를_정상적으로_조회할_수_있다() {
        Code code = gifticonPolicy.getStatusCodeByCode("002");
        assertThat(code.getCodeName()).isEqualTo("USED");
    }

    @Test
    void 코드명으로_Code_객체를_정상적으로_조회할_수_있다() {
        Code code = gifticonPolicy.getStatusCodeByName("EXPIRED");
        assertThat(code.getCode()).isEqualTo("003");
    }

    @Test
    void 존재하지_않는_코드값_조회시_예외가_발생해야_한다() {
        assertThatThrownBy(() -> gifticonPolicy.getStatusCodeByCode("999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 코드값이 존재하지 않습니다");
    }

    @Test
    void 존재하지_않는_코드명_조회시_예외가_발생해야_한다() {
        assertThatThrownBy(() -> gifticonPolicy.getStatusCodeByName("NOT_EXIST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 코드명이 존재하지 않습니다");
    }

    @Test
    void 코드리스트가_정상적으로_초기화되어야_한다() {
        assertThat(gifticonPolicy.getGifticonStatusList()).hasSize(3);
        verify(codeRepository).findByGroupCode(GroupCode.AVAILABILITY.getCode());
    }
}
