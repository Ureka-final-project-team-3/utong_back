package com.ureka.team3.utong_backend.payment.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.exception.business.CouponExpiredException;
import com.ureka.team3.utong_backend.common.exception.business.CouponNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.InvalidCouponStatusException;
import com.ureka.team3.utong_backend.common.exception.business.NotFeeWaiveCouponException;
import com.ureka.team3.utong_backend.coupon.entity.UserCoupon;
import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.payment.dto.PaymentApproveResponse;
import com.ureka.team3.utong_backend.payment.dto.PaymentConfirmRequestDto;
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.point.entity.PointChargeHistory;
import com.ureka.team3.utong_backend.point.repository.PointChargeHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PointChargeHistoryRepository pointChargeHistoryRepository;
    private final AccountRepository accountRepository;
    private final MyCouponRepository myCouponRepository;
    private final TradeCalculator tradeCalculator;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

//    @Override   // todo : 메서드 리팩터링 : 책임과 역할에 맞게 메서드 분리 ( 클래스도 분리할거 있는지 고민해보길)
//    public PointChargeResponseDto confirmAndCharge(Account account, PaymentConfirmRequestDto requestDto) {
//        System.out.println("[결제 승인 요청] paymentKey: " + requestDto.getPaymentKey());
//        System.out.println("[결제 승인 요청] orderId: " + requestDto.getOrderId());
//        System.out.println("[결제 승인 요청] amount: " + requestDto.getAmount());
//        System.out.println("[결제 승인 요청] couponId: " + requestDto.getUserCouponId());
//        // todo : 로그 수정
//
//        // 1. 토스 결제 승인 요청
//        HttpHeaders headers = new HttpHeaders();
//        headers.setBasicAuth(tossSecretKey, "");
//        headers.setContentType(MediaType.APPLICATION_JSON);
//
//        Map<String, Object> body = new HashMap<>();
//        body.put("paymentKey", requestDto.getPaymentKey());
//        body.put("orderId", requestDto.getOrderId());
//        body.put("amount", requestDto.getAmount());
//
//        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(body, headers);
//
//        ResponseEntity<PaymentApproveResponse> response = restTemplate.postForEntity(
//                "https://api.tosspayments.com/v1/payments/confirm", // todo : 환경 변수로 (도연이꺼 아님)
//                httpRequest,
//                PaymentApproveResponse.class
//        );
//
//
//        // Todo : log 수정
//        System.out.println(" Toss 응답 원문: " + response);
//        System.out.println(" Toss 응답 body: " + response.getBody());
//        System.out.println(" Toss amount: " + response.getBody().getTotalAmount());
//
//        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
//            throw new RuntimeException("토스 결제 승인 실패");
//        }
//
//        PaymentApproveResponse approve = response.getBody();
//        Long charged = approve.getTotalAmount();
//
//        // 2. 쿠폰 처리
//        UserCoupon usedCoupon = null;
//        boolean isFeeWaived = false;
//
//        if (requestDto.getUserCouponId() != null) {
//            usedCoupon = myCouponRepository.findById(requestDto.getUserCouponId())
//                    .orElseThrow(CouponNotFoundException::new);
//
//            if (!"002".equals(usedCoupon.getStatus())) {
//                throw new InvalidCouponStatusException();
//            }
//
//            if (!"001".equals(usedCoupon.getCoupon().getCouponCode())) {
//                throw new NotFeeWaiveCouponException();
//            }
//
//            if (usedCoupon.getExpiredAt() != null &&
//                    usedCoupon.getExpiredAt().isBefore(LocalDateTime.now())) {
//                throw new CouponExpiredException();
//            }
//
//            isFeeWaived = true;
//        }
//
//        // 3. 수수료 계싼
////        Long fee = isFeeWaived ? 0L : Math.round(charged * FEE_RATE);
//        Long fee = isFeeWaived ? 0L : tradeCalculator.calculateSubtractFee(charged);
//        Long finalAmount = charged - fee;
//
//        // 4. 포인트 적립
//        account.addMileage(finalAmount);
//        accountRepository.save(account);
//
//        PointChargeHistory history = PointChargeHistory.builder()
//                .id(UUID.randomUUID().toString())
//                .account(account)
//                .chargedAmount(charged)
//                .feeAmount(fee)
//                .finalAmount(finalAmount)
//                .build();
//
//        pointChargeHistoryRepository.save(history);
//
//        // 5. 쿠폰 상태 변경
//        if (usedCoupon != null) {
//            usedCoupon.markAsUsed();  // 사용 완료
//            myCouponRepository.save(usedCoupon);
//        }
//
//        return PointChargeResponseDto.builder()
//                .chargedAmount(charged)
//                .feeAmount(fee)
//                .finalAmount(finalAmount)
//                .updatedMileage(account.getMileage())
//                .build();
//    }
    @Override   // todo : 메서드 리팩터링 : 책임과 역할에 맞게 메서드 분리 ( 클래스도 분리할거 있는지 고민해보길)
    public PointChargeResponseDto confirmAndCharge(Account account, PaymentConfirmRequestDto requestDto) {
        logPayRequest(requestDto);

        // 1. 토스 결제 승인 요청
        PaymentApproveResponse approve = approvePayment(requestDto);
        Long charged = approve.getTotalAmount();

        // 2. 쿠폰 검증 및 처리
        boolean isFeeWaived = false;
        UserCoupon userCoupon = null;
        if(requestDto.getUserCouponId() != null) {
            userCoupon = validateCouponIfPresent(requestDto.getUserCouponId());
            isFeeWaived = true;
        }

        // 3. 수수료 계산
        Long fee = calculateFee(charged, isFeeWaived);
        Long finalAmount = charged - fee;

        // 4. 포인트 적립 및 내역 저장
        savePointCharge(account, charged, fee, finalAmount);

        // 5. 쿠폰 상태 업데이트
        markCouponAsUsedIfExists(userCoupon);

        return PointChargeResponseDto.builder()
                .chargedAmount(charged)
                .feeAmount(fee)
                .finalAmount(finalAmount)
                .updatedMileage(account.getMileage())
                .build();
    }

    // 결제 승인 요청에 포함된 정보
    private void logPayRequest(PaymentConfirmRequestDto requestDto) {
        log.info("[결제 승인 요청] patmentKey: {}", requestDto.getPaymentKey());
        log.info("[결제 승인 요청] paymentKey: {}", requestDto.getPaymentKey());
        log.info("[결제 승인 요청] amount: {}", requestDto.getAmount());
        log.info("[결제 승인 요청] couponId: {}", requestDto.getUserCouponId());
        // todo : 로그 수정 - 완료
    }

    // 결제 승인 요청 -> 결제 결과 반환
    private PaymentApproveResponse approvePayment(PaymentConfirmRequestDto requestDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossSecretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", requestDto.getPaymentKey());
        body.put("orderId", requestDto.getOrderId());
        body.put("amount", requestDto.getAmount());

        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(body, headers);

        ResponseEntity<PaymentApproveResponse> response = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/confirm", // todo : 환경 변수로 (도연이꺼 아님)
                httpRequest,
                PaymentApproveResponse.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("결제 승인 실패");
        }
        return response.getBody();
    }

    // 쿠폰 유효성 검증
    private UserCoupon validateCouponIfPresent(String userCouponId) {
        UserCoupon coupon = myCouponRepository.findById(userCouponId).orElseThrow(CouponNotFoundException::new);

        if ( !"002".equals(coupon.getStatus()) ) {
            throw new InvalidCouponStatusException();
        }

        if ( !"001".equals(coupon.getCoupon().getCouponCode())) {
            throw new NotFeeWaiveCouponException();
        }

        if ( coupon.getExpiredAt() != null && coupon.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new CouponExpiredException();
        }

        return coupon;

    }

    // 수수료 계산
    private Long calculateFee(Long charged, boolean isFeeWaived) {
        return isFeeWaived ? 0L : tradeCalculator.calculateSubtractFee(charged);
    }

    // 포인트 충전 내역 저장
    private void savePointCharge(Account account, Long charged, Long fee, Long finalAmount) {
        account.addMileage(finalAmount);
        accountRepository.save(account);

        PointChargeHistory history = PointChargeHistory.builder()
                .id(UUID.randomUUID().toString())
                .account(account)
                .chargedAmount(charged)
                .feeAmount(fee)
                .finalAmount(finalAmount)
                .build();
        pointChargeHistoryRepository.save(history);
    }

    // 쿠폰 존재할 경우 사용 처리 저장
    private void markCouponAsUsedIfExists(UserCoupon userCoupon) {
        if ( userCoupon != null) {
            userCoupon.markAsUsed();
            myCouponRepository.save(userCoupon);
        }
    }


}
