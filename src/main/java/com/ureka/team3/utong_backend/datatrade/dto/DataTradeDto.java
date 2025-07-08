package com.ureka.team3.utong_backend.datatrade.dto;

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
}
