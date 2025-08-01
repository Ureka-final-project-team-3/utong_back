package com.ureka.team3.utong_backend.datatrade.service.trade.queue;

import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRepository;
import com.ureka.team3.utong_backend.datatrade.domain.result.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderRecoveryService {

    private final TradeOrderQueueService tradeOrderQueueService;
    private final OrderRepository orderRepository;

    public void restoreSellOrdersOnFailure(BuyMatchingResult result) {
        List<TradeMatch> matchList = result.getMatchList();
        Collections.reverse(matchList);

        for (int i = 0; i < matchList.size(); i++) {
            TradeMatch tradeMatch = matchList.get(i);
            OrderDto original = tradeMatch.getMatchedOrder();

            if (i == 0 && !result.getBuyMatchingStatus().isWaitingOnly()) {
                restoreFirstPoppedOrder(tradeMatch, original);
            } else {
                restoreSingleOrder(original);
            }
        }
    }

    public void restoreBuyOrdersOnFailure(SaleMatchingResult result) {
        List<TradeMatch> matchList = result.getMatchList();
        Collections.reverse(matchList);

        for (int i = 0; i < matchList.size(); i++) {
            TradeMatch tradeMatch = matchList.get(i);
            OrderDto original = tradeMatch.getMatchedOrder();

            if (i == 0 && !result.getSaleMatchingStatus().isWaitingOnly()) {
                restoreFirstPoppedBuyOrder(tradeMatch, original);
            } else {
                restoreSingleBuyOrder(original);
            }
        }
    }

    private void restoreFirstPoppedBuyOrder(TradeMatch tradeMatch, OrderDto original) {
        OrderDto toFix = orderRepository.popFirstBuyOrderFromList(original.getDataCode(), original.getPrice());

        if (toFix != null) {
            if (!toFix.getOrderId().equals(original.getOrderId())) {
                tradeOrderQueueService.requeuePartialBuyOrder(toFix);
                tradeOrderQueueService.requeuePartialBuyOrder(original);
            } else {
                original.setQuantity(original.getQuantity() + tradeMatch.getAmount());
                tradeOrderQueueService.requeuePartialBuyOrder(original);
            }
        }
    }

    private void restoreSingleBuyOrder(OrderDto order) {
        tradeOrderQueueService.requeuePartialBuyOrder(order);
    }


    private void restoreFirstPoppedOrder(TradeMatch tradeMatch, OrderDto original) {
        OrderDto toFix = orderRepository.popFirstSellOrderFromList(original.getDataCode(), original.getPrice());

        if (toFix != null) {
            if (!toFix.getOrderId().equals(original.getOrderId())) {
                tradeOrderQueueService.requeuePartialSellOrder(toFix);
                tradeOrderQueueService.requeuePartialSellOrder(original);
            } else {
                original.setQuantity(original.getQuantity() + tradeMatch.getAmount());
                tradeOrderQueueService.requeuePartialSellOrder(original);
            }
        }
    }

    private void restoreSingleOrder(OrderDto order) {
        tradeOrderQueueService.requeuePartialSellOrder(order);
    }
}
