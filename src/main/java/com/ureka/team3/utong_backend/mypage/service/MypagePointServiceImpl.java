package com.ureka.team3.utong_backend.mypage.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeRequestDto;
import com.ureka.team3.utong_backend.mypage.dto.PointChargeResponseDto;
import com.ureka.team3.utong_backend.mypage.entity.PointChargeHistory;
import com.ureka.team3.utong_backend.mypage.repository.PointChargeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MypagePointServiceImpl implements MypagePointService {

    private final PointChargeHistoryRepository pointChargeHistoryRepository;
    private final AccountRepository accountRepository;

    private static final double FEE_RATE = 0.025; // 수수료율 2.5%

    @Transactional
    @Override
    public PointChargeResponseDto chargePoints(Account account, PointChargeRequestDto requestDto) {
        Long charged = requestDto.getChargedAmount();
        Long fee = Math.round(charged * FEE_RATE);
        Long finalAmount = charged - fee;

        // 1. Account 포인트 업데이트
        account.addMileage(finalAmount);
        accountRepository.save(account);

        // 2. 충전 기록 저장
        PointChargeHistory history = PointChargeHistory.builder()
                .id(UUID.randomUUID().toString())
                .account(account)
                .chargedAmount(charged)
                .feeAmount(fee)
                .finalAmount(finalAmount)
                .build();

        pointChargeHistoryRepository.save(history);

        // 3. 응답 반환
        return PointChargeResponseDto.builder()
                .chargedAmount(charged)
                .feeAmount(fee)
                .finalAmount(finalAmount)
                .updatedMileage(account.getMileage())
                .build();
    }


}
