package com.ureka.team3.utong_backend.datatrade.processor;

import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrderDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.service.trade.queue.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

@RequiredArgsConstructor
@Service
public class PurchaseMatchingProcessorImpl implements PurchaseMatchingProcessor {

    private final QueueService queueService;
    private final RedisLockRegistry lockRegistry;

    // 클러스터 모드 대비 슬롯 고정: {dataCode} 해시태그 사용
    private static final String LOCK_PREFIX = "order:{%s}";
    private static final long LOCK_WAIT_TIME_SEC = 5;   // 락 대기 시간
    // 실제 보유 만료는 RedisLockRegistry 생성 시 expireAfter(위에서 10초)로 관리됨

    @Override
    public PurchaseMatchingResult handle(DataTradeDto.DataTradeRequestDto buyRequest) {
        Long lowestPrice = queueService.getLoweSellPriceByDataCode(buyRequest.getDataCode());
        if (lowestPrice == null || buyRequest.getPrice() < lowestPrice) {
            return PurchaseMatchingResult.underMinimumPrice(buyRequest);
        }

        String lockKey = String.format(LOCK_PREFIX, buyRequest.getDataCode());
        Lock lock = lockRegistry.obtain(lockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(LOCK_WAIT_TIME_SEC, TimeUnit.SECONDS);
            if (!acquired) {
                throw new IllegalStateException("현재 다른 거래가 진행 중입니다. 잠시 후 다시 시도해주세요.");
            }

            // ✅ 가격 상한을 올바르게 사용하도록 수정
            List<TradeMatch> matches = matchOrders(buyRequest, lowestPrice);
            return PurchaseMatchingResult.of(matches, buyRequest);

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("매칭 처리 중 인터럽트 발생", ie);
        } finally {
            // 만료되었더라도 unlock 시도 (이미 만료면 내부적으로 무시되거나 예외 없음)
            if (acquired) {
                try {
                    lock.unlock();
                } catch (Exception ignore) {
                    // 이미 만료/해제된 경우 무시
                }
            }
        }
    }

    private List<TradeMatch> matchOrders(DataTradeDto.DataTradeRequestDto buyRequest, Long priceLimit) {
        List<TradeMatch> matches = new ArrayList<>();
        long remaining = buyRequest.getDataAmount();

        while (remaining > 0) {
            // ✅ priceLimit을 사용하도록 수정 (최저가 이상에서만 매칭)
            OrderDto sellOrder = queueService.popValidSellOrder(buyRequest.getDataCode(), priceLimit);
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
