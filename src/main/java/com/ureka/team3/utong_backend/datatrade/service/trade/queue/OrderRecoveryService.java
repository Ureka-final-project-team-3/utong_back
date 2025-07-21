package com.ureka.team3.utong_backend.datatrade.service.trade.queue;

import com.ureka.team3.utong_backend.datatrade.dto.*;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderRecoveryService {

    private final TradeOrderQueueService tradeOrderQueueService;
    private final OrderRepository orderRepository;

    public void restoreSellOrdersOnFailure(List<PurchaseMatch> matchList, BuyMatchingResult result) {
        Collections.reverse(matchList);

        for (int i = 0; i < matchList.size(); i++) {
            PurchaseMatch purchaseMatch = matchList.get(i);
            OrderDto original = purchaseMatch.getMatchedOrder();

            if (i == 0 && !result.getBuyMatchingStatus().isWaitingOnly()) {
                restoreFirstPoppedOrder(purchaseMatch, original);
            } else {
                restoreSingleOrder(original);
            }
        }
    }

    public void restoreBuyOrdersOnFailure(List<SaleMatch> matchList, SaleMatchingResult result) {
        Collections.reverse(matchList);

        for (int i = 0; i < matchList.size(); i++) {
            SaleMatch saleMatch = matchList.get(i);
            OrderDto original = saleMatch.getMatchedOrder();

            if (i == 0 && !result.getSaleMatchingStatus().isWaitingOnly()) {
                restoreFirstPoppedBuyOrder(saleMatch, original);
            } else {
                restoreSingleBuyOrder(original);
            }
        }
    }

    private void restoreFirstPoppedBuyOrder(SaleMatch saleMatch, OrderDto original) {
        OrderDto toFix = orderRepository.popFirstBuyOrderFromList(original.getDataCode(), original.getPrice());

        if (toFix != null) {
            if (!toFix.getOrderId().equals(original.getOrderId())) {
                tradeOrderQueueService.requeuePartialBuyOrder(toFix);
                tradeOrderQueueService.requeuePartialBuyOrder(original);
            } else {
                original.setQuantity(original.getQuantity() + saleMatch.getAmount());
                tradeOrderQueueService.requeuePartialBuyOrder(original);
            }
        }
    }

    private void restoreSingleBuyOrder(OrderDto order) {
        tradeOrderQueueService.requeuePartialBuyOrder(order);
    }


    private void restoreFirstPoppedOrder(PurchaseMatch purchaseMatch, OrderDto original) {
        OrderDto toFix = orderRepository.popFirstSellOrderFromList(original.getDataCode(), original.getPrice());

        if (toFix != null) {
            if (!toFix.getOrderId().equals(original.getOrderId())) {
                tradeOrderQueueService.requeuePartialSellOrder(toFix);
                tradeOrderQueueService.requeuePartialSellOrder(original);
            } else {
                original.setQuantity(original.getQuantity() + purchaseMatch.getAmount());
                tradeOrderQueueService.requeuePartialSellOrder(original);
            }
        }
    }

    private void restoreSingleOrder(OrderDto order) {
        tradeOrderQueueService.requeuePartialSellOrder(order);
    }
}
