package com.ureka.team3.utong_backend.datatrade.dto;

import com.ureka.team3.utong_backend.datatrade.enums.SaleMatchingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SaleMatchingResult implements MatchingResult{
    private SaleMatchingStatus saleMatchingStatus;
    private List<TradeMatch> matchList;
    private Long request;
    private Long used;
    private Long remain;
    private String dataCode;
    private Long price;

    public static SaleMatchingResult of(List<TradeMatch> matches, DataTradeDto.DataTradeRequestDto requestDto) {
        long request = requestDto.getDataAmount();
        long used = matches.stream()
                .mapToLong(TradeMatch::getAmount)
                .sum();

        long remain = request - used;

        SaleMatchingStatus status = (remain == 0)
                ? SaleMatchingStatus.ALL_MATCHED
                : SaleMatchingStatus.PART_MATCHED;

        return SaleMatchingResult.builder()
                .saleMatchingStatus(status)
                .matchList(matches)
                .request(request)
                .used(used)
                .remain(remain)
                .dataCode(requestDto.getDataCode())
                .price(requestDto.getPrice())
                .build();
    }

    public static SaleMatchingResult overMaxPurchasePrice(DataTradeDto.DataTradeRequestDto requestDto) {
        return SaleMatchingResult.builder()
                .saleMatchingStatus(SaleMatchingStatus.OVER_MAX_PURCHASE_PRICE)
                .price(requestDto.getPrice())
                .used(0L)
                .dataCode(requestDto.getDataCode())
                .price(requestDto.getPrice())
                .remain(requestDto.getDataAmount())
                .matchList(null)
                .build();
    }

}
