package com.ureka.team3.utong_backend.toss.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.mypage.dto.*;
import com.ureka.team3.utong_backend.mypage.entity.PointChargeHistory;
import com.ureka.team3.utong_backend.mypage.repository.PointChargeHistoryRepository;
import com.ureka.team3.utong_backend.toss.dto.TossApproveResponse;
import com.ureka.team3.utong_backend.toss.dto.TossPaymentConfirmRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class TossPaymentServiceImpl implements TossPaymentService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final PointChargeHistoryRepository pointChargeHistoryRepository;
    private final AccountRepository accountRepository;

    private static final double FEE_RATE = 0.025;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Override
    public PointChargeResponseDto confirmAndCharge(Account account, TossPaymentConfirmRequestDto requestDto) {
        // 1. 토스 결제 승인 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(tossSecretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", requestDto.getPaymentKey());
        body.put("orderId", requestDto.getOrderId());
        body.put("amount", requestDto.getAmount());

        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(body, headers);

        ResponseEntity<TossApproveResponse> response = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/confirm",
                httpRequest,
                TossApproveResponse.class
        );


        System.out.println(" Toss 응답 원문: " + response);
        System.out.println(" Toss 응답 body: " + response.getBody());
        System.out.println(" Toss amount: " + response.getBody().getTotalAmount());

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            throw new RuntimeException("토스 결제 승인 실패");
        }

        TossApproveResponse approve = response.getBody();

        // 2. 결제 성공 → 포인트 충전
        Long charged = approve.getTotalAmount();
        Long fee = Math.round(charged * FEE_RATE);
        Long finalAmount = charged - fee;

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

        return PointChargeResponseDto.builder()
                .chargedAmount(charged)
                .feeAmount(fee)
                .finalAmount(finalAmount)
                .updatedMileage(account.getMileage())
                .build();
    }
}
