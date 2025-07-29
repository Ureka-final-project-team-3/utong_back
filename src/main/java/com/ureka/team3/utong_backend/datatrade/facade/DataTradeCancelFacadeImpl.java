package com.ureka.team3.utong_backend.datatrade.facade;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.AccountService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.common.exception.business.AlreadyCancelOrderException;
import com.ureka.team3.utong_backend.common.exception.business.CannotCancelCompletedOrderException;
import com.ureka.team3.utong_backend.common.exception.business.NotMyOrderException;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.BuyDataRequestService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import com.ureka.team3.utong_backend.datatrade.service.trade.sale.SaleDataRequestService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.line.service.LineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult.ALL_COMPLETE;
import static com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult.CANCEL;

@Service
@RequiredArgsConstructor
public class DataTradeCancelFacadeImpl implements DataTradeCancelFacade{
    private final AccountService accountService;
    private final BuyDataRequestService buyDataRequestService;
    private final TradeCalculator tradeCalculator;
    private final TradeOrderQueueService tradeOrderQueueService;
    private final DataTradePolicy dataTradePolicy;
    private final SaleDataRequestService saleDataRequestService;
    private final LineService lineService;


    @Transactional
    @Override
    public ApiResponse cancelBuyWaiting(Account account, DataTradeDto.CancelWaitingTradeRequestDto requestDto) {
        // 1. 본인의 orderId가 맞는지 확인
        account = accountService.findById(account.getId());
        BuyDataRequest buyOrderById = buyDataRequestService.findBuyOrderById(requestDto.getOrderId());
        validateCancelBuyWaiting(account, buyOrderById);
        // 3. mysql에서 004로 변경
        buyDataRequestService.changeStatus(buyOrderById, BuyOrderResult.CANCEL);
        // 4. 포인트 반환
        account.increasePoint(tradeCalculator.calculatePayPoint(buyOrderById.getRemaining(), buyOrderById.getPrice()));
        // 5. 레디스에서 제거
        tradeOrderQueueService.removeFromBuyQueue(buyOrderById);
        return ApiResponse.success("구매 대기 취소 완료",null);
    }

    private void validateCancelBuyWaiting(Account account, BuyDataRequest buyOrderById) {
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
    }

    @Transactional
    @Override
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
