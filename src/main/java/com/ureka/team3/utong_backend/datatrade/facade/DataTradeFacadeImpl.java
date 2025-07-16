package com.ureka.team3.utong_backend.datatrade.facade;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.InsufficientPointException;
import com.ureka.team3.utong_backend.datatrade.dto.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.handler.BuyMatchingResultHandler;
import com.ureka.team3.utong_backend.datatrade.handler.SaleMatchingResultHandler;
import com.ureka.team3.utong_backend.datatrade.processor.BuyMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.processor.SaleMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.service.BuyDataRequestService;
import com.ureka.team3.utong_backend.datatrade.service.SaleDataRequestService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
import com.ureka.team3.utong_backend.datatrade.validator.TradeValidator;
import com.ureka.team3.utong_backend.line.service.LineService;
import com.ureka.team3.utong_backend.mypage.service.MypagePointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataTradeFacadeImpl implements DataTradeFacade {

    private final TradeValidator tradeValidator;
    private final MypagePointService pointService;
    private final TradeCalculator tradeCalculator;
    private final BuyDataRequestService buyDataRequestService;
    private final BuyMatchingProcessor buyMatchingProcessor;
    private final BuyMatchingResultHandler buyMatchingResultHandler;
    private final SaleDataRequestService saleDataRequestService;
    private final LineService lineService;
    private final SaleMatchingProcessor saleMatchingProcessor;
    private final SaleMatchingResultHandler saleMatchingResultHandler;

    @Override
    @Transactional
    public ApiResponse requestBuy(Account account, DataTradeDto.BuyDataRequestDto dto) {
        // 1. 검증
        String defaultLineId = account.getDefaultLine();
        ApiResponse validationResult = tradeValidator.validatePurchase(defaultLineId);
        if (validationResult != null) return validationResult;
        // 2. 포인트 결제
        try {
            Long purchaseCoast = tradeCalculator.calculateTotalCoastForConsumer(dto);
            pointService.usePoint(account, purchaseCoast);
        } catch (InsufficientPointException e) {
            return TradeResponseFactory.insufficientPoint();
        }
        // 3. DB에 저장
        BuyDataRequest saved = buyDataRequestService.save(account, dto);
        // 4. 판매 주문과 매칭
        BuyMatchingResult buyMatchingResult = buyMatchingProcessor.handle(dto);
        // 5. 매칭 결과에 따른 분기
        return buyMatchingResultHandler.handle(buyMatchingResult, saved);
    }

    @Override
    @Transactional
    public ApiResponse requestSale(Account account, DataTradeDto.SaleDataRequestDto dto) {
        // 1. 기본 회선 조회
        String defaultLineId = account.getDefaultLine();
        ApiResponse validationResult = tradeValidator.validateSale(defaultLineId, dto);
        if (validationResult != null) return validationResult;

        SaleDataRequest savedOrder = saleDataRequestService.save(account, dto);
        lineService.saleData(savedOrder.getLineId(), dto.getDataAmount());
        SaleMatchingResult saleMatchingResult = saleMatchingProcessor.handle(dto);
        return saleMatchingResultHandler.handle(saleMatchingResult, savedOrder);
    }
}