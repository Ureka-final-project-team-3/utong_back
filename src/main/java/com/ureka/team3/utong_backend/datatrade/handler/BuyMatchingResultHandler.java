package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.dto.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.processor.TradeProcessor;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.BuyDataRequestService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.OrderRecoveryService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
//import com.ureka.team3.utong_backend.publisher.TradeExecutePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BuyMatchingResultHandler {

    private final TradeProcessor tradeProcessor;
    private final TradeOrderQueueService tradeOrderQueueService;
    private final BuyDataRequestService buyDataRequestService;

    @Transactional
    public ApiResponse handle(BuyMatchingResult result, BuyDataRequest saved) {
        List<TradeMatch> matchList = result.getMatchList();
        buyDataRequestService.subtractPurchased(saved, result.getUsed());
        ApiResponse response;

        try {
            switch (result.getBuyMatchingStatus()) {
                case ALL_MATCHED -> {
                    response = TradeResponseFactory.successPurchaseComplete();
                }
                case PART_MATCHED -> {
                    tradeOrderQueueService.addToBuyOrderQueue(saved, result.getRemain());
                    response = TradeResponseFactory.successPurchasePartComplete(result);
                }
                case UNDER_MINIMUM_SALE_PRICE -> {
                    tradeOrderQueueService.addToBuyOrderQueue(saved, result.getRemain());
                    response = TradeResponseFactory.waitingPurchase();
                }
                default -> throw new IllegalStateException("Unexpected Matching Status");
            }
            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalArgumentException();
        }
    }

}
