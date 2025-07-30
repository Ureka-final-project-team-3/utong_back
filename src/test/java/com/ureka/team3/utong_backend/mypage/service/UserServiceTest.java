package com.ureka.team3.utong_backend.mypage.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.plan.entity.Plan;
import com.ureka.team3.utong_backend.user.dto.MyInfoResponseDto;
import com.ureka.team3.utong_backend.user.entity.User;
import com.ureka.team3.utong_backend.user.repository.UserRepository;
import com.ureka.team3.utong_backend.user.service.UserServiceImpl;
import com.ureka.team3.utong_backend.line.service.LineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock private UserRepository userRepository;
    @Mock private LineService lineService;
    @Mock private TradeCalculator tradeCalculator;
    @Mock private MyCouponRepository myCouponRepository;

    private Account account;
    private User user;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id("acc-1")
                .email("user@test.com")
                .mileage(3000L)
                .defaultLine("line-1")
                .build();

        user = User.builder()
                .id("user-1")
                .name("테스트유저")
                .build();
    }

    @Test
    void getMyInfo_정상_데이터있음() {
        // given
        Plan plan = Plan.builder()
                .id("plan-1")
                .name("테스트 요금제")
                .data(10000L)
                .availableTradeRate(50.0f) // 50% 판매 가능
                .build();

        Line line = Line.builder()
                .id("line-1")
                .phoneNumber("010-0000-0000")
                .plan(plan)
                .build();

        LineData lineData = LineData.builder()
                .remaining(8000L)
                .purchased(1000L)
                .sell(1000L)
                .dataCode("LTE")
                .build();

        when(userRepository.findByAccountId("acc-1")).thenReturn(Optional.of(user));
        when(lineService.findById("line-1")).thenReturn(line);
        when(lineService.getLineDataByLineAndDate(line, LocalDate.now())).thenReturn(lineData);
        when(myCouponRepository.sumUsedDataCouponAmountByUserId("user-1")).thenReturn(500L);
        when(tradeCalculator.calculateCanSellAmount(8000L, 5000L, 1000L)).thenReturn(4000L);

        // when
        MyInfoResponseDto result = userService.getMyInfo(account);

        // then
        assertEquals("테스트유저", result.getName());
        assertEquals("user@test.com", result.getEmail());
        assertEquals(3000L, result.getMileage());
        assertEquals("010-0000-0000", result.getPhoneNumber());
        assertEquals(9500L, result.getRemainingData()); // 8000+1000+500
        assertEquals("LTE", result.getDataCode());
        assertEquals(4000L, result.getCanSale());
    }

    @Test
    void getMyInfo_기본회선없음() {
        // given
        Account accNoLine = Account.builder()
                .id("acc-2")
                .email("nolines@test.com")
                .mileage(1000L)
                .build();

        when(userRepository.findByAccountId("acc-2")).thenReturn(Optional.of(user));

        // when
        MyInfoResponseDto result = userService.getMyInfo(accNoLine);

        // then
        assertEquals("테스트유저", result.getName());
        assertEquals("nolines@test.com", result.getEmail());
        assertEquals(1000L, result.getMileage());
        assertEquals(0L, result.getRemainingData());
        assertEquals(0L, result.getCanSale());
    }

    @Test
    void getMyInfo_쿠폰데이터_null처리() {
        // given
        Plan plan = Plan.builder()
                .id("plan-2")
                .name("데이터 플랜")
                .data(5000L)
                .availableTradeRate(20.0f)
                .build();

        Line line = Line.builder()
                .id("line-2")
                .phoneNumber("010-1111-2222")
                .plan(plan)
                .build();

        LineData lineData = LineData.builder()
                .remaining(3000L)
                .purchased(0L)
                .sell(0L)
                .dataCode("5G")
                .build();

        when(userRepository.findByAccountId("acc-1")).thenReturn(Optional.of(user));
        when(lineService.findById("line-2")).thenReturn(line);
        when(lineService.getLineDataByLineAndDate(line, LocalDate.now())).thenReturn(lineData);
        when(myCouponRepository.sumUsedDataCouponAmountByUserId("user-1")).thenReturn(null);
        when(tradeCalculator.calculateCanSellAmount(3000L, 1000L, 0L)).thenReturn(1000L);

        Account newAccount = Account.builder()
                .id("acc-1")
                .email("user@test.com")
                .mileage(3000L)
                .defaultLine("line-2")
                .build();

        // when
        MyInfoResponseDto result = userService.getMyInfo(newAccount);

        // then
        assertEquals(3000L, result.getRemainingData());
        assertEquals(1000L, result.getCanSale());
    }
}