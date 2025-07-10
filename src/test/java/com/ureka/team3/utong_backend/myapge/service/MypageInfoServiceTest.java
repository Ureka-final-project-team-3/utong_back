package com.ureka.team3.utong_backend.myapge.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.entity.Line;
import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.LineRepository;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.line.repository.LineDataRepository;
import com.ureka.team3.utong_backend.mypage.dto.MyInfoResponseDto;
import com.ureka.team3.utong_backend.mypage.service.MypageInfoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MypageInfoServiceTest {

    @InjectMocks
    private MypageInfoServiceImpl mypageInfoService;

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;
    @Mock private LineRepository lineRepository;
    @Mock
    private LineDataRepository lineDataRepository;

    @Test
    void getMyInfo_성공() {
        String accountId = "test-id";

        Account account = Account.builder().id(accountId).email("user@test.com").mileage(3000L).build();
        User user = User.builder().id("user-id").name("테스트유저").build();
        Line line = Line.builder().phoneNumber("010-0000-0000").build();
        LineData lineData = LineData.builder().remining(8000L).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(userRepository.findByAccountId(accountId)).thenReturn(Optional.of(user));
        when(lineRepository.findByUserId(user.getId())).thenReturn(Optional.of(line));
        when(lineDataRepository.findTopByPhoneNumberOrderByCreatedAtDesc(line.getPhoneNumber())).thenReturn(Optional.ofNullable(lineData));

        MyInfoResponseDto result = mypageInfoService.getMyInfo(accountId);

        assertEquals("테스트유저", result.getName());
        assertEquals("user@test.com", result.getEmail());
        assertEquals(3000L, result.getMileage());
        assertEquals("010-0000-0000", result.getPhoneNumber());
        assertEquals(8000L, result.getRemainingData());
    }
}
