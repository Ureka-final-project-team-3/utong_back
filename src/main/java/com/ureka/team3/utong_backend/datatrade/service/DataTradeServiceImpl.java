package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.UserRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.UserNotFoundException;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderMQDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.OrderMQRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ureka.team3.utong_backend.datatrade.utils.TimeUtil.toEpochMillis;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataTradeServiceImpl implements DataTradeService {

    private final SaleDataRequestRepository saleDataRequestRepository;
    private final BuyDataRequestRepository buyDataRequestRepository;
    private final UserRepository userRepository;
    private final OrderMQRepository orderMQRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public ApiResponse requestBuy(Account account, DataTradeDto.BuyDataRequestDto dto) {
        if (isInsufficientPoint(account, dto)) {
            return handleInsufficientPoint();
        }

        BuyDataRequest savedOrder = saveBuyOrder(account, dto);
        return handleBuyMatching(savedOrder);
    }

    private boolean isInsufficientPoint(Account account, DataTradeDto.BuyDataRequestDto dto) {
        log.info(String.valueOf(account.getMileage()));
        Account findAccount = accountRepository.findById(account.getId()).orElseThrow(UserNotFoundException::new);
        return findAccount.getMileage() < dto.getPrice() * dto.getDataAmount();
    }

    private ApiResponse handleInsufficientPoint() {
        return ApiResponse.success("포인트 부족", DataTradeDto.BuyDataResponseDto.builder()
                .result(BuyOrderResult.INSUFFICIENT_POINT)
                .build());
    }

    private BuyDataRequest saveBuyOrder(Account account, DataTradeDto.BuyDataRequestDto dto) {
        return buyDataRequestRepository.save(BuyDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .build());
    }

    private ApiResponse handleWaitingBuyRequest(BuyDataRequest request, Long alreadyBuy) {
        addToBuyOrderQueue(request,alreadyBuy);
        return ApiResponse.success("입력한 금액이 최저가보다 낮아 예약 구매로 등록되었습니다.",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.WAITING)
                        .build());
    }

    private void addToBuyOrderQueue(BuyDataRequest order,long alreadyBuy) {
        orderMQRepository.savePurchaseOrder(OrderMQDto.builder()
                .orderId(order.getId())
                .createdAt(toEpochMillis(order.getCreatedAt()))
                .expiredAt(toEpochMillis(order.getExpiredAt()))
                .quantity(order.getQuantity()-alreadyBuy)
                .dataCode(order.getDataCode())
                .price(order.getPrice())
                .build());
    }

    private ApiResponse handleBuyMatching(BuyDataRequest request) {
        Long lowestSellPrice = orderMQRepository.getLowestSellPrice(request.getDataCode());
        if (lowestSellPrice == null || request.getPrice() < lowestSellPrice) {
            return handleWaitingBuyRequest(request,0L);
        }

        long remaining = request.getQuantity();

        while (remaining > 0) {
            OrderMQDto order = orderMQRepository.popValidSellOrder(request.getDataCode(), lowestSellPrice);
            if (order == null) break;

            long available = order.getQuantity();
            long used = Math.min(available, remaining);

            processTrade(request, order, used);
            remaining -= used;

            if (available > used) {
                order.setQuantity(available - used);
                orderMQRepository.requeuePartialSellOrder(order);
            }
        }

        if(remaining>0) {
            addToBuyOrderQueue(request,request.getQuantity()-remaining);
        }
        return buildBuyResultResponse(request, remaining);
    }

    private ApiResponse buildBuyResultResponse(BuyDataRequest request, long remaining) {
        if (remaining == 0) {
            return ApiResponse.success("데이터 구매 성공",
                    DataTradeDto.BuyDataResponseDto.builder()
                            .result(BuyOrderResult.ALL_COMPLETE)
                            .build());
        } else if (remaining < request.getQuantity()) {
            return ApiResponse.success("일부 데이터만 구매 완료",
                    DataTradeDto.BuyDataResponseDto.builder()
                            .result(BuyOrderResult.PART_COMPLETE)
                            .remainData(remaining)
                            .build());
        } else {
            return handleWaitingBuyRequest(request,0L);
        }
    }

    private void processTrade(BuyDataRequest request, OrderMQDto order, long used) {
        // TODO: 거래 내역 저장, 포인트 차감/지급 처리 등
    }

    @Override
    public ApiResponse requestSale(Account account, DataTradeDto.SaleDataRequestDto dto) {
        SaleDataRequest saved = saleDataRequestRepository.save(SaleDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .build());

        addToSellOrderQueue(saved);

        return ApiResponse.success("판매 등록 완료", saved.getId());
    }

    private void addToSellOrderQueue(SaleDataRequest order) {
        orderMQRepository.saveSellOrder(OrderMQDto.builder()
                .orderId(order.getId())
                .createdAt(toEpochMillis(order.getCreatedAt()))
                .expiredAt(toEpochMillis(order.getExpiredAt()))
                .quantity(order.getQuantity())
                .dataCode(order.getDataCode())
                .price(order.getPrice())
                .build());
    }

    private void matchOrders(String dataCode, Long price) {
        // TODO: 매칭 로직 예정
    }
}