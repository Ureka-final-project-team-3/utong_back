package com.ureka.team3.utong_backend.user.service;

import com.ureka.team3.utong_backend.coupon.repository.MyCouponRepository;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.line.service.LineService;
import org.springframework.stereotype.Service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.user.entity.User;
import com.ureka.team3.utong_backend.user.repository.UserRepository;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.user.dto.MyInfoResponseDto;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final LineService lineService;
    private final TradeCalculator tradeCalculator;
    private final MyCouponRepository myCouponRepository;

//    @Override   // todo : 메서드 리팩터링 : 책임과 역할에 맞게 메서드 분리 ( 클래스도 분리할거 있는지 고민해보길)
//    public MyInfoResponseDto getMyInfo(Account account) {   // -> 리팩터링 필요
//        User user = userRepository.findByAccountId(account.getId())
//                .orElseThrow(UserNotFoundException::new);
//
//        // line & line_data 정보 가져오기(보유 데이터 조회용)
////        Line line = lineRepository.findByUserId(user.getId())
////                .orElseThrow(LineNotFoundException::new);
//        // defaultline ID 로 Line 조회
//        String defaultLineId = account.getDefaultLine();
//
//        Line defaultLine = null;
//        if(defaultLineId !=null){
//            defaultLine = lineService.findById(defaultLineId);
//        }
//        String phoneNumber = null;
//        Long remaining = 0L;
//        Long canSale = 0L;
//        Long totalCouponAmount = myCouponRepository.sumUsedDataCouponAmountByUserId(user.getId());
//        String dataCode = null;
//        if(defaultLine !=null){
//            LineData lineData = lineService.getLineDataByLineAndDate(defaultLine, LocalDate.now());
//            phoneNumber = defaultLine.getPhoneNumber();
//            remaining = lineData.getRemaining()+lineData.getPurchased() + totalCouponAmount;
//            dataCode = lineData.getDataCode();
//            canSale = tradeCalculator.calculateCanSellAmount(lineData.getRemaining(), defaultLine.getPlan().canSell(), lineData.getSell());
//            canSale = Math.min(remaining,canSale);
//        }
//
//        // 해당 Line 의 최신 LineData 조회
//
////        LineData lineData = lineDataRepository.findTopByLineOrderByMonthDesc(line)
////                .orElse(null);
//
//
//        return MyInfoResponseDto.builder()
//                .name(user.getName())
//                .email(account.getEmail())
//                .mileage(account.getMileage())
//                .phoneNumber(phoneNumber)
//                .remainingData(remaining)
//                .dataCode(dataCode)
//                .canSale(canSale)
//                .build();
//    }

    @Override
    public MyInfoResponseDto getMyInfo(Account account) {
        User user = getUserByAccount(account);
        Line defaultLine = getDefaultLine(account.getDefaultLine());

        String phoneNumber = null;
        Long remaining = 0L;
        Long canSale = 0L;
        String dataCode = null;

        if ( defaultLine != null ) {
            phoneNumber = defaultLine.getPhoneNumber();
            LineData lineData = lineService.getLineDataByLineAndDate(defaultLine, LocalDate.now());
            Long couponAmount = getCouponAmount(user.getId());
            remaining = calculateTotalRemaining(lineData, couponAmount);
            canSale = calculateCanSellAmount(lineData, defaultLine, remaining);
            dataCode = lineData.getDataCode();
        }

        return MyInfoResponseDto.builder()
                .name(user.getName())
                .email(account.getEmail())
                .mileage(account.getMileage())
                .phoneNumber(phoneNumber)
                .remainingData(remaining)
                .dataCode(dataCode)
                .canSale(canSale)
                .build();

    }

    // Account 객체 User 엔티티 조회
    private User getUserByAccount(Account account) {
        return userRepository.findByAccountId(account.getId()).orElseThrow(UserNotFoundException::new);
    }

    // account에 등록된 기본 회선 ID 로 Line 객체 조회
    private Line getDefaultLine(String defaultLineId) {
        return defaultLineId != null ? lineService.findById(defaultLineId) : null;
    }

    // 데이터 쿠폰 총 데이터량 조회
    private Long getCouponAmount(String userId) {
        Long couponAmount = myCouponRepository.sumUsedDataCouponAmountByUserId(userId);
        return couponAmount != null ? couponAmount : 0L;
    }

    // 현재 보유 데이터량 계산
    private Long calculateTotalRemaining(LineData data, Long couponAmount) {
        return data.getRemaining() + data.getPurchased() + couponAmount;
    }

    // 판매 가능 데이터량 계산
    private Long calculateCanSellAmount(LineData data, Line line, Long remaining) {
        Long canSell = tradeCalculator.calculateCanSellAmount(
                data.getRemaining(),
                line.getPlan().getData(),
                data.getSell()
        );

        return Math.min(remaining, canSell);
    }



}
