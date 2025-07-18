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
import com.ureka.team3.utong_backend.point.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.point.entity.PointChargeHistory;
import com.ureka.team3.utong_backend.point.repository.PointChargeHistoryRepository;
import com.ureka.team3.utong_backend.payment.dto.PaymentApproveResponse;
import com.ureka.team3.utong_backend.payment.dto.PaymentConfirmRequestDto;
import lombok.RequiredArgsConstructor;
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
public class PaymentServiceImpl implements PaymentService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PointChargeHistoryRepository pointChargeHistoryRepository;
    private final AccountRepository accountRepository;
    private final MyCouponRepository myCouponRepository;
    private final TradeCalculator tradeCalculator;

//    private static final double FEE_RATE = 0.025;


    @Value("${toss.secret-key}")
    private String tossSecretKey;

//    // 토스 결제 시작
//    @Override
//    public TossPaymentResponseDto startPayment(Account account, TossPaymentRequestDto requestDto) {
//        // 1. orderId, 금액, 사용자 정보 등을 저장하거나 검증
//        String orderId = requestDto.getOrderId();
//        Long amount = requestDto.getAmount();
//
//        // 2. 결제창 URL 생성
//        // 실제로는 프론트에서 Checkout SDK가 처리, 백에서는 보통 orderId만 관리
//        String fakeCheckoutUrl = "https://pay.toss.im/checkout/" + orderId;
//
//        return TossPaymentResponseDto.builder()
//                .paymentKey(null) // 결제 후 승인 시에야 paymentKey가 생김
//                .checkoutUrl(fakeCheckoutUrl)
//                .build();
//    }


    @Override
    public PointChargeResponseDto confirmAndCharge(Account account, PaymentConfirmRequestDto requestDto) {
        System.out.println("[결제 승인 요청] paymentKey: " + requestDto.getPaymentKey());
        System.out.println("[결제 승인 요청] orderId: " + requestDto.getOrderId());
        System.out.println("[결제 승인 요청] amount: " + requestDto.getAmount());
        System.out.println("[결제 승인 요청] couponId: " + requestDto.getUserCouponId());

        // 1. 토스 결제 승인 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossSecretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", requestDto.getPaymentKey());
        body.put("orderId", requestDto.getOrderId());
        body.put("amount", requestDto.getAmount());

        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(body, headers);

        ResponseEntity<PaymentApproveResponse> response = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/confirm",
                httpRequest,
                PaymentApproveResponse.class
        );


        System.out.println(" Toss 응답 원문: " + response);
        System.out.println(" Toss 응답 body: " + response.getBody());
        System.out.println(" Toss amount: " + response.getBody().getTotalAmount());

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("토스 결제 승인 실패");
        }

        PaymentApproveResponse approve = response.getBody();
        Long charged = approve.getTotalAmount();

        // 2. 쿠폰 처리
        UserCoupon usedCoupon = null;
        boolean isFeeWaived = false;

        if (requestDto.getUserCouponId() != null) {
            usedCoupon = myCouponRepository.findById(requestDto.getUserCouponId())
                    .orElseThrow(CouponNotFoundException::new);

            if (!"002".equals(usedCoupon.getStatus())) {
                throw new InvalidCouponStatusException();
            }

            if (!"001".equals(usedCoupon.getCoupon().getCouponCode())) {
                throw new NotFeeWaiveCouponException();
            }

            if (usedCoupon.getExpiredAt() != null &&
                    usedCoupon.getExpiredAt().isBefore(LocalDateTime.now())) {
                throw new CouponExpiredException();
            }

            isFeeWaived = true;
        }

        // 3. 수수료 계싼
//        Long fee = isFeeWaived ? 0L : Math.round(charged * FEE_RATE);
        Long fee = isFeeWaived ? 0L : tradeCalculator.calculateSubtractFee(charged);
        Long finalAmount = charged - fee;

        // 4. 포인트 적립
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

        // 5. 쿠폰 상태 변경
        if (usedCoupon != null) {
            usedCoupon.markAsUsed();  // 사용 완료
            myCouponRepository.save(usedCoupon);
        }

        return PointChargeResponseDto.builder()
                .chargedAmount(charged)
                .feeAmount(fee)
                .finalAmount(finalAmount)
                .updatedMileage(account.getMileage())
                .build();
    }
}
