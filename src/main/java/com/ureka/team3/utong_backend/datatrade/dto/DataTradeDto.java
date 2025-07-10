package com.ureka.team3.utong_backend.datatrade.dto;

import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DataTradeDto {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuyDataRequestDto {
        private long dataAmount;
        private long price;
        private String dataCode;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleDataRequestDto {
        private long dataAmount;
        private long price;
        private String  dataCode;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuyDataResponseDto<T> {
        private BuyOrderResult result;
        private long remainData;
        private T data;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleDataResponseDto<T> {
        private BuyOrderResult result;
        private long remainData;
        private T data;
    }
}
