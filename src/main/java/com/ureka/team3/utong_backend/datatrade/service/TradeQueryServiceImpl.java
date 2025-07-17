package com.ureka.team3.utong_backend.datatrade.service;

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
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeQueryServiceImpl implements TradeQueryService {

    private final ContractRepository contractRepository;
    private final BuyDataRequestRepository buyRepo;
    private final SaleDataRequestRepository saleRepo;
    private final LineRepository lineRepository;

    private LocalDateTime calculateFromDate(String range) {
        LocalDateTime now = LocalDateTime.now();
        return switch (range != null ? range : "WEEK") {
            case "MONTH" -> now.minusMonths(1);
            case "YEAR" -> now.minusYears(1);
            default -> now.minusDays(7);
        };
    }

    @Override
    public ApiResponse getMyPurchases(Account account, TradeHistoryRequestDto requestDto) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());

        List<PurchaseResponseDto> completePurchases = getCompletePurchases(accountId, fromDate);
        List<PurchaseResponseDto> waitingPurchases = getWaitingPurchases(accountId, fromDate);

        PurchaseHistoryResponseDto result = PurchaseHistoryResponseDto.builder()
                .completePurchases(completePurchases)
                .waitingPurchases(waitingPurchases)
                .build();

        return ApiResponse.success(result);
    }

    private List<PurchaseResponseDto> getCompletePurchases(String accountId, LocalDateTime fromDate) {
        List<PurchaseResponseDto> result = new ArrayList<>();
        for (Contract c : contractRepository.findCompletedPurchasesByAccountId(accountId, fromDate)) {
            Line line = lineRepository.findById(c.getBuyDataRequest().getLineId()).orElseThrow(LineNotFoundException::new);
            result.add(PurchaseResponseDto.builder()
                    .purchaseId(c.getBuyDataRequest().getId())
                    .status("001") // 거래완료
                    .dataCode(c.getBuyDataRequest().getDataCode())
                    .quantity(c.getAmount())
                    .tradeDate(c.getCreatedAt())
                    .pricePerGb(c.getPrice())
                    .phoneNumber(line.getPhoneNumber())
                    .build());
        }
        return result;
    }

    private List<PurchaseResponseDto> getWaitingPurchases(String accountId, LocalDateTime fromDate) {
        List<PurchaseResponseDto> result = new ArrayList<>();

        for (BuyDataRequest buyDataRequest : buyRepo.findWaitingPurchasesByAccountId(accountId, fromDate)) {
            Long quantity = buyDataRequest.getQuantity();
            if (buyDataRequest.getStatus().equals("002"))
                quantity = buyDataRequest.getRemaining();
            Line line = lineRepository.findById(buyDataRequest.getLineId()).orElseThrow(LineNotFoundException::new);
            result.add(PurchaseResponseDto.builder()
                    .purchaseId(buyDataRequest.getId())
                    .status("003") // 대기 중
                    .dataCode(buyDataRequest.getDataCode())
                    .quantity(quantity)
                    .tradeDate(buyDataRequest.getCreatedAt())
                    .pricePerGb(buyDataRequest.getPrice())
                    .phoneNumber(line.getPhoneNumber())
                    .build());
        }
        return result;
    }

    @Override
    public ApiResponse getMySales(Account account, TradeHistoryRequestDto requestDto) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(requestDto.getRange());

        List<SaleResponseDto> completeSales = getCompleteSales(accountId, fromDate);
        List<SaleResponseDto> waitingSales = getWaitingSales(accountId, fromDate);

        SaleHistoryResponseDto result = SaleHistoryResponseDto.builder()
                .completeSales(completeSales)
                .waitingSales(waitingSales)
                .build();

        return ApiResponse.success(result);
    }

    private List<SaleResponseDto> getCompleteSales(String accountId, LocalDateTime fromDate) {
        List<SaleResponseDto> result = new ArrayList<>();
        for (Contract c : contractRepository.findCompletedSalesByAccountId(accountId, fromDate)) {
            Line line = lineRepository.findById(c.getSaleDataRequest().getLineId()).orElseThrow(LineNotFoundException::new);
            result.add(SaleResponseDto.builder()
                    .saleId(c.getBuyDataRequest().getId())
                    .status("001") // 거래완료
                    .dataCode(c.getBuyDataRequest().getDataCode())
                    .quantity(c.getAmount())
                    .tradeDate(c.getCreatedAt())
                    .pricePerGb(c.getPrice())
                    .phoneNumber(line.getPhoneNumber())
                    .build());
        }
        return result;
    }

    private List<SaleResponseDto> getWaitingSales(String accountId, LocalDateTime fromDate) {
        List<SaleResponseDto> result = new ArrayList<>();

        for (SaleDataRequest saleDataRequest : saleRepo.findWaitingSalesByAccountId(accountId, fromDate)) {
            Long quantity = saleDataRequest.getQuantity();
            if (saleDataRequest.getStatus().equals("002"))
                quantity = saleDataRequest.getRemaining();
            Line line = lineRepository.findById(saleDataRequest.getLineId()).orElseThrow(LineNotFoundException::new);
            result.add(SaleResponseDto.builder()
                    .saleId(saleDataRequest.getId())
                    .status("003") // 대기 중
                    .dataCode(saleDataRequest.getDataCode())
                    .quantity(quantity)
                    .tradeDate(saleDataRequest.getCreatedAt())
                    .pricePerGb(saleDataRequest.getPrice())
                    .phoneNumber(line.getPhoneNumber())
                    .build());
        }
        return result;
    }
}
