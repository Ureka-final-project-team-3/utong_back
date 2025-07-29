package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.*;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.TradeOrderQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SaleMatchingProcessorImpl implements SaleMatchingProcessor {
    private final TradeOrderQueueService tradeOrderQueueService;

    @Override
    public SaleMatchingResult handle(DataTradeDto.DataTradeRequestDto request) {
        Long highestBuyPrice = tradeOrderQueueService.getHighestBuyPrice(request.getDataCode());
        long remaining = request.getDataAmount();

        if (highestBuyPrice == null || request.getPrice() > highestBuyPrice) {
            return SaleMatchingResult.overMaxPurchasePrice(request);
        }

        List<TradeMatch> matches = matchOrders(request, highestBuyPrice);

        return SaleMatchingResult.of(matches, request);
    }

    private List<TradeMatch> matchOrders(DataTradeDto.DataTradeRequestDto saleRequest, Long priceLimit) {
        List<TradeMatch> matches = new ArrayList<>();
        long remaining = saleRequest.getDataAmount();

        while (remaining > 0) {
            // 구매자 대기 주문 중 가장 높은 가격을 가진 유효한 주문을 꺼낸다
            OrderDto buyOrder = tradeOrderQueueService.popValidBuyOrder(saleRequest.getDataCode(), saleRequest.getPrice());
            if (buyOrder == null) break;

            long available = buyOrder.getQuantity();
            long used = Math.min(available, remaining);

            matches.add(TradeMatch.of(buyOrder, used));
            remaining -= used;

            // 구매자가 원하는 양이 일부 남았다면 재삽입
            if (available > used) {
                buyOrder.setQuantity(available - used);
                tradeOrderQueueService.requeuePartialBuyOrder(buyOrder);
            }
        }

        return matches;
    }

}
