package com.ureka.team3.utong_backend.datatrade.config;

import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.common.enums.GroupCode;
import com.ureka.team3.utong_backend.common.repository.CodeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DataTradePolicyTest {

    private CodeRepository codeRepository;
    private DataTradePolicy dataTradePolicy;

    @BeforeEach
    void setUp() {
        // Mock Repository
        codeRepository = mock(CodeRepository.class);

        // Stub: 거래상태 코드
        List<Code> mockTradeStatusCodes = List.of(
                new Code("010", "001", "COMPLETE", "전체 완료", 1),
                new Code("010", "002", "PART_COMPLETE", "부분완료", 2)
        );

        // Stub: 데이터유형 코드
        List<Code> mockDataTypeCodes = List.of(
                new Code("020", "001", "LTE", "LTE", 1),
                new Code("020", "002", "5G", "5G", 2)
        );

        // When
        when(codeRepository.findByGroupCode(GroupCode.TRADE_STATUS.getCode()))
                .thenReturn(mockTradeStatusCodes);
        when(codeRepository.findByGroupCode(GroupCode.DATA_TYPE.getCode()))
                .thenReturn(mockDataTypeCodes);

        // Initialize target class
        dataTradePolicy = new DataTradePolicy(codeRepository);
        dataTradePolicy.init(); // 수동 호출
    }

    @Test
    void 코드_초기화_후_목록이_정상적으로_세팅되어야_한다() {
        assertThat(dataTradePolicy.getTradeStatusCodeList()).hasSize(2);
        assertThat(dataTradePolicy.getDataTypeCodeList()).hasSize(2);

        assertThat(dataTradePolicy.getTradeStatusCodeList().get(0).getCodeName()).isEqualTo("COMPLETE");
        assertThat(dataTradePolicy.getDataTypeCodeList().get(1).getCodeName()).isEqualTo("5G");

        // Verify repository methods were called
        verify(codeRepository).findByGroupCode(GroupCode.TRADE_STATUS.getCode());
        verify(codeRepository).findByGroupCode(GroupCode.DATA_TYPE.getCode());
    }
}
