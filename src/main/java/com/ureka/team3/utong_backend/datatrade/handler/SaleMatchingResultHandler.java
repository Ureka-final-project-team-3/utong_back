package com.ureka.team3.utong_backend.datatrade.handler;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.processor.TradeProcessor;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.OrderRecoveryService;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import com.ureka.team3.utong_backend.datatrade.service.trade.sale.SaleDataRequestService;
import com.ureka.team3.utong_backend.datatrade.utils.TradeResponseFactory;
//import com.ureka.team3.utong_backend.publisher.TradeExecutePublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaleMatchingResultHandler {

    private final TradeProcessor tradeProcessor;
    private final TradeOrderQueueService tradeOrderQueueService;
    private final SaleDataRequestService saleDataRequestService;

    public ApiResponse handle(SaleMatchingResult result, SaleDataRequest saved) {
        List<TradeMatch> matchList = result.getMatchList();
        saleDataRequestService.subtractSell(saved, saved.getQuantity() - result.getRemain());
        ApiResponse response;

        try {
            switch (result.getSaleMatchingStatus()) {
                case ALL_MATCHED -> {
                    matchList.forEach(match -> tradeProcessor.processSaleMatches(saved, match));
                    response = TradeResponseFactory.successSaleComplete(result);
                }
                case PART_MATCHED -> {
                    matchList.forEach(match -> tradeProcessor.processSaleMatches(saved, match));
                    tradeOrderQueueService.addToSaleOrderQueue(saved, result.getRemain());
                    response = TradeResponseFactory.successSalePartComplete(result);
                }
                case OVER_MAX_PURCHASE_PRICE -> {
                    tradeOrderQueueService.addToSaleOrderQueue(saved, result.getRemain());
                    response = TradeResponseFactory.waitingSale();
                }
                default -> throw new IllegalStateException("Unexpected Matching Status");
            }

            return response;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new IllegalStateException();
        }
    }
}