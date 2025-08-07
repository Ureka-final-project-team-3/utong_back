package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@Service
@RequiredArgsConstructor
public class SaleMatchingProcessorImpl implements SaleMatchingProcessor { // Lettuce 락 적용

    private final QueueService queueService;
    private final RedisLockRegistry lockRegistry;

    private static final String LOCK_KEY_FORMAT = "order:{%s}"; // 해시태그로 슬롯 고정
    private static final long LOCK_WAIT_TIME_SEC = 5;           // 락 대기 시간 (초)
    // 보유 만료 시간은 RedisLockRegistry 등록 시 expireAfter(ms)로 제어

    @Override
    public SaleMatchingResult handle(DataTradeDto.DataTradeRequestDto request) {
        Long highestBuyPrice = queueService.getHighestBuyPrice(request.getDataCode());
        if (highestBuyPrice == null || request.getPrice() > highestBuyPrice) {
            return SaleMatchingResult.overMaxPurchasePrice(request);
        }

        String lockKey = String.format(LOCK_KEY_FORMAT, request.getDataCode());
        Lock lock = lockRegistry.obtain(lockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_TIME_SEC, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("현재 다른 거래가 진행 중입니다. 잠시 후 다시 시도해주세요.");
            }

            List<TradeMatch> matches = matchOrders(request, highestBuyPrice);
            return SaleMatchingResult.of(matches, request);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("매칭 중단됨", e);
        } finally {
            if (acquired) {
                try {
                    lock.unlock();
                } catch (Exception ignore) {
                    // 이미 만료/해제된 경우 무시
                }
            }
        }
    }

    private List<TradeMatch> matchOrders(DataTradeDto.DataTradeRequestDto saleRequest, Long priceCeiling) {
        List<TradeMatch> matches = new ArrayList<>();
        long remaining = saleRequest.getDataAmount();

        while (remaining > 0) {
            // ✅ 최고 매수 호가 한도 내에서 주문 팝
            OrderDto buyOrder = queueService.popValidBuyOrder(saleRequest.getDataCode(), priceCeiling);
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
