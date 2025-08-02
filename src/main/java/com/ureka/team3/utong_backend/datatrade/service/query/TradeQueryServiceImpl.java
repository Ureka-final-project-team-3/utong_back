package com.ureka.team3.utong_backend.datatrade.service.query;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.business.LineNotFoundException;
import com.ureka.team3.utong_backend.datatrade.domain.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.domain.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.domain.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.dto.query.ContractResponseDto;
import com.ureka.team3.utong_backend.datatrade.dto.query.PurchaseResponseDto;
import com.ureka.team3.utong_backend.datatrade.dto.query.SaleResponseDto;
import com.ureka.team3.utong_backend.datatrade.dto.trade.TradeHistoryRequestDto;
import com.ureka.team3.utong_backend.datatrade.repository.perman.ContractRepository;
import com.ureka.team3.utong_backend.datatrade.repository.perman.PurchaseRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.perman.SaleRequestRepository;
import com.ureka.team3.utong_backend.datatrade.utils.TradeCalculator;
import com.ureka.team3.utong_backend.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TradeQueryServiceImpl implements TradeQueryService {

    private final ContractRepository contractRepository;
    private final PurchaseRequestRepository purchaseRequestRepository;
    private final SaleRequestRepository saleRequestRepository;
    private final LineRepository lineRepository;
    private final TradeCalculator tradeCalculator;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ApiResponse getMyPurchases(Account account, TradeHistoryRequestDto requestDto) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());

        List<BuyDataRequest> purchaseRequests = purchaseRequestRepository.findPurchaseRequestsByAccountId(accountId, fromDate);
        Map<String, String> lineMap = preloadLineNumbers(purchaseRequests.stream().map(BuyDataRequest::getLineId).collect(Collectors.toSet()));

        List<PurchaseResponseDto> purchases = purchaseRequests.stream()
                .map(req -> buildPurchaseResponse(req, lineMap.get(req.getLineId())))
                .toList();

        return ApiResponse.success(purchases);
    }

    @Override
    public ApiResponse getMySales(Account account, TradeHistoryRequestDto requestDto) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());

        List<SaleDataRequest> saleRequests = saleRequestRepository.findSaleRequestsByAccountId(accountId, fromDate);
        Map<String, String> lineMap = preloadLineNumbers(saleRequests.stream().map(SaleDataRequest::getLineId).collect(Collectors.toSet()));

        List<SaleResponseDto> sales = saleRequests.stream()
                .map(req -> buildSaleResponse(req, lineMap.get(req.getLineId())))
                .toList();

        return ApiResponse.success(sales);
    }

    /**
     * 계약 리스트를 [시간_가격] 기준으로 그룹핑
     */
    private Map<String, ContractResponseDto> groupContracts(List<Contract> contracts) {
        Map<String, ContractResponseDto> grouped = new HashMap<>();
        for (Contract contract : contracts) {
            String key = contract.getCreatedAt().format(FORMATTER) + "_" + contract.getPrice();

            grouped.compute(key, (k, v) -> {
                if (v == null) {
                    return ContractResponseDto.builder()
                            .contractDate(contract.getCreatedAt())
                            .pricePerUnit(contract.getPrice())
                            .contractQuantity(contract.getAmount())
                            .build();
                } else {
                    v.setContractQuantity(v.getContractQuantity() + contract.getAmount());
                    return v;
                }
            });
        }
        return grouped;
    }

    /**
     * 라인 번호 미리 로딩
     */
    private Map<String, String> preloadLineNumbers(Set<String> lineIds) {
        Map<String, String> lineMap = new HashMap<>();
        for (String lineId : lineIds) {
            String phoneNumber = lineRepository.findById(lineId)
                    .orElseThrow(LineNotFoundException::new)
                    .getPhoneNumber();
            lineMap.put(lineId, phoneNumber);
        }
        return lineMap;
    }

    private PurchaseResponseDto buildPurchaseResponse(BuyDataRequest request, String phoneNumber) {
        List<Contract> contracts = request.getContracts();
        List<ContractResponseDto> grouped = new ArrayList<>(groupContracts(contracts).values());

        long totalPay = grouped.stream()
                .mapToLong(dto -> dto.getPricePerUnit() * dto.getContractQuantity())
                .sum();

        return PurchaseResponseDto.builder()
                .purchaseId(request.getId())
                .phoneNumber(phoneNumber)
                .pricePerGb(request.getPrice())
                .quantity(request.getQuantity())
                .status(request.getStatus())
                .dataCode(request.getDataCode())
                .contractDto(grouped)
                .remaining(request.getRemaining())
                .requestDate(request.getCreatedAt())
                .totalPay(totalPay)
                .build();
    }

    private SaleResponseDto buildSaleResponse(SaleDataRequest request, String phoneNumber) {
        List<Contract> contracts = request.getContracts();
        List<ContractResponseDto> grouped = new ArrayList<>(groupContracts(contracts).values());

        long totalPay = grouped.stream()
                .mapToLong(dto -> tradeCalculator.calculateTotalIncomeForSeller(dto.getPricePerUnit(), dto.getContractQuantity()))
                .sum();

        return SaleResponseDto.builder()
                .saleId(request.getId())
                .phoneNumber(phoneNumber)
                .pricePerGb(request.getPrice())
                .quantity(request.getQuantity())
                .status(request.getStatus())
                .dataCode(request.getDataCode())
                .contractDto(grouped)
                .remaining(request.getRemaining())
                .tradeDate(request.getCreatedAt())
                .totalPay(totalPay)
                .build();
    }

    private LocalDateTime calculateFromDate(String range) {
        LocalDateTime now = LocalDateTime.now();
        return switch (Optional.ofNullable(range).orElse("ALL")) {
            case "TODAY" -> now.toLocalDate().atStartOfDay();
            case "WEEK" -> now.minusDays(7);
            case "MONTH" -> now.minusMonths(1);
            case "YEAR" -> now.minusYears(1);
            default -> LocalDateTime.of(2000, 1, 1, 0, 0);
        };
    }
}
