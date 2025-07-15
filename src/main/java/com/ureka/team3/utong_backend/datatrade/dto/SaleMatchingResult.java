package com.ureka.team3.utong_backend.datatrade.dto;

import com.ureka.team3.utong_backend.datatrade.enums.SaleMatchingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SaleMatchingResult {
    private SaleMatchingStatus saleMatchingStatus;
    private List<SaleMatch> matchList;
    private Long remain;

    public static SaleMatchingResult of(List<SaleMatch> matches, long remain) {
        SaleMatchingStatus status = (remain == 0)
                ? SaleMatchingStatus.ALL_MATCHED
                : SaleMatchingStatus.PART_MATCHED;

        return SaleMatchingResult.builder()
                .saleMatchingStatus(status)
                .matchList(matches)
                .remain(remain)
                .build();
    }

    public static SaleMatchingResult overMaxPurchasePrice(long remain) {
        return SaleMatchingResult.builder()
                .saleMatchingStatus(SaleMatchingStatus.OVER_MAX_PURCHASE_PRICE)
                .remain(remain)
                .build();
    }

}
