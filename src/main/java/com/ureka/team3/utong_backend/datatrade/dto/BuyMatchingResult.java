package com.ureka.team3.utong_backend.datatrade.dto;

import com.ureka.team3.utong_backend.datatrade.enums.BuyMatchingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BuyMatchingResult {
    private BuyMatchingStatus buyMatchingStatus;
    private List<PurchaseMatch> matchList;
    private Long remain;

    public static BuyMatchingResult of(List<PurchaseMatch> matches, long remain) {
        BuyMatchingStatus status = (remain == 0)
                ? BuyMatchingStatus.ALL_MATCHED
                : BuyMatchingStatus.PART_MATCHED;

        return BuyMatchingResult.builder()
                .buyMatchingStatus(status)
                .matchList(matches)
                .remain(remain)
                .build();
    }

    public static BuyMatchingResult underMinimumPrice(long remain) {
        return BuyMatchingResult.builder()
                .buyMatchingStatus(BuyMatchingStatus.UNDER_MINIMUM_SALE_PRICE)
                .remain(remain)
                .build();
    }

}
