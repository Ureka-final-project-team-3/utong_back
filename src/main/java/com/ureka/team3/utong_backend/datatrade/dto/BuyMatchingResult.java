package com.ureka.team3.utong_backend.datatrade.dto;

import com.ureka.team3.utong_backend.datatrade.enums.BuyMatchingStatus;
import com.ureka.team3.utong_backend.datatrade.enums.SaleMatchingStatus;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class BuyMatchingResult implements MatchingResult {
    private BuyMatchingStatus buyMatchingStatus;
    private List<TradeMatch> matchList;
    private Long request = 0L;
    private Long used;
    private Long remain;
    private String dataCode;
    private Long price;

    public static BuyMatchingResult of(List<TradeMatch> matches, DataTradeDto.DataTradeRequestDto requestDto) {
        long request = requestDto.getDataAmount();
        long used = matches.stream()
                .mapToLong(TradeMatch::getAmount)
                .sum();
        long remain = request-used;

        BuyMatchingStatus status = (remain == 0)
                ? BuyMatchingStatus.ALL_MATCHED
                : BuyMatchingStatus.PART_MATCHED;

        return BuyMatchingResult.builder()
                .buyMatchingStatus(status)
                .matchList(matches)
                .request(request)
                .used(used)
                .remain(remain)
                .dataCode(requestDto.getDataCode())
                .price(requestDto.getPrice())
                .build();
    }

    public static BuyMatchingResult underMinimumPrice(DataTradeDto.DataTradeRequestDto requestDto) {
        return BuyMatchingResult.builder()
                .buyMatchingStatus(BuyMatchingStatus.UNDER_MINIMUM_SALE_PRICE)
                .price(requestDto.getPrice())
                .used(0L)
                .dataCode(requestDto.getDataCode())
                .price(requestDto.getPrice())
                .remain(requestDto.getDataAmount())
                .matchList(null)
                .build();
    }
}
