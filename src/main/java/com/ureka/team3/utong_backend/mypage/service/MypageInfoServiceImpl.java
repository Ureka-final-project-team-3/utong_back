package com.ureka.team3.utong_backend.mypage.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.entity.User;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.line.repository.LineDataRepository;
import com.ureka.team3.utong_backend.line.repository.LineRepository;
import com.ureka.team3.utong_backend.mypage.dto.MyInfoResponseDto;
import com.ureka.team3.utong_backend.common.exception.business.LineNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.AccountNotFoundException;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class MypageInfoServiceImpl implements MypageInfoService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final LineRepository lineRepository;
    private final LineDataRepository lineDataRepository;

    @Override
    public MyInfoResponseDto getMyInfo(String accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(UserNotFoundException::new);

        // line & line_data 정보 가져오기(보유 데이터 조회용)
        Line line = lineRepository.findByUserId(user.getId())
                .orElseThrow(LineNotFoundException::new);


        LineData lineData = lineDataRepository.findTopByPhoneNumberOrderByCreatedAtDesc(line.getPhoneNumber())
                .orElse(null); // 없을 수도 있으니까

        return MyInfoResponseDto.builder()
                .name(user.getName())
                .email(account.getEmail())
                .mileage(account.getMileage())
                .phoneNumber(line.getPhoneNumber())
                .remainingData(lineData != null ? lineData.getRemining() : 0L)
                .build();
    }

//        return new MyInfoResponseDto(
//                user.getName(),
//                account.getEmail(),
//                account.getMileage()
//        );
//    }
}
