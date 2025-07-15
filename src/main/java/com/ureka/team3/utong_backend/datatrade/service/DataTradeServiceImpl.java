package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.auth.repository.LineRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.InsufficientPointException;
import com.ureka.team3.utong_backend.common.exception.business.LineNotFoundException;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderMQDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import com.ureka.team3.utong_backend.datatrade.repository.OrderMQRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.entity.LineData;
import com.ureka.team3.utong_backend.line.repository.LineDataRepository;
import com.ureka.team3.utong_backend.plan.entity.Plan;
import com.ureka.team3.utong_backend.price.entity.Price;
import com.ureka.team3.utong_backend.price.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.ureka.team3.utong_backend.datatrade.utils.TimeUtil.toEpochMillis;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataTradeServiceImpl implements DataTradeService {

    private final SaleDataRequestRepository saleDataRequestRepository;
    private final BuyDataRequestRepository buyDataRequestRepository;
    private final ContractRepository contractRepository;
    private final OrderMQRepository orderMQRepository;
    private final AccountRepository accountRepository;
    private final LineRepository lineRepository;
    private final LineDataRepository lineDataRepository;
    private final PriceRepository priceRepository;

    @Override
    @Transactional
    public ApiResponse requestBuy(Account account, DataTradeDto.BuyDataRequestDto dto) {
        String defaultLineId = account.getDefaultLine();
        if (defaultLineId == null) {
            return handleNotExistDefaultLine();
        }

        Line line = lineRepository.findById(defaultLineId).orElseThrow(LineNotFoundException::new);
        Long planTotalData = line.getPlan().getData();
        if (planTotalData == -1) {
            handlePurchaseUnlimitedPlan();
        }

        if(saleDataRequestRepository.existsWaitingRequestByLineId(line.getId())){
            return handleExistSaleRequest();
        }
        try {
            account.decreasePoint(dto.getPrice() * dto.getDataAmount());
            accountRepository.save(account);
        } catch (InsufficientPointException e) {
            return handleInsufficientPoint();
        }
        BuyDataRequest savedOrder = saveBuyOrder(account, dto);
        return handleBuyMatching(savedOrder);
    }

    private static ApiResponse<DataTradeDto.BuyDataResponseDto<Object>> handleExistSaleRequest() {
        return ApiResponse.success("이미 판매 대기중인 데이터가 있습니다. 취소 후 다시 이용해주세요", DataTradeDto.BuyDataResponseDto.builder()
                .result(BuyOrderResult.EXIST_SALE_REQUEST)
                .build());
    }

    private static ApiResponse<DataTradeDto.BuyDataResponseDto<Object>> handleNotExistDefaultLine() {
        return ApiResponse.success("데이터 거래에 필요한 기본 회선 선택 필수", DataTradeDto.BuyDataResponseDto.builder()
                .result(BuyOrderResult.NEED_DEFAULT_LINE)
                .build()
        );
    }

    private ApiResponse handleInsufficientPoint() {
        return ApiResponse.success("포인트 부족", DataTradeDto.BuyDataResponseDto.builder()
                .result(BuyOrderResult.INSUFFICIENT_POINT)
                .build());
    }

    private ApiResponse handlePurchaseUnlimitedPlan() {
        return ApiResponse.success("무제한 요금제는 데이터를 구매할 수 없습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.BORDERLESS)
                        .build());
    }

    private BuyDataRequest saveBuyOrder(Account account, DataTradeDto.BuyDataRequestDto dto) {
        return buyDataRequestRepository.save(BuyDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .lineId(account.getDefaultLine())
                .build());
    }

    private ApiResponse handleWaitingBuyRequest(BuyDataRequest request, Long alreadyBuy) {
        addToBuyOrderQueue(request, alreadyBuy);
        return ApiResponse.success("입력한 금액이 최저가보다 낮아 예약 구매로 등록되었습니다.",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.WAITING)
                        .build());
    }

    private void addToBuyOrderQueue(BuyDataRequest order, long alreadyBuy) {
        orderMQRepository.savePurchaseOrder(OrderMQDto.builder()
                .orderId(order.getId())
                .createdAt(toEpochMillis(order.getCreatedAt()))
                .expiredAt(toEpochMillis(order.getExpiredAt()))
                .quantity(order.getQuantity() - alreadyBuy)
                .dataCode(order.getDataCode())
                .price(order.getPrice())
                .build());
    }

    private ApiResponse handleBuyMatching(BuyDataRequest buyDataRequest) {
        Long lowestSellPrice = orderMQRepository.getLowestSellPrice(buyDataRequest.getDataCode());
        if (lowestSellPrice == null || buyDataRequest.getPrice() < lowestSellPrice) {
            return handleWaitingBuyRequest(buyDataRequest, 0L);
        }

        long remaining = buyDataRequest.getQuantity();

        while (remaining > 0) {
            OrderMQDto sellOrder = orderMQRepository.popValidSellOrder(buyDataRequest.getDataCode(), lowestSellPrice);
            if (sellOrder == null) break;
            SaleDataRequest saleDataRequest = saleDataRequestRepository.findById(sellOrder.getOrderId()).orElseThrow(IllegalArgumentException::new);

            long available = sellOrder.getQuantity();
            long used = Math.min(available, remaining);

            processTrade(buyDataRequest, saleDataRequest, used);
            remaining -= used;

            if (available > used) {
                sellOrder.setQuantity(available - used);
                orderMQRepository.requeuePartialSellOrder(sellOrder);
            }
        }

        if (remaining > 0) {
            addToBuyOrderQueue(buyDataRequest, buyDataRequest.getQuantity() - remaining);
        }
        return buildBuyResultResponse(buyDataRequest, remaining);
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
            return handleWaitingBuyRequest(request, 0L);
        }
    }

    private void processTrade(BuyDataRequest buyDataRequest, SaleDataRequest saleDataRequest, long used) {
        Contract contract = Contract.builder()
                .saleDataRequest(saleDataRequest)
                .buyDataRequest(buyDataRequest)
                .price(buyDataRequest.getPrice())
                .amount(used)
                .build();

        contractRepository.save(contract);

        // 포인트 지급
        Account account = saleDataRequest.getAccount();
        Price price = priceRepository.findAll().get(0);
        account.increasePoint((long) (buyDataRequest.getPrice() - ((float) buyDataRequest.getPrice() /100 * price.getTax())));

        Line line = lineRepository.findById(buyDataRequest.getLineId()).orElseThrow(IllegalArgumentException::new);
        LineData lineData = lineDataRepository.findLineDataByLine(line).stream().findFirst().orElseThrow(IllegalArgumentException::new);
        lineData.purchaseData(used);
    }

    @Override
    @Transactional
    public ApiResponse requestSale(Account account, DataTradeDto.SaleDataRequestDto dto) {
        // 1. 기본 회선 조회
        String defaultLineId = account.getDefaultLine();
        if (defaultLineId == null) {
            return handleNotExistDefaultLine();
        }

        Line line = lineRepository.findById(defaultLineId).orElseThrow(LineNotFoundException::new);
        Plan plan = line.getPlan();
        Long planTotalData = plan.getData();
        float availableTradeRate = plan.getAvailableTradeRate();
        LineData lineData = lineDataRepository.findLineDataByLine(line).stream().findFirst().orElseThrow(IllegalArgumentException::new);
        if (planTotalData == -1) {
            // 무제한 요금제는 판매 불가
            return handleSaleUnlimitedPlan();
        }

        Long canSaleData = (long) (((float) planTotalData / 100) * availableTradeRate - lineData.getSell() );
        if (dto.getDataAmount() > canSaleData) {
            return handleExceedSaleLimit();
        }

        if(buyDataRequestRepository.existsWaitingRequestByLineId(line.getId())){
            return handleExistBuyRequest();
        }
        SaleDataRequest savedOrder = saveSaleOrder(account, dto);
        lineData.saleData(dto.getDataAmount());
        return handleSaleMatching(savedOrder);
    }

    private ApiResponse handleExistBuyRequest() {
        return ApiResponse.success("이미 판매 대기중인 데이터가 있습니다. 취소 후 다시 이용해주세요", DataTradeDto.SaleDataResponseDto.builder()
                .result(SaleOrderResult.EXIST_BUY_REQUEST)
                .build());
    }


    private ApiResponse handleSaleUnlimitedPlan() {
        return ApiResponse.success("무제한 요금제는 데이터를 판매할 수 없습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.BORDERLESS)
                        .build());
    }

    private ApiResponse handleExceedSaleLimit() {
        return ApiResponse.success("판매 요청한 데이터가 판매 가능량을 초과했습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.EXCEED_SALE_LIMIT)
                        .build());
    }

    private SaleDataRequest saveSaleOrder(Account account, DataTradeDto.SaleDataRequestDto dto) {
        return saleDataRequestRepository.save(SaleDataRequest.builder()
                .price(dto.getPrice())
                .account(account)
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .lineId(account.getDefaultLine())
                .build());
    }

    private ApiResponse handleSaleMatching(SaleDataRequest saleDataRequest) {
        Long highestBuyPrice = orderMQRepository.getHighestBuyPrice(saleDataRequest.getDataCode());
        if (highestBuyPrice == null || saleDataRequest.getPrice() > highestBuyPrice) {
            return handleWaitingSaleRequest(saleDataRequest, 0L);
        }

        long remaining = saleDataRequest.getQuantity();

        while (remaining > 0) {
            OrderMQDto buyOrder = orderMQRepository.popValidBuyOrder(saleDataRequest.getDataCode(), highestBuyPrice);
            if (buyOrder == null) break;

            BuyDataRequest buyDataRequest = buyDataRequestRepository.findById(buyOrder.getOrderId())
                    .orElseThrow(IllegalArgumentException::new);

            long available = buyOrder.getQuantity();
            long used = Math.min(available, remaining);

            processTrade(buyDataRequest, saleDataRequest, used);
            remaining -= used;

            if (available > used) {
                buyOrder.setQuantity(available - used);
                orderMQRepository.requeuePartialBuyOrder(buyOrder);
            }
        }

        if (remaining > 0) {
            addToSellOrderQueue(saleDataRequest, saleDataRequest.getQuantity() - remaining);
        }

        return buildSaleResultResponse(saleDataRequest, remaining);
    }

    private ApiResponse handleWaitingSaleRequest(SaleDataRequest request, long alreadySold) {
        addToSellOrderQueue(request, alreadySold);
        return ApiResponse.success("입력한 가격이 최고 구매가보다 높아 예약 판매로 등록되었습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.WAITING)
                        .build());
    }

    private void addToSellOrderQueue(SaleDataRequest order, long alreadySold) {
        orderMQRepository.saveSellOrder(OrderMQDto.builder()
                .orderId(order.getId())
                .createdAt(toEpochMillis(order.getCreatedAt()))
                .expiredAt(toEpochMillis(order.getExpiredAt()))
                .quantity(order.getQuantity() - alreadySold)
                .dataCode(order.getDataCode())
                .price(order.getPrice())
                .build());
    }

    private ApiResponse buildSaleResultResponse(SaleDataRequest request, long remaining) {
        if (remaining == 0) {
            return ApiResponse.success("데이터 판매 성공",
                    DataTradeDto.SaleDataResponseDto.builder()
                            .result(SaleOrderResult.ALL_COMPLETE)
                            .build());
        } else if (remaining < request.getQuantity()) {
            return ApiResponse.success("일부 데이터만 판매 완료",
                    DataTradeDto.SaleDataResponseDto.builder()
                            .result(SaleOrderResult.PART_COMPLETE)
                            .remainData(remaining)
                            .build());
        } else {
            return handleWaitingSaleRequest(request, 0L);
        }
    }

}