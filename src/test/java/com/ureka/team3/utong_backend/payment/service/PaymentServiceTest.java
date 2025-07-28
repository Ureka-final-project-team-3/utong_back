package com.ureka.team3.utong_backend.payment.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.exception.business.CouponExpiredException;
import com.ureka.team3.utong_backend.common.exception.business.CouponNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.InvalidCouponStatusException;
import com.ureka.team3.utong_backend.common.exception.business.NotFeeWaiveCouponException;
import com.ureka.team3.utong_backend.coupon.entity.Coupon;
import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.payment.dto.PaymentApproveResponse;
import com.ureka.team3.utong_backend.payment.dto.PaymentConfirmRequestDto;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.point.entity.PointChargeHistory;
import com.ureka.team3.utong_backend.point.repository.PointChargeHistoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock private AccountRepository accountRepository;
    @Mock private MyCouponRepository myCouponRepository;
    @Mock private PointChargeHistoryRepository pointChargeHistoryRepository;
    @Mock private TradeCalculator tradeCalculator;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void confirmAndCharge_successful_withoutCoupon() {
        // tossSecretKey 수동 주입
        ReflectionTestUtils.setField(paymentService, "tossSecretKey", "test-secret");

        // restTemplate도 수동으로 mock 객체 주입
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(paymentService, "restTemplate", mockRestTemplate);

        // given
        Account mockAccount = mock(Account.class);
        PaymentConfirmRequestDto requestDto = new PaymentConfirmRequestDto();
        requestDto.setOrderId("order123");
        requestDto.setAmount(10000L);
        requestDto.setPaymentKey("paykey123");
        requestDto.setUserCouponId(null); // 쿠폰 없음

        PaymentApproveResponse mockApproveResponse = new PaymentApproveResponse();
        ReflectionTestUtils.setField(mockApproveResponse, "totalAmount", 10000L);
        ResponseEntity<PaymentApproveResponse> mockResponse = ResponseEntity.ok(mockApproveResponse);

        when(mockRestTemplate.postForEntity(
                eq("https://api.tosspayments.com/v1/payments/confirm"),
                any(HttpEntity.class),
                eq(PaymentApproveResponse.class))
        ).thenReturn(mockResponse);

        when(tradeCalculator.calculateSubtractFee(10000L)).thenReturn(250L);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pointChargeHistoryRepository.save(any(PointChargeHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        PointChargeResponseDto result = paymentService.confirmAndCharge(mockAccount, requestDto);

        // then
        assertEquals(10000L, result.getChargedAmount());
        assertEquals(250L, result.getFeeAmount());
        assertEquals(9750L, result.getFinalAmount());
    }

    // 쿠폰이 존재하지 않을 경우
    @Test
    void confirmAndCharge_couponNotFound_shouldThrowException() {
        // given
        PaymentConfirmRequestDto requestDto = new PaymentConfirmRequestDto();
        requestDto.setOrderId("order123");
        requestDto.setAmount(10000L);
        requestDto.setPaymentKey("key123");
        requestDto.setUserCouponId("invalid-coupon");

        ReflectionTestUtils.setField(paymentService, "tossSecretKey", "test-secret");
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(paymentService, "restTemplate", mockRestTemplate);

        PaymentApproveResponse approve = new PaymentApproveResponse();
        ReflectionTestUtils.setField(approve, "totalAmount", 10000L);
        ResponseEntity<PaymentApproveResponse> response = ResponseEntity.ok(approve);

        when(mockRestTemplate.postForEntity(anyString(), any(), eq(PaymentApproveResponse.class))).thenReturn(response);
        when(myCouponRepository.findById("invalid-coupon")).thenReturn(Optional.empty());

        // then
        assertThrows(CouponNotFoundException.class, () -> paymentService.confirmAndCharge(new Account(), requestDto));
    }

    // 쿠폰 상태가 002 가 아닐 경우
    @Test
    void confirmAndCharge_couponInvalidStatus_shouldThrowException() {
        // given
        PaymentConfirmRequestDto requestDto = baseRequestWithCoupon();

        ReflectionTestUtils.setField(paymentService, "tossSecretKey", "test-secret");
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(paymentService, "restTemplate", mockRestTemplate);

        PaymentApproveResponse approve = new PaymentApproveResponse();
        ReflectionTestUtils.setField(approve, "totalAmount", 10000L);
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(PaymentApproveResponse.class)))
                .thenReturn(ResponseEntity.ok(approve));

        UserCoupon invalidStatusCoupon = mock(UserCoupon.class);
        when(invalidStatusCoupon.getStatus()).thenReturn("001");
        when(myCouponRepository.findById("coupon-id")).thenReturn(Optional.of(invalidStatusCoupon));

        // then
        assertThrows(InvalidCouponStatusException.class, () -> paymentService.confirmAndCharge(new Account(), requestDto));
    }

    // 쿠폰 코드가 001 이 아닐 경우
    @Test
    void confirmAndCharge_couponWrongType_shouldThrowException() {
        PaymentConfirmRequestDto requestDto = baseRequestWithCoupon();

        ReflectionTestUtils.setField(paymentService, "tossSecretKey", "test-secret");
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(paymentService, "restTemplate", mockRestTemplate);

        PaymentApproveResponse approve = new PaymentApproveResponse();
        ReflectionTestUtils.setField(approve, "totalAmount", 10000L);
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(PaymentApproveResponse.class)))
                .thenReturn(ResponseEntity.ok(approve));

        UserCoupon coupon = mock(UserCoupon.class);
        when(coupon.getStatus()).thenReturn("002");

        Coupon mockInnerCoupon = mock(Coupon.class);
        when(mockInnerCoupon.getCouponCode()).thenReturn("999"); // 잘못된 코드
        when(coupon.getCoupon()).thenReturn(mockInnerCoupon);    // 연결

        when(myCouponRepository.findById("coupon-id")).thenReturn(Optional.of(coupon));

        // then
        assertThrows(NotFeeWaiveCouponException.class, () -> paymentService.confirmAndCharge(new Account(), requestDto));
    }


    // 쿠폰 만료된 경우
    @Test
    void confirmAndCharge_couponExpired_shouldThrowException() {
        PaymentConfirmRequestDto requestDto = baseRequestWithCoupon();

        ReflectionTestUtils.setField(paymentService, "tossSecretKey", "test-secret");
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(paymentService, "restTemplate", mockRestTemplate);

        PaymentApproveResponse approve = new PaymentApproveResponse();
        ReflectionTestUtils.setField(approve, "totalAmount", 10000L);
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(PaymentApproveResponse.class)))
                .thenReturn(ResponseEntity.ok(approve));

        //  Coupon mock 추가
        Coupon mockCoupon = mock(Coupon.class);
        when(mockCoupon.getCouponCode()).thenReturn("001");

        //  UserCoupon mock 설정
        UserCoupon expiredCoupon = mock(UserCoupon.class);
        when(expiredCoupon.getStatus()).thenReturn("002");
        when(expiredCoupon.getCoupon()).thenReturn(mockCoupon); // 여기 추가!
        when(expiredCoupon.getExpiredAt()).thenReturn(LocalDateTime.now().minusDays(1));

        when(myCouponRepository.findById("coupon-id")).thenReturn(Optional.of(expiredCoupon));

        // then
        assertThrows(CouponExpiredException.class, () -> paymentService.confirmAndCharge(new Account(), requestDto));
    }


    // 응답 실패한 경우
    @Test
    void confirmAndCharge_tossResponseFailure_shouldThrowRuntimeException() {
        PaymentConfirmRequestDto requestDto = new PaymentConfirmRequestDto();
        requestDto.setOrderId("order123");
        requestDto.setAmount(10000L);
        requestDto.setPaymentKey("key123");

        ReflectionTestUtils.setField(paymentService, "tossSecretKey", "test-secret");
        RestTemplate mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(paymentService, "restTemplate", mockRestTemplate);

        ResponseEntity<PaymentApproveResponse> mockResponse = ResponseEntity.status(401).body(null);
        when(mockRestTemplate.postForEntity(anyString(), any(), eq(PaymentApproveResponse.class)))
                .thenReturn(mockResponse);

        assertThrows(RuntimeException.class, () -> paymentService.confirmAndCharge(new Account(), requestDto));
    }

    private PaymentConfirmRequestDto baseRequestWithCoupon() {
        PaymentConfirmRequestDto dto = new PaymentConfirmRequestDto();
        dto.setOrderId("order123");
        dto.setAmount(10000L);
        dto.setPaymentKey("key123");
        dto.setUserCouponId("coupon-id");
        return dto;
    }



}
