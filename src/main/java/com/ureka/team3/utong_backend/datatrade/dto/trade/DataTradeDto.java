package com.ureka.team3.utong_backend.datatrade.dto.trade;

import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class DataTradeDto {
//    @Getter
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class BuyDataRequestDto {
//        private long dataAmount;
//        private long price;
//        private String dataCode;
//    }
//
//    @Getter
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class SaleDataRequestDto {
//        private long dataAmount;
//        private long price;
//        private String dataCode;
//    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataTradeRequestDto {
        private long dataAmount;
        private long price;
        private String dataCode;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuyDataResponseDto {
        private BuyOrderResult result;
        private long remainData;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SaleDataResponseDto {
        private SaleOrderResult result;
        private long remainData;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CancelWaitingTradeRequestDto {
        private String orderId;
    }


}
