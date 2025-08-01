package com.ureka.team3.utong_backend.datatrade.service.query;

//@Service
//@RequiredArgsConstructor
//public class TradeQueryServiceImpl implements TradeQueryService {
//
//    private final ContractRepository contractRepository;
//    private final BuyDataRequestRepository buyDataRequestRepository;
//    private final SaleDataRequestRepository saleDataRequestRepository;
//    private final LineRepository lineRepository;
//
//    private static final String STATUS_COMPLETED = "001";
//    private static final String STATUS_PARTIAL = "002";
//    private static final String STATUS_WAITING = "003";
//
//    private static final String RANGE_TODAY = "TODAY";
//    private static final String RANGE_MONTH = "MONTH";
//    private static final String RANGE_YEAR = "YEAR";
//    private static final String RANGE_WEEK = "WEEK";
//    private static final String RANGE_ALL = "ALL";
//
//
//    @Override
//    public ApiResponse getMyPurchases(Account account, TradeHistoryRequestDto requestDto) {
//        String accountId = account.getId();
//        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());
//
//        List<PurchaseResponseDto> completePurchases = buildCompletePurchases(accountId, fromDate);
//        List<PurchaseResponseDto> waitingPurchases = buildWaitingPurchases(accountId, fromDate);
//
//        PurchaseHistoryResponseDto result = PurchaseHistoryResponseDto.builder()
//                .completePurchases(completePurchases)
//                .waitingPurchases(waitingPurchases)
//                .build();
//
//        return ApiResponse.success(result);
//    }
//
//    @Override
//    public ApiResponse getMySales(Account account, TradeHistoryRequestDto requestDto) {
//        String accountId = account.getId();
//        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());
//
//        List<SaleResponseDto> completeSales = buildCompleteSales(accountId, fromDate);
//        List<SaleResponseDto> waitingSales = buildWaitingSales(accountId, fromDate);
//
//        SaleHistoryResponseDto result = SaleHistoryResponseDto.builder()
//                .completeSales(completeSales)
//                .waitingSales(waitingSales)
//                .build();
//
//        return ApiResponse.success(result);
//    }
//
//    private LocalDateTime calculateFromDate(String range) {
//        LocalDateTime now = LocalDateTime.now();
//        String safeRange = range != null ? range : RANGE_ALL;
//
//        return switch (safeRange) {
//            case RANGE_TODAY -> now.toLocalDate().atStartOfDay();
//            case RANGE_WEEK -> now.minusDays(7);
//            case RANGE_MONTH -> now.minusMonths(1);
//            case RANGE_YEAR -> now.minusYears(1);
//            default -> LocalDateTime.of(2000, 1, 1, 0, 0);
//        };
//    }
//
//    private List<PurchaseResponseDto> buildCompletePurchases(String accountId, LocalDateTime fromDate) {
//        List<Contract> completedContracts = contractRepository.findCompletedPurchasesByAccountId(accountId, fromDate);
//        List<PurchaseResponseDto> result = new ArrayList<>();
//
//        for (Contract contract : completedContracts) {
//            BuyDataRequest buyRequest = contract.getBuyDataRequest();
//            Line line = findLineById(buyRequest.getLineId());
//
//            PurchaseResponseDto dto = createPurchaseResponseDto(
//                    buyRequest.getId(),
//                    STATUS_COMPLETED,
//                    buyRequest.getDataCode(),
//                    contract.getAmount(),
//                    contract.getCreatedAt(),
//                    contract.getPrice(),
//                    line.getPhoneNumber()
//            );
//            result.add(dto);
//        }
//        return result;
//    }
//
//    private List<PurchaseResponseDto> buildWaitingPurchases(String accountId, LocalDateTime fromDate) {
//        List<BuyDataRequest> waitingRequests = buyDataRequestRepository.findWaitingPurchasesByAccountId(accountId, fromDate);
//        List<PurchaseResponseDto> result = new ArrayList<>();
//
//        for (BuyDataRequest buyRequest : waitingRequests) {

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
import com.ureka.team3.utong_backend.line.entity.Line;
import com.ureka.team3.utong_backend.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.LinkedHashMap;


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

    private static final String RANGE_TODAY = "TODAY";
    private static final String RANGE_MONTH = "MONTH";
    private static final String RANGE_YEAR = "YEAR";
    private static final String RANGE_WEEK = "WEEK";
    private static final String RANGE_ALL = "ALL";


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
        String safeRange = range != null ? range : RANGE_ALL;

        return switch (safeRange) {
            case RANGE_TODAY -> now.toLocalDate().atStartOfDay();
            case RANGE_WEEK -> now.minusDays(7);
            case RANGE_MONTH -> now.minusMonths(1);
            case RANGE_YEAR -> now.minusYears(1);
            default -> LocalDateTime.of(2000, 1, 1, 0, 0);
        };
    }

    private List<PurchaseResponseDto> buildCompletePurchases(String accountId, LocalDateTime fromDate) {
        List<Contract> contracts = contractRepository.findCompletedPurchasesByAccountId(accountId, fromDate);

        // 1. purchaseId 기준으로 그룹핑
        Map<String, List<Contract>> groupedContracts = contracts.stream()
                .collect(Collectors.groupingBy(c -> c.getBuyDataRequest().getId(),
                        LinkedHashMap::new, Collectors.toList()));

        List<PurchaseResponseDto> result = new ArrayList<>();

        for (Map.Entry<String, List<Contract>> entry : groupedContracts.entrySet()) {
            String purchaseId = entry.getKey();
            List<Contract> contractList = entry.getValue();

            BuyDataRequest buyRequest = contractList.get(0).getBuyDataRequest();

            if (buyRequest.getRemaining() != null && buyRequest.getRemaining() > 0) {
                continue;
            }

            Line line = findLineById(buyRequest.getLineId());

            PurchaseResponseDto dto = PurchaseResponseDto.builder()
                    .purchaseId(purchaseId)
                    .status(STATUS_COMPLETED)
                    .dataCode(buyRequest.getDataCode())
                    .quantity(contractList.stream().mapToLong(Contract::getAmount).sum()) // 합산
                    .remaining(buyRequest.getRemaining())
                    .tradeDate(contractList.stream()
                            .map(Contract::getCreatedAt)
                            .max(LocalDateTime::compareTo).orElse(null)) // 최신 체결시간
                    .pricePerGb(contractList.get(0).getPrice()) // 동일 단가라면 아무거나
                    .phoneNumber(line.getPhoneNumber())
                    .contractDto(buildContractDtosForBuy(purchaseId))
                    .build();

            result.add(dto);
            result.sort(Comparator.comparing(PurchaseResponseDto::getTradeDate).reversed());
        }
        return result;
    }

    private List<PurchaseResponseDto> buildWaitingPurchases(String accountId, LocalDateTime fromDate) {
        List<BuyDataRequest> waitingRequests = buyDataRequestRepository.findWaitingPurchasesByAccountId(accountId, fromDate);
        List<PurchaseResponseDto> result = new ArrayList<>();

        for (BuyDataRequest buyRequest : waitingRequests) {
            Long requestedQuantity = buyRequest.getQuantity();
            Long remainingQuantity = calculateRemainingQuantity(buyRequest);

            Line line = findLineById(buyRequest.getLineId());

            PurchaseResponseDto dto = PurchaseResponseDto.builder()
                    .purchaseId(buyRequest.getId())
                    .status(STATUS_WAITING)
                    .dataCode(buyRequest.getDataCode())
                    .quantity(requestedQuantity)
                    .remaining(remainingQuantity)
                    .tradeDate(buyRequest.getCreatedAt())
                    .pricePerGb(buyRequest.getPrice())
                    .phoneNumber(line.getPhoneNumber())
                    .contractDto(buildContractDtosForBuy(buyRequest.getId()))
                    .build();

            result.add(dto);
        }
        return result;
    }

    private List<SaleResponseDto> buildCompleteSales(String accountId, LocalDateTime fromDate) {
        List<Contract> contracts = contractRepository.findCompletedSalesByAccountId(accountId, fromDate);

        // 1. purchaseId 기준으로 그룹핑
        Map<String, List<Contract>> groupedContracts = contracts.stream()
                .collect(Collectors.groupingBy(c -> c.getSaleDataRequest().getId(),
                        LinkedHashMap::new, Collectors.toList()));

        List<SaleResponseDto> result = new ArrayList<>();

            for (Map.Entry<String, List<Contract>> entry : groupedContracts.entrySet()) {
            String saleId = entry.getKey();
            List<Contract> contractList = entry.getValue();

            SaleDataRequest saleRequest = contractList.get(0).getSaleDataRequest();

                if (saleRequest.getRemaining() != null && saleRequest.getRemaining() > 0) {
                    continue;
                }

            Line line = findLineById(saleRequest.getLineId());

                SaleResponseDto dto = SaleResponseDto.builder()
                    .saleId(saleId)
                    .status(STATUS_COMPLETED)
                    .dataCode(saleRequest.getDataCode())
                    .quantity(contractList.stream().mapToLong(Contract::getAmount).sum()) // 합산
                    .remaining(saleRequest.getRemaining())
                    .tradeDate(contractList.stream()
                            .map(Contract::getCreatedAt)
                            .max(LocalDateTime::compareTo).orElse(null)) // 최신 체결시간
                    .pricePerGb(contractList.get(0).getPrice()) // 동일 단가라면 아무거나
                    .phoneNumber(line.getPhoneNumber())
                    .contractDto(buildContractDtosForSale(saleId))
                    .build();

                result.add(dto);
                result.sort(Comparator.comparing(SaleResponseDto::getTradeDate).reversed());

            }
            return result;
    }

    private List<SaleResponseDto> buildWaitingSales(String accountId, LocalDateTime fromDate) {
        List<SaleDataRequest> waitingRequests = saleDataRequestRepository.findWaitingSalesByAccountId(accountId, fromDate);
        List<SaleResponseDto> result = new ArrayList<>();

        for (SaleDataRequest saleRequest : waitingRequests) {
            Long requestedQuantity = saleRequest.getQuantity();
            Long remainingQuantity = calculateRemainingQuantity(saleRequest);

            Line line = findLineById(saleRequest.getLineId());

            SaleResponseDto dto = SaleResponseDto.builder()
                    .saleId(saleRequest.getId())
                    .status(STATUS_WAITING)
                    .dataCode(saleRequest.getDataCode())
                    .quantity(requestedQuantity)
                    .remaining(remainingQuantity)
                    .tradeDate(saleRequest.getCreatedAt())
                    .pricePerGb(saleRequest.getPrice())
                    .phoneNumber(line.getPhoneNumber())
                    .contractDto(buildContractDtosForSale(saleRequest.getId()))
                    .build();

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
                                                          Long requestedQuantity, Long remainingQuantity, LocalDateTime tradeDate,
                                                          Long pricePerGb, String phoneNumber) {
        return PurchaseResponseDto.builder()
                .purchaseId(purchaseId)
                .status(status)
                .dataCode(dataCode)
                .quantity(requestedQuantity)
                .remaining(remainingQuantity)
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

    private List<ContractDto> buildContractDtosForBuy(String buyRequestId) {
        return contractRepository.findByBuyRequestIdOrderByCreatedAtDesc(buyRequestId)
                .stream()
                .map(ContractDto::ofWithoutAccount)
                .toList();
    }

    private List<ContractDto> buildContractDtosForSale(String saleRequestId) {
        return contractRepository.findBySaleRequestIdOrderByCreatedAtDesc(saleRequestId)
                .stream()
                .map(ContractDto::ofWithoutAccount)
                .toList();
    }


}