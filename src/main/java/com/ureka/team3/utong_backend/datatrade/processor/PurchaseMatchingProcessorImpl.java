package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


@RequiredArgsConstructor
@Service
public class PurchaseMatchingProcessorImpl implements PurchaseMatchingProcessor {

    private final QueueService queueService;
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "order-lock:";
    private static final long LOCK_WAIT_TIME = 5; // 락 대기 시간 (초)
    private static final long LOCK_LEASE_TIME = 10; // 락 보유 시간 (초)

    @Override
    public PurchaseMatchingResult handle(DataTradeDto.DataTradeRequestDto buyRequest) {
        Long lowestPrice = queueService.getLoweSellPriceByDataCode(buyRequest.getDataCode());
        long remain = buyRequest.getDataAmount();

        if (lowestPrice == null || buyRequest.getPrice() < lowestPrice) {
            return PurchaseMatchingResult.underMinimumPrice(buyRequest);
        }

// 락 설정
        String lockKey = LOCK_PREFIX + buyRequest.getDataCode();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("현재 다른 거래가 진행 중입니다. 잠시 후 다시 시도해주세요.");
            }

// 매칭 로직 실행
            List<TradeMatch> matches = matchOrders(buyRequest, lowestPrice);
            return PurchaseMatchingResult.of(matches, buyRequest);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("매칭 중단됨", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
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
