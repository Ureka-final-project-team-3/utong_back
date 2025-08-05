package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SaleMatchingProcessorImpl implements SaleMatchingProcessor {

    private final QueueService queueService;
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "order-lock:";
    private static final long LOCK_WAIT_TIME = 5; // 락 대기 시간 (초)
    private static final long LOCK_LEASE_TIME = 10; // 락 보유 시간 (초)

    @Override
    public SaleMatchingResult handle(DataTradeDto.DataTradeRequestDto request) {
        Long highestBuyPrice = queueService.getHighestBuyPrice(request.getDataCode());
        long remaining = request.getDataAmount();

        if (highestBuyPrice == null || request.getPrice() > highestBuyPrice) {
            return SaleMatchingResult.overMaxPurchasePrice(request);
        }

        String lockKey = LOCK_PREFIX + request.getDataCode();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean acquired = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!acquired) {
                throw new RuntimeException("현재 다른 거래가 진행 중입니다. 잠시 후 다시 시도해주세요.");
            }

            List<TradeMatch> matches = matchOrders(request);
            return SaleMatchingResult.of(matches, request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("매칭 중단됨", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private List<TradeMatch> matchOrders(DataTradeDto.DataTradeRequestDto saleRequest) {
        List<TradeMatch> matches = new ArrayList<>();
        long remaining = saleRequest.getDataAmount();

        while (remaining > 0) {
            OrderDto buyOrder = queueService.popValidBuyOrder(saleRequest.getDataCode(), saleRequest.getPrice());
            if (buyOrder == null) break;

            long available = buyOrder.getQuantity();
            long used = Math.min(available, remaining);

            matches.add(TradeMatch.of(buyOrder, used));
            remaining -= used;

            if (available > used) {
                buyOrder.setQuantity(available - used);
                queueService.requeuePartialBuyOrder(buyOrder);
            }
        }

        return matches;
    }
}
