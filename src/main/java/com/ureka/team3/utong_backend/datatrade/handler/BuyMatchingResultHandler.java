package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.PurchaseMatch;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.processor.TradeProcessor;
import com.ureka.team3.utong_backend.datatrade.service.trade.purchase.BuyDataRequestService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.OrderRecoveryService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BuyMatchingResultHandler {

    private final TradeProcessor tradeProcessor;
    private final TradeOrderQueueService tradeOrderQueueService;
    private final OrderRecoveryService orderRecoveryService;
    private final BuyDataRequestService buyDataRequestService;

    @Transactional
    public ApiResponse handle(BuyMatchingResult result, BuyDataRequest saved) {
        List<PurchaseMatch> matchList = result.getMatchList();
        buyDataRequestService.subtractPurchased(saved,saved.getQuantity() - result.getRemain());
        try {
            switch (result.getBuyMatchingStatus()) {
                case ALL_MATCHED -> {
                    matchList.forEach(match -> tradeProcessor.processBuyMatches(saved, match));
                    return TradeResponseFactory.successPurchaseComplete();
                }
                case PART_MATCHED -> {
                    matchList.forEach(match -> tradeProcessor.processBuyMatches(saved, match));
                    tradeOrderQueueService.addToBuyOrderQueue(saved, result.getRemain());
                    return TradeResponseFactory.successPurchasePartComplete(result);
                }
                case UNDER_MINIMUM_SALE_PRICE -> {
                    tradeOrderQueueService.addToBuyOrderQueue(saved, result.getRemain());
                    return TradeResponseFactory.waitingPurchase();
                }
                default -> throw new IllegalStateException("Unexpected Matching Status");
            }
        } catch (Exception e) {
            orderRecoveryService.restoreSellOrdersOnFailure(matchList, result);
            throw e; // 다시 던져서 트랜잭션 롤백
        }
    }
}
