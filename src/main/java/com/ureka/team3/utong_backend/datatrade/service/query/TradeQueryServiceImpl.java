package com.ureka.team3.utong_backend.datatrade.service.query;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.LineNotFoundException;
import com.ureka.team3.utong_backend.datatrade.dto.*;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeQueryServiceImpl implements TradeQueryService {

    private final ContractRepository contractRepository;
    private final BuyDataRequestRepository buyDataRequestRepository;
    private final SaleDataRequestRepository saleDataRequestRepository;
    private final LineRepository lineRepository;

    private static final String STATUS_COMPLETED = "001";
    private static final String STATUS_PARTIAL = "002";
    private static final String STATUS_WAITING = "003";

    private static final String RANGE_MONTH = "MONTH";
    private static final String RANGE_YEAR = "YEAR";
    private static final String RANGE_WEEK = "WEEK";

    @Override
    public ApiResponse getMyPurchases(Account account, TradeHistoryRequestDto requestDto) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());

        List<PurchaseResponseDto> completePurchases = buildCompletePurchases(accountId, fromDate);
        List<PurchaseResponseDto> waitingPurchases = buildWaitingPurchases(accountId, fromDate);

        PurchaseHistoryResponseDto result = PurchaseHistoryResponseDto.builder()
                .completePurchases(completePurchases)
                .waitingPurchases(waitingPurchases)
                .build();

        return ApiResponse.success(result);
    }

    @Override
    public ApiResponse getMySales(Account account, TradeHistoryRequestDto requestDto) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());

        List<SaleResponseDto> completeSales = buildCompleteSales(accountId, fromDate);
        List<SaleResponseDto> waitingSales = buildWaitingSales(accountId, fromDate);

        SaleHistoryResponseDto result = SaleHistoryResponseDto.builder()
                .completeSales(completeSales)
                .waitingSales(waitingSales)
                .build();

        return ApiResponse.success(result);
    }

    private LocalDateTime calculateFromDate(String range) {
        LocalDateTime now = LocalDateTime.now();
        String safeRange = range != null ? range : RANGE_WEEK;

        return switch (safeRange) {
            case RANGE_MONTH -> now.minusMonths(1);
            case RANGE_YEAR -> now.minusYears(1);
            default -> now.minusDays(7);
        };
    }

    private List<PurchaseResponseDto> buildCompletePurchases(String accountId, LocalDateTime fromDate) {
        List<Contract> completedContracts = contractRepository.findCompletedPurchasesByAccountId(accountId, fromDate);
        List<PurchaseResponseDto> result = new ArrayList<>();

        for (Contract contract : completedContracts) {
            BuyDataRequest buyRequest = contract.getBuyDataRequest();
            Line line = findLineById(buyRequest.getLineId());

            PurchaseResponseDto dto = createPurchaseResponseDto(
                    buyRequest.getId(),
                    STATUS_COMPLETED,
                    buyRequest.getDataCode(),
                    contract.getAmount(),
                    contract.getCreatedAt(),
                    contract.getPrice(),
                    line.getPhoneNumber()
            );
            result.add(dto);
        }
        return result;
    }

    private List<PurchaseResponseDto> buildWaitingPurchases(String accountId, LocalDateTime fromDate) {
        List<BuyDataRequest> waitingRequests = buyDataRequestRepository.findWaitingPurchasesByAccountId(accountId, fromDate);
        List<PurchaseResponseDto> result = new ArrayList<>();

        for (BuyDataRequest buyRequest : waitingRequests) {
            Long quantity = calculateRemainingQuantity(buyRequest);
            Line line = findLineById(buyRequest.getLineId());

            PurchaseResponseDto dto = createPurchaseResponseDto(
                    buyRequest.getId(),
                    STATUS_WAITING,
                    buyRequest.getDataCode(),
                    quantity,
                    buyRequest.getCreatedAt(),
                    buyRequest.getPrice(),
                    line.getPhoneNumber()
            );
            result.add(dto);
        }
        return result;
    }

    private List<SaleResponseDto> buildCompleteSales(String accountId, LocalDateTime fromDate) {
        List<Contract> completedContracts = contractRepository.findCompletedSalesByAccountId(accountId, fromDate);
        List<SaleResponseDto> result = new ArrayList<>();

        for (Contract contract : completedContracts) {
            SaleDataRequest saleRequest = contract.getSaleDataRequest();
            Line line = findLineById(saleRequest.getLineId());

            SaleResponseDto dto = createSaleResponseDto(
                    saleRequest.getId(),
                    STATUS_COMPLETED,
                    saleRequest.getDataCode(),
                    contract.getAmount(),
                    contract.getCreatedAt(),
                    contract.getPrice(),
                    line.getPhoneNumber()
            );
            result.add(dto);
        }
        return result;
    }

    private List<SaleResponseDto> buildWaitingSales(String accountId, LocalDateTime fromDate) {
        List<SaleDataRequest> waitingRequests = saleDataRequestRepository.findWaitingSalesByAccountId(accountId, fromDate);
        List<SaleResponseDto> result = new ArrayList<>();

        for (SaleDataRequest saleRequest : waitingRequests) {
            Long quantity = calculateRemainingQuantity(saleRequest);
            Line line = findLineById(saleRequest.getLineId());

            SaleResponseDto dto = createSaleResponseDto(
                    saleRequest.getId(),
                    STATUS_WAITING,
                    saleRequest.getDataCode(),
                    quantity,
                    saleRequest.getCreatedAt(),
                    saleRequest.getPrice(),
                    line.getPhoneNumber()
            );
            result.add(dto);
        }
        return result;
    }

    private Line findLineById(String lineId) {
        return lineRepository.findById(lineId)
                .orElseThrow(LineNotFoundException::new);
    }

    private Long calculateRemainingQuantity(BuyDataRequest request) {
        return STATUS_PARTIAL.equals(request.getStatus()) ?
                request.getRemaining() : request.getQuantity();
    }

    private Long calculateRemainingQuantity(SaleDataRequest request) {
        return STATUS_PARTIAL.equals(request.getStatus()) ?
                request.getRemaining() : request.getQuantity();
    }

    private PurchaseResponseDto createPurchaseResponseDto(String purchaseId, String status, String dataCode,
                                                          Long quantity, LocalDateTime tradeDate,
                                                          Long pricePerGb, String phoneNumber) {
        return PurchaseResponseDto.builder()
                .purchaseId(purchaseId)
                .status(status)
                .dataCode(dataCode)
                .quantity(quantity)
                .tradeDate(tradeDate)
                .pricePerGb(pricePerGb)
                .phoneNumber(phoneNumber)
                .build();
    }

    private SaleResponseDto createSaleResponseDto(String saleId, String status, String dataCode,
                                                  Long quantity, LocalDateTime tradeDate,
                                                  Long pricePerGb, String phoneNumber) {
        return SaleResponseDto.builder()
                .saleId(saleId)
                .status(status)
                .dataCode(dataCode)
                .quantity(quantity)
                .tradeDate(tradeDate)
                .pricePerGb(pricePerGb)
                .phoneNumber(phoneNumber)
                .build();
    }
}