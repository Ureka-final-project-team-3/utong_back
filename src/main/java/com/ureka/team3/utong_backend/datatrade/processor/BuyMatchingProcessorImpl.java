package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.dto.PurchaseMatch;
import com.ureka.team3.utong_backend.datatrade.service.TradeOrderQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BuyMatchingProcessorImpl implements BuyMatchingProcessor {
    private final TradeOrderQueueService tradeOrderQueueService;

    @Override
    public BuyMatchingResult handle(DataTradeDto.BuyDataRequestDto buyRequest) {
        Long lowestPrice = tradeOrderQueueService.getLoweSellPriceByDataCode(buyRequest.getDataCode());
        long remain = buyRequest.getDataAmount();
        if (lowestPrice == null || buyRequest.getPrice() < lowestPrice) {
            return BuyMatchingResult.underMinimumPrice(remain);
        }

        List<PurchaseMatch> matches = matchOrders(buyRequest, lowestPrice);

        long matchedAmount = matches.stream().mapToLong(PurchaseMatch::getAmount).sum();

        remain -= matchedAmount;
        return BuyMatchingResult.of(matches, remain);
    }

    private List<PurchaseMatch> matchOrders(DataTradeDto.BuyDataRequestDto buyRequest, Long priceLimit) {
        List<PurchaseMatch> matches = new ArrayList<>();
        long remaining = buyRequest.getDataAmount();

        while (remaining > 0) {
            OrderDto sellOrder = tradeOrderQueueService.popValidSellOrder(buyRequest.getDataCode(), priceLimit);
            if (sellOrder == null) break;

            long available = sellOrder.getQuantity();
            long used = Math.min(available, remaining);

            matches.add(PurchaseMatch.of(sellOrder, used));
            remaining -= used;

            if (available > used) {
                sellOrder.setQuantity(available - used);
                tradeOrderQueueService.requeuePartialSellOrder(sellOrder);
            }
        }
        return matches;
    }

}
