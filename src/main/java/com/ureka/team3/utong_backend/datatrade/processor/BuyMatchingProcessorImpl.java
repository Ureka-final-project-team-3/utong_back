package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.*;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuyMatchingProcessorImpl implements BuyMatchingProcessor {
    private final TradeOrderQueueService tradeOrderQueueService;

    @Override
    public BuyMatchingResult handle(DataTradeDto.DataTradeRequestDto buyRequest) {
        Long lowestPrice = tradeOrderQueueService.getLoweSellPriceByDataCode(buyRequest.getDataCode());
        long remain = buyRequest.getDataAmount();
        if (lowestPrice == null || buyRequest.getPrice() < lowestPrice) {
            return BuyMatchingResult.underMinimumPrice(buyRequest);
        }

        List<TradeMatch> matches = matchOrders(buyRequest, lowestPrice);

        return BuyMatchingResult.of(matches, buyRequest);
    }

    private List<TradeMatch> matchOrders(DataTradeDto.DataTradeRequestDto buyRequest, Long priceLimit) {
        List<TradeMatch> matches = new ArrayList<>();
        long remaining = buyRequest.getDataAmount();

        while (remaining > 0) {
            OrderDto sellOrder = tradeOrderQueueService.popValidSellOrder(buyRequest.getDataCode(), priceLimit);
            if (sellOrder == null) break;

            long available = sellOrder.getQuantity();
            long used = Math.min(available, remaining);

            matches.add(TradeMatch.of(sellOrder, used));
            remaining -= used;

            if (available > used) {
                sellOrder.setQuantity(available - used);
                tradeOrderQueueService.requeuePartialSellOrder(sellOrder);
            }
        }
        return matches;
    }

}
