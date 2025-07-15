package com.ureka.team3.utong_backend.line.service;

import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.LineRepository;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;
import com.ureka.team3.utong_backend.common.exception.business.LineNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.line.dto.MyLineResponseDto;
import com.ureka.team3.utong_backend.line.entity.Line;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class MyLineServiceTest {

    @InjectMocks
    private MyLineServiceImpl mypageLineService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LineRepository lineRepository;

    @Mock
    private AccountRepository accountRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //  회선 조회 테스트
    @Test
    void getMyLines_정상적으로_회선목록과_기본여부조회() {
        // given
        String accountId = "acc123";
        String defaultLineId = "line1";
        User user = User.builder().id("user123").build();

        Line line1 = Line.builder().id("line1").phoneNumber("010-1111-1111").user(user).build();
        Line line2 = Line.builder().id("line2").phoneNumber("010-2222-2222").user(user).build();

        when(userRepository.findByAccountId(accountId)).thenReturn(Optional.of(user));
        when(lineRepository.findAllByUserId(user.getId())).thenReturn(Arrays.asList(line1, line2));

        // when
        List<MyLineResponseDto> result = mypageLineService.getMyLines(accountId, defaultLineId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getLineId()).isEqualTo("line1");
        assertThat(result.get(0).isDefault()).isTrue();
        assertThat(result.get(1).isDefault()).isFalse();
    }

    // 회선 설정 테스트 - 정상
    @Test
    void setDefaultLine_정상적으로_기본회선_설정됨() {
        // given
        String accountId = "acc123";
        String lineId = "line1";
        User user = User.builder().id("user123").build();

        when(userRepository.findByAccountId(accountId)).thenReturn(Optional.of(user));
        when(lineRepository.existsByIdAndUserId(lineId, user.getId())).thenReturn(true);

        // when
        mypageLineService.setDefaultLine(accountId, lineId);

        // then
        verify(accountRepository, times(1)).updateDefaultLine(accountId, lineId);
    }

    //  예외: 유저가 없음
    @Test
    void setDefaultLine_유저없으면_예외발생() {
        // given
        String accountId = "acc404";
        when(userRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> mypageLineService.setDefaultLine(accountId, "lineX"))
                .isInstanceOf(UserNotFoundException.class);
    }

    // 예외: 회선이 유저꺼가 아님
    @Test
    void setDefaultLine_유저소유아닌_회선이면_예외발생() {
        // given
        String accountId = "acc123";
        String lineId = "line999";
        User user = User.builder().id("user123").build();

        when(userRepository.findByAccountId(accountId)).thenReturn(Optional.of(user));
        when(lineRepository.existsByIdAndUserId(lineId, user.getId())).thenReturn(false);

        // expect
        assertThatThrownBy(() -> mypageLineService.setDefaultLine(accountId, lineId))
                .isInstanceOf(LineNotFoundException.class);
    }
}
