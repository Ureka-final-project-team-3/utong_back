package com.ureka.team3.utong_backend.datatrade.facade;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.AccountService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.*;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.RequestType;
import com.ureka.team3.utong_backend.datatrade.handler.BuyMatchingResultHandler;
import com.ureka.team3.utong_backend.datatrade.handler.SaleMatchingResultHandler;
import com.ureka.team3.utong_backend.datatrade.processor.BuyMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.processor.SaleMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.processor.TradeProcessor;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.BuyDataRequestService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.OrderRecoveryService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import com.ureka.team3.utong_backend.datatrade.service.trade.sale.SaleDataRequestService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
import com.ureka.team3.utong_backend.datatrade.validator.TradeValidator;
import com.ureka.team3.utong_backend.line.service.LineService;
//import com.ureka.team3.utong_backend.publisher.TradeExecutePublisher;
import com.ureka.team3.utong_backend.datatrade.publisher.TradeExecutePublisher;
import com.ureka.team3.utong_backend.datatrade.publisher.dto.TradeExecutedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataTradeFacadeImpl implements DataTradeFacade {

    private final TradeValidator tradeValidator;
    private final TradeCalculator tradeCalculator;
    private final BuyDataRequestService buyDataRequestService;
    private final BuyMatchingProcessor buyMatchingProcessor;
    private final BuyMatchingResultHandler buyMatchingResultHandler;
    private final SaleDataRequestService saleDataRequestService;
    private final LineService lineService;
    private final SaleMatchingProcessor saleMatchingProcessor;
    private final SaleMatchingResultHandler saleMatchingResultHandler;
    private final AccountService accountService;
    private final TradeExecutePublisher tradeExecutePublisher;
    private final OrderRecoveryService orderRecoveryService;
    private final TradeProcessor tradeProcessor;
    private final TradeOrderQueueService tradeOrderQueueService;

    @Override
    @Transactional
    public ApiResponse requestBuy(Account account, DataTradeDto.DataTradeRequestDto dto) {
        BuyMatchingResult buyMatchingResult = null;
        List<ContractDto> contractDtoList = new ArrayList<>();
        try {
            account = accountService.findById(account.getId());

            // 1. 검증
            tradeValidator.validatePurchase(account, dto);

            // 2. 저장
            BuyDataRequest saved = buyDataRequestService.save(account, dto);

            // 3. 매칭
            buyMatchingResult = buyMatchingProcessor.handle(dto);
            if(!buyMatchingResult.getBuyMatchingStatus().isWaitingOnly()){
                for (TradeMatch tradeMatch : buyMatchingResult.getMatchList()) {
                    ContractDto contractDto = tradeProcessor.processBuyMatches(saved, tradeMatch);
                    contractDtoList.add(contractDto);
                    account.decreasePoint(contractDto.getPrice()*contractDto.getQuantity());
                }
            }
            account.decreasePoint(buyMatchingResult.getRemain()*dto.getPrice());
            if(!buyMatchingResult.getBuyMatchingStatus().isAllMatched()){
                tradeOrderQueueService.addToBuyOrderQueue(saved,buyMatchingResult.getRemain());
            }
            // 5. 결과 처리
            ApiResponse response;
            switch (buyMatchingResult.getBuyMatchingStatus()) {
                case ALL_MATCHED -> {
                    response = TradeResponseFactory.successPurchaseComplete();
                }
                case PART_MATCHED -> {
                    response = TradeResponseFactory.successPurchasePartComplete(buyMatchingResult);
                }
                case UNDER_MINIMUM_SALE_PRICE -> {
                    response = TradeResponseFactory.waitingPurchase();
                }
                default -> throw new IllegalStateException("Unexpected Matching Status");
            }

            // 6. 메시지 발행
            TradeExecutedMessage message = buildPurchaseExecutedMessage(buyMatchingResult,contractDtoList);
            tradeExecutePublisher.publish(message);

            return response;
        } catch (Exception e) {
            if(buyMatchingResult!=null)
                orderRecoveryService.restoreSellOrdersOnFailure(buyMatchingResult);
            throw e;
        }
    }


    private TradeExecutedMessage buildPurchaseExecutedMessage(BuyMatchingResult buyMatchingResult, List<ContractDto> contractDtoList) {
        long used = buyMatchingResult.getUsed() != null ? buyMatchingResult.getUsed() : 0L;
        long remain = buyMatchingResult.getRemain() != null ? buyMatchingResult.getRemain() : 0L;
        String requestOrderId = contractDtoList.isEmpty()? null: contractDtoList.get(0).getPurchaseOrderId();
        return TradeExecutedMessage.builder()
                .dataCode(buyMatchingResult.getDataCode()) // 반드시 null 아님 확인
                .requestType(RequestType.PURCHASE)
                .matchedList(buyMatchingResult.getMatchList())
                .remain(remain)
                .requestPrice(buyMatchingResult.getPrice())
                .newContracts(contractDtoList)
                .requestOrderId(requestOrderId)
                .build();
    }


    @Override
    @Transactional
    public ApiResponse requestSale(Account account, DataTradeDto.DataTradeRequestDto dto) {
        // 1. 기본 회선 조회
        SaleMatchingResult saleMatchingResult = null;
        List<ContractDto> contractDtoList = new ArrayList<>();
        try{
            String defaultLineId = account.getDefaultLine();
            tradeValidator.validateSale(defaultLineId, dto);

            SaleDataRequest saved = saleDataRequestService.save(account, dto);
            lineService.saleData(saved.getLineId(), dto.getDataAmount());  // 데이터 차감
            saleMatchingResult = saleMatchingProcessor.handle(dto);
            if(!saleMatchingResult.getSaleMatchingStatus().isWaitingOnly()){
                for (TradeMatch tradeMatch : saleMatchingResult.getMatchList()) {
                    ContractDto contractDto = tradeProcessor.processSaleMatches(saved, tradeMatch);
                    contractDtoList.add(contractDto);
                }
            }

            if(!saleMatchingResult.getSaleMatchingStatus().isAllMatched()){
                tradeOrderQueueService.addToSaleOrderQueue(saved,saleMatchingResult.getRemain());
            }
            ApiResponse response;
            switch (saleMatchingResult.getSaleMatchingStatus()) {
                case ALL_MATCHED -> {
                    response = TradeResponseFactory.successSaleComplete(saleMatchingResult);
                }
                case PART_MATCHED -> {
                    response = TradeResponseFactory.successSalePartComplete(saleMatchingResult);
                }
                case OVER_MAX_PURCHASE_PRICE -> {
                    response = TradeResponseFactory.waitingSale();
                }
                default -> throw new IllegalStateException("Unexpected Matching Status");
            }
            TradeExecutedMessage message = buildSaleExecutedMessage(saleMatchingResult,contractDtoList);
            tradeExecutePublisher.publish(message);
            return response;
        }catch (Exception e){
            if(saleMatchingResult!=null)
                orderRecoveryService.restoreBuyOrdersOnFailure(saleMatchingResult);
            throw e;
        }

    }

    private TradeExecutedMessage buildSaleExecutedMessage(SaleMatchingResult saleMatchingResult, List<ContractDto> contractDtoList) {
        long used = saleMatchingResult.getUsed() != null ? saleMatchingResult.getUsed() : 0L;
        long remain = saleMatchingResult.getRemain() != null ? saleMatchingResult.getRemain() : 0L;
        String requestOrderId = contractDtoList.isEmpty() ? null: contractDtoList.get(0).getSaleOrderId();

        return TradeExecutedMessage.builder()
                .dataCode(saleMatchingResult.getDataCode())
                .requestType(RequestType.SALE)
                .matchedList(saleMatchingResult.getMatchList())
                .remain(remain)
                .requestPrice(saleMatchingResult.getPrice())
                .requestOrderId(requestOrderId)
                .newContracts(contractDtoList)
                .build();
    }

}