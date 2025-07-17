package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.datatrade.dto.MyDataPurchaseDto;
import com.ureka.team3.utong_backend.datatrade.dto.MyDataSaleDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyTradeServiceImpl implements MyTradeService {

    private final ContractRepository contractRepository;
    private final BuyDataRequestRepository buyRepo;
    private final SaleDataRequestRepository saleRepo;

    private LocalDateTime calculateFromDate(String range) {
        LocalDateTime now = LocalDateTime.now();
        return switch (range != null ? range : "WEEK") {
            case "MONTH" -> now.minusMonths(1);
            case "YEAR" -> now.minusYears(1);
            default -> now.minusDays(7);
        };
    }

    @Override
    public List<MyDataPurchaseDto> getMyPurchases(Account account, String range) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(range);

        List<MyDataPurchaseDto> result = new ArrayList<>();

        for (Contract c : contractRepository.findCompletedPurchasesByAccountId(accountId, fromDate)) {
            result.add(MyDataPurchaseDto.builder()
                    .purchaseId(c.getBuyDataRequest().getId())
                    .tradeStatus("001") // 거래완료
                    .dataType(c.getBuyDataRequest().getDataCode())
                    .quantity(c.getAmount())
                    .tradeDate(c.getCreatedAt())
                    .pricePerGb(c.getPrice())
                    .build());
        }

        for (BuyDataRequest b : buyRepo.findWaitingPurchasesByAccountId(accountId, fromDate)) {
            result.add(MyDataPurchaseDto.builder()
                    .purchaseId(b.getId())
                    .tradeStatus("002") // 거래대기
                    .dataType(b.getDataCode())
                    .quantity(b.getQuantity())
                    .tradeDate(b.getCreatedAt())
                    .pricePerGb(b.getPrice())
                    .build());
        }

        return result;
    }

    @Override
    public List<MyDataSaleDto> getMySales(Account account, String range) {
        String accountId = account.getId();
        LocalDateTime fromDate = calculateFromDate(range);

        List<MyDataSaleDto> result = new ArrayList<>();

        for (Contract c : contractRepository.findCompletedSalesByAccountId(accountId, fromDate)) {
            result.add(MyDataSaleDto.builder()
                    .saleId(c.getSaleDataRequest().getId())
                    .Status("001") // 거래완료
                    .dataCode(c.getSaleDataRequest().getDataCode())
                    .quantity(c.getAmount())
                    .tradeDate(c.getCreatedAt())
                    .pricePerGb(c.getPrice())
                    .build());
        }

        for (SaleDataRequest s : saleRepo.findWaitingSalesByAccountId(accountId, fromDate)) {
            result.add(MyDataSaleDto.builder()
                    .saleId(s.getId())
                    .Status("002") // 거래대기
                    .dataCode(s.getDataCode())
                    .quantity(s.getQuantity())
                    .tradeDate(s.getCreatedAt())
                    .pricePerGb(s.getPrice())
                    .build());
        }

        return result;
    }
}
