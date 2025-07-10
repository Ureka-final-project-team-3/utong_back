package com.ureka.team3.utong_backend.myapge.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeRequestDto;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.mypage.entity.PointChargeHistory;
import com.ureka.team3.utong_backend.mypage.repository.PointChargeHistoryRepository;
import com.ureka.team3.utong_backend.mypage.service.MypagePointServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MypagePointServiceTest {

    @InjectMocks
    private MypagePointServiceImpl mypagePointService;

    @Mock
    private AccountRepository accountRepository;
    @Mock private PointChargeHistoryRepository pointChargeHistoryRepository;

    @Test
    void chargePoints_성공() {
        Account account = Account.builder().id("acc-id").mileage(10000L).build();
        PointChargeRequestDto requestDto = new PointChargeRequestDto();
        requestDto.setChargedAmount(20000L); // → 예상: 수수료 500, 실제 적립 19500

        PointChargeResponseDto result = mypagePointService.chargePoints(account, requestDto);

        assertEquals(20000L, result.getChargedAmount());
        assertEquals(500L, result.getFeeAmount());
        assertEquals(19500L, result.getFinalAmount());
        assertEquals(29500L, result.getUpdatedMileage());

        verify(accountRepository).save(account);
        verify(pointChargeHistoryRepository).save(any(PointChargeHistory.class));
    }
}

