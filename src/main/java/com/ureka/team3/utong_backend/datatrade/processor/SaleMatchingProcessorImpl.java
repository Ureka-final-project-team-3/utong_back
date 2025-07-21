package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatch;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatchingResult;
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
    public SaleMatchingResult handle(DataTradeDto.SaleDataRequestDto request) {
        Long highestBuyPrice = tradeOrderQueueService.getHighestBuyPrice(request.getDataCode());
        long remaining = request.getDataAmount();

        if (highestBuyPrice == null || request.getPrice() < highestBuyPrice) {
            return SaleMatchingResult.overMaxPurchasePrice(remaining);
        }

        List<SaleMatch> matches = matchOrders(request, highestBuyPrice);

        long matchedAmount = matches.stream()
                .mapToLong(SaleMatch::getAmount)
                .sum();

        long remain = remaining - matchedAmount;

        return SaleMatchingResult.of(matches, remain);
    }

    private List<SaleMatch> matchOrders(DataTradeDto.SaleDataRequestDto saleRequest, Long priceLimit) {
        List<SaleMatch> matches = new ArrayList<>();
        long remaining = saleRequest.getDataAmount();

        while (remaining > 0) {
            // 구매자 대기 주문 중 가장 높은 가격을 가진 유효한 주문을 꺼낸다
            OrderDto buyOrder = tradeOrderQueueService.popValidBuyOrder(saleRequest.getDataCode(), priceLimit);
            if (buyOrder == null) break;

            long available = buyOrder.getQuantity();
            long used = Math.min(available, remaining);

            matches.add(SaleMatch.of(buyOrder, used));
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
