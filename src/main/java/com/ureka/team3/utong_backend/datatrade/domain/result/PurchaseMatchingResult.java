package com.ureka.team3.utong_backend.datatrade.domain.result;

import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.enums.PurchaseMatchingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PurchaseMatchingResult implements MatchingResult {
    private PurchaseMatchingStatus purchaseMatchingStatus;
    private List<TradeMatch> matchList;
    private Long request = 0L;
    private Long used;
    private Long remain;
    private String dataCode;
    private Long price;

    public static PurchaseMatchingResult of(List<TradeMatch> matches, DataTradeDto.DataTradeRequestDto requestDto) {
        long request = requestDto.getDataAmount();
        long used = matches.stream()
                .mapToLong(TradeMatch::getAmount)
                .sum();
        long remain = request-used;

        PurchaseMatchingStatus status = (remain == 0)
                ? PurchaseMatchingStatus.ALL_MATCHED
                : PurchaseMatchingStatus.PART_MATCHED;

        return PurchaseMatchingResult.builder()
                .purchaseMatchingStatus(status)
                .matchList(matches)
                .request(request)
                .used(used)
                .remain(remain)
                .dataCode(requestDto.getDataCode())
                .price(requestDto.getPrice())
                .build();
    }

    public static PurchaseMatchingResult underMinimumPrice(DataTradeDto.DataTradeRequestDto requestDto) {
        return PurchaseMatchingResult.builder()
                .purchaseMatchingStatus(PurchaseMatchingStatus.UNDER_MINIMUM_SALE_PRICE)
                .price(requestDto.getPrice())
                .used(0L)
                .dataCode(requestDto.getDataCode())
                .price(requestDto.getPrice())
                .remain(requestDto.getDataAmount())
                .matchList(null)
                .build();
    }
}
