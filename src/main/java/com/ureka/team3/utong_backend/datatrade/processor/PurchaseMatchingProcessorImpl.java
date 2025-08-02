package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseMatchingProcessorImpl implements PurchaseMatchingProcessor {
    private final QueueService queueService;

    @Override
    public PurchaseMatchingResult handle(DataTradeDto.DataTradeRequestDto buyRequest) {
        Long lowestPrice = queueService.getLoweSellPriceByDataCode(buyRequest.getDataCode());
        long remain = buyRequest.getDataAmount();
        if (lowestPrice == null || buyRequest.getPrice() < lowestPrice) {
            return PurchaseMatchingResult.underMinimumPrice(buyRequest);
        }

        List<TradeMatch> matches = matchOrders(buyRequest, lowestPrice);

        return PurchaseMatchingResult.of(matches, buyRequest);
    }

    private List<TradeMatch> matchOrders(DataTradeDto.DataTradeRequestDto buyRequest, Long priceLimit) {
        List<TradeMatch> matches = new ArrayList<>();
        long remaining = buyRequest.getDataAmount();

        while (remaining > 0) {
            OrderDto sellOrder = queueService.popValidSellOrder(buyRequest.getDataCode(), buyRequest.getPrice());
            if (sellOrder == null) break;

            long available = sellOrder.getQuantity();
            long used = Math.min(available, remaining);

            matches.add(TradeMatch.of(sellOrder, used));
            remaining -= used;

            if (available > used) {
                sellOrder.setQuantity(available - used);
                queueService.requeuePartialSellOrder(sellOrder);
            }
        }
        return matches;
    }

}
