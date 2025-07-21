package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatch;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.processor.TradeProcessor;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.OrderRecoveryService;
import com.ureka.team3.utong_backend.datatrade.service.trade.sale.SaleDataRequestService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SaleMatchingResultHandler {

    private final TradeProcessor tradeProcessor;
    private final TradeOrderQueueService tradeOrderQueueService;
    private final OrderRecoveryService orderRecoveryService;
    private final SaleDataRequestService saleDataRequestService;

    public ApiResponse handle(SaleMatchingResult result, SaleDataRequest saved) {
        List<SaleMatch> matchList = result.getMatchList();
        saleDataRequestService.subtractSell(saved,saved.getQuantity() - result.getRemain());
        try {
            switch (result.getSaleMatchingStatus()) {
                case ALL_MATCHED -> {
                    matchList.forEach(match -> tradeProcessor.processSaleMatches(saved, match));
                    return TradeResponseFactory.successSaleComplete(result);
                }
                case PART_MATCHED -> {
                    matchList.forEach(match -> tradeProcessor.processSaleMatches(saved, match));
                    tradeOrderQueueService.addToSaleOrderQueue(saved, result.getRemain());
                    return TradeResponseFactory.successSalePartComplete(result);
                }
                case OVER_MAX_PURCHASE_PRICE -> {
                    tradeOrderQueueService.addToSaleOrderQueue(saved, result.getRemain());
                    return TradeResponseFactory.waitingSale();
                }
                default -> throw new IllegalStateException("Unexpected Matching Status");
            }
        } catch (Exception e) {
            orderRecoveryService.restoreBuyOrdersOnFailure(matchList, result);
            throw e;
        }
    }
}
