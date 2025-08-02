package com.ureka.team3.utong_backend.datatrade.facade;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.service.AccountService;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.trade.ContractDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.RequestType;
import com.ureka.team3.utong_backend.datatrade.processor.PurchaseMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.processor.SaleMatchingProcessor;
import com.ureka.team3.utong_backend.datatrade.processor.TradeProcessor;
import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.PurchaseRequestService;
import com.ureka.team3.utong_backend.datatrade.processor.OrderRecoveryProcessorImpl;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueService;
import com.ureka.team3.utong_backend.datatrade.service.trade.sale.SaleRequestService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
import com.ureka.team3.utong_backend.datatrade.utils.TradeValidator;
import com.ureka.team3.utong_backend.line.service.LineService;
//import com.ureka.team3.utong_backend.publisher.TradeExecutePublisher;
import com.ureka.team3.utong_backend.datatrade.messaging.publisher.TradeExecutePublisher;
import com.ureka.team3.utong_backend.datatrade.messaging.message.TradeExecutedMessage;
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
    private final PurchaseRequestService purchaseRequestService;
    private final PurchaseMatchingProcessor purchaseMatchingProcessor;
    private final SaleRequestService saleRequestService;
    private final LineService lineService;
    private final SaleMatchingProcessor saleMatchingProcessor;
    private final AccountService accountService;
    private final TradeExecutePublisher tradeExecutePublisher;
    private final OrderRecoveryProcessorImpl orderRecoveryProcessorImpl;
    private final TradeProcessor tradeProcessor;
    private final QueueService queueService;

    @Override
    @Transactional
    public ApiResponse requestBuy(Account account, DataTradeDto.DataTradeRequestDto dto) {
        PurchaseMatchingResult purchaseMatchingResult = null;
        List<ContractDto> contractDtoList = new ArrayList<>();
        try {
            account = accountService.findById(account.getId());
            // 1. 검증
            tradeValidator.validatePurchase(account, dto);
            // 2. 저장
            BuyDataRequest saved = purchaseRequestService.save(account, dto);
            // 3. 매칭
            purchaseMatchingResult = purchaseMatchingProcessor.handle(dto);
            if(!purchaseMatchingResult.getPurchaseMatchingStatus().isWaitingOnly()){
                for (TradeMatch tradeMatch : purchaseMatchingResult.getMatchList()) {
                    ContractDto contractDto = tradeProcessor.processBuyMatches(saved, tradeMatch);
                    contractDtoList.add(contractDto);
                    account.decreasePoint(contractDto.getPrice()*contractDto.getQuantity());
                }
            }
            account.decreasePoint(purchaseMatchingResult.getRemain()*dto.getPrice());
            if(!purchaseMatchingResult.getPurchaseMatchingStatus().isAllMatched()){
                queueService.addToBuyOrderQueue(saved, purchaseMatchingResult.getRemain());
            }
            // 5. 결과 처리
            ApiResponse response = buildPurchaseResponse(purchaseMatchingResult);

            // 6. 메시지 발행
            TradeExecutedMessage message = buildPurchaseExecutedMessage(purchaseMatchingResult,contractDtoList);
            tradeExecutePublisher.publish(message);

            return response;
        } catch (Exception e) {
            if(purchaseMatchingResult !=null)
                orderRecoveryProcessorImpl.restoreSellOrdersOnFailure(purchaseMatchingResult);
            throw e;
        }
    }

    private static ApiResponse buildPurchaseResponse(PurchaseMatchingResult purchaseMatchingResult) {
        ApiResponse response;
        switch (purchaseMatchingResult.getPurchaseMatchingStatus()) {
            case ALL_MATCHED -> {
                response = TradeResponseFactory.successPurchaseComplete();
            }
            case PART_MATCHED -> {
                response = TradeResponseFactory.successPurchasePartComplete(purchaseMatchingResult);
            }
            case UNDER_MINIMUM_SALE_PRICE -> {
                response = TradeResponseFactory.waitingPurchase();
            }
            default -> throw new IllegalStateException("Unexpected Matching Status");
        }
        return response;
    }


    private TradeExecutedMessage buildPurchaseExecutedMessage(PurchaseMatchingResult purchaseMatchingResult, List<ContractDto> contractDtoList) {
        long remain = purchaseMatchingResult.getRemain() != null ? purchaseMatchingResult.getRemain() : 0L;
        String requestOrderId = contractDtoList.isEmpty()? null: contractDtoList.get(0).getPurchaseOrderId();
        return TradeExecutedMessage.builder()
                .dataCode(purchaseMatchingResult.getDataCode()) // 반드시 null 아님 확인
                .requestType(RequestType.PURCHASE)
                .matchedList(purchaseMatchingResult.getMatchList())
                .remain(remain)
                .requestPrice(purchaseMatchingResult.getPrice())
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

            SaleDataRequest saved = saleRequestService.save(account, dto);
            lineService.saleData(saved.getLineId(), dto.getDataAmount());  // 데이터 차감
            saleMatchingResult = saleMatchingProcessor.handle(dto);
            if(!saleMatchingResult.getSaleMatchingStatus().isWaitingOnly()){
                for (TradeMatch tradeMatch : saleMatchingResult.getMatchList()) {
                    ContractDto contractDto = tradeProcessor.processSaleMatches(saved, tradeMatch);
                    contractDtoList.add(contractDto);
                }
            }

            if(!saleMatchingResult.getSaleMatchingStatus().isAllMatched()){
                queueService.addToSaleOrderQueue(saved,saleMatchingResult.getRemain());
            }
            ApiResponse response;
            response = buildSaleApiResponse(saleMatchingResult);
            TradeExecutedMessage message = buildSaleExecutedMessage(saleMatchingResult,contractDtoList);
            tradeExecutePublisher.publish(message);
            return response;
        }catch (Exception e){
            if(saleMatchingResult!=null)
                orderRecoveryProcessorImpl.restoreBuyOrdersOnFailure(saleMatchingResult);
            throw e;
        }

    }

    private static ApiResponse buildSaleApiResponse(SaleMatchingResult saleMatchingResult) {
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
        return response;
    }

    private TradeExecutedMessage buildSaleExecutedMessage(SaleMatchingResult saleMatchingResult, List<ContractDto> contractDtoList) {
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