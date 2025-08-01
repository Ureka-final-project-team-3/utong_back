package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.repository.perman.OrderRepository;
import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderRecoveryProcessorImpl implements OrderRecovertProcessor{

    private final QueueService queueService;
    private final OrderRepository orderRepository;

    @Override
    public void restoreSellOrdersOnFailure(PurchaseMatchingResult result) {
        List<TradeMatch> matchList = result.getMatchList();
        Collections.reverse(matchList);

        for (int i = 0; i < matchList.size(); i++) {
            TradeMatch tradeMatch = matchList.get(i);
            OrderDto original = tradeMatch.getMatchedOrder();

            if (i == 0 && !result.getPurchaseMatchingStatus().isWaitingOnly()) {
                restoreFirstPoppedOrder(tradeMatch, original);
            } else {
                restoreSingleOrder(original);
            }
        }
    }

    @Override
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
                queueService.requeuePartialBuyOrder(toFix);
                queueService.requeuePartialBuyOrder(original);
            } else {
                original.setQuantity(original.getQuantity() + tradeMatch.getAmount());
                queueService.requeuePartialBuyOrder(original);
            }
        }
    }

    private void restoreSingleBuyOrder(OrderDto order) {
        queueService.requeuePartialBuyOrder(order);
    }


    private void restoreFirstPoppedOrder(TradeMatch tradeMatch, OrderDto original) {
        OrderDto toFix = orderRepository.popFirstSellOrderFromList(original.getDataCode(), original.getPrice());

        if (toFix != null) {
            if (!toFix.getOrderId().equals(original.getOrderId())) {
                queueService.requeuePartialSellOrder(toFix);
                queueService.requeuePartialSellOrder(original);
            } else {
                original.setQuantity(original.getQuantity() + tradeMatch.getAmount());
                queueService.requeuePartialSellOrder(original);
            }
        }
    }

    private void restoreSingleOrder(OrderDto order) {
        queueService.requeuePartialSellOrder(order);
    }
}
