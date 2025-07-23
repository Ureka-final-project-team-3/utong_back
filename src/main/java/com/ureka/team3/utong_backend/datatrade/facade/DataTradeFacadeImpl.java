package com.ureka.team3.utong_backend.datatrade.facade;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.AccountService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.common.exception.business.AlreadyCancelOrderException;
import com.ureka.team3.utong_backend.common.exception.business.CannotCancelCompletedOrderException;
import com.ureka.team3.utong_backend.common.exception.business.InsufficientPointException;
import com.ureka.team3.utong_backend.common.exception.business.NotMyOrderException;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;
import com.ureka.team3.utong_backend.datatrade.handler.BuyMatchingResultHandler;
import com.ureka.team3.utong_backend.datatrade.handler.SaleMatchingResultHandler;
import com.ureka.team3.utong_backend.datatrade.processor.BuyMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.processor.SaleMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.service.query.TradeQueryService;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.BuyDataRequestService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import com.ureka.team3.utong_backend.datatrade.service.trade.sale.SaleDataRequestService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
import com.ureka.team3.utong_backend.datatrade.validator.TradeValidator;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.line.service.LineService;
import com.ureka.team3.utong_backend.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDate;

import static com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataTradeFacadeImpl implements DataTradeFacade {

    private final TradeValidator tradeValidator;
    private final PointService pointService;
    private final TradeCalculator tradeCalculator;
    private final BuyDataRequestService buyDataRequestService;
    private final BuyMatchingProcessor buyMatchingProcessor;
    private final BuyMatchingResultHandler buyMatchingResultHandler;
    private final SaleDataRequestService saleDataRequestService;
    private final LineService lineService;
    private final SaleMatchingProcessor saleMatchingProcessor;
    private final SaleMatchingResultHandler saleMatchingResultHandler;
    private final AccountService accountService;
    private final DataTradePolicy dataTradePolicy;
    private final TradeOrderQueueService tradeOrderQueueService;

    @Override
    @Transactional
    public ApiResponse requestBuy(Account account, DataTradeDto.BuyDataRequestDto dto) {
        account = accountService.findById(account.getId());
        // 1. 검증
        String defaultLineId = account.getDefaultLine();
        ApiResponse validationResult = tradeValidator.validatePurchase(defaultLineId);
        if (validationResult != null) return validationResult;
        // 2. 포인트 결제
        try {
            Long purchaseCoast = tradeCalculator.calculateTotalCoastForConsumer(dto);
            pointService.usePoint(account, purchaseCoast);
        } catch (InsufficientPointException e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
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
        lineService.saleData(savedOrder.getLineId(), dto.getDataAmount());  // 데이터 차감
        SaleMatchingResult saleMatchingResult = saleMatchingProcessor.handle(dto);
        return saleMatchingResultHandler.handle(saleMatchingResult, savedOrder);
    }

    @Override
    @Transactional
    public ApiResponse cancelBuyWaiting(Account account, DataTradeDto.CancelWaitingTradeRequestDto requestDto) {
        // 1. 본인의 orderId가 맞는지 확인
        account = accountService.findById(account.getId());
        BuyDataRequest buyOrderById = buyDataRequestService.findBuyOrderById(requestDto.getOrderId());
        if(!buyOrderById.isOwner(account.getId()))
            throw new NotMyOrderException();
        // 2. AllComplete가 아닌지 확인
        Code completeStatusCode = dataTradePolicy.getStatusCode(ALL_COMPLETE.name());
        if(buyOrderById.isStatus(completeStatusCode.getCode())){
            throw new CannotCancelCompletedOrderException();
        }

        Code cancelStatusCode = dataTradePolicy.getStatusCode(CANCEL.name());
        if(buyOrderById.isStatus(cancelStatusCode.getCode())){
            throw new AlreadyCancelOrderException();
        }
        // 3. mysql에서 004로 변경
        buyDataRequestService.changeStatus(buyOrderById,BuyOrderResult.CANCEL);
        // 4. 포인트 반환
        account.increasePoint(tradeCalculator.calculatePayPoint(buyOrderById.getRemaining(), buyOrderById.getPrice()));
        // 5. 레디스에서 제거
        tradeOrderQueueService.removeFromBuyQueue(buyOrderById);
        return ApiResponse.success("구매 대기 취소 완료",null);
    }

    @Override
    @Transactional
    public ApiResponse cancelSaleWaiting(Account account, DataTradeDto.CancelWaitingTradeRequestDto requestDto) {
        // 1. 본인의 orderId가 맞는지 확인
        SaleDataRequest saleOrderById = saleDataRequestService.findSaleOrderById(requestDto.getOrderId());
        validateSaleWaitingCancel(account, saleOrderById);
        // 3. mysql에서 004로 변경
        saleDataRequestService.changeStatus(saleOrderById, SaleOrderResult.CANCEL);
        // 4. 데이터 복구
        recoverData(saleOrderById);
        // 5. 레디스에서 제거
        tradeOrderQueueService.removeFromSaleQueue(saleOrderById);
        return ApiResponse.success("판매 대기 취소 완료",null);
    }

    private void validateSaleWaitingCancel(Account account, SaleDataRequest saleOrderById) {
        if(!saleOrderById.isOwner(account.getId()))
            throw new NotMyOrderException();
        // 2. AllComplete가 아닌지 확인
        Code statusCode = dataTradePolicy.getStatusCode(ALL_COMPLETE.name());
        if(saleOrderById.isStatus(statusCode.getCode())){
            throw new CannotCancelCompletedOrderException();
        }
        Code cancelStatusCode = dataTradePolicy.getStatusCode(CANCEL.name());
        if(saleOrderById.isStatus(cancelStatusCode.getCode())){
            throw new AlreadyCancelOrderException();
        }
    }

    private void recoverData(SaleDataRequest saleOrderById) {
        Line line = lineService.findById(saleOrderById.getLineId());
        LineData lineData = lineService.getLineDataByLineAndDate(line, LocalDate.now());
        lineData.recoverData(saleOrderById.getRemaining());
    }
}