package com.ureka.team3.utong_backend.datatrade.utils;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.domain.result.PurchaseMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.trade.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;

import static com.ureka.team3.utong_backend.common.exception.ErrorCode.*;

public class TradeResponseFactory {

    public static ApiResponse needDefaultLine() {
        return ApiResponse.fail(NEED_DEFAULT_LINE);
    }

    public static ApiResponse unlimitedTradeNotAllowed() {
        return ApiResponse.fail(BORDERLESS);
    }

    public static ApiResponse existSaleRequest() {
        return ApiResponse.fail(EXIST_SALE_REQUEST);
    }

    public static ApiResponse existBuyRequest() {
        return ApiResponse.fail(EXIST_PURCHASE_REQUEST);
    }

    public static ApiResponse insufficientPoint() {
        return ApiResponse.fail(INSUFFICIENT_POINT);
    }

    public static ApiResponse exceedSaleLimit() {
        return ApiResponse.fail(EXCEED_SALE_LIMIT);
    }

    public static ApiResponse successPurchaseComplete() {
        return ApiResponse.success("데이터 구매 완료",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.ALL_COMPLETE)
                        .build());
    }

    public static ApiResponse successPurchasePartComplete(PurchaseMatchingResult purchaseMatchingResult) {
        return ApiResponse.success("일부 데이터만 구매 완료",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.PART_COMPLETE)
                        .remainData(purchaseMatchingResult.getRemain())
                        .build());
    }

    public static ApiResponse waitingPurchase() {
        return ApiResponse.success("입력한 가격이 최저 판매가보다 낮아 예약 구매로 등록되었습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.WAITING)
                        .build());
    }

    public static ApiResponse successSaleComplete(SaleMatchingResult saleMatchingResult) {
        return ApiResponse.success("데이터 판매 성공",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.ALL_COMPLETE)
                        .remainData(saleMatchingResult.getRemain())
                        .build());
    }

    public static ApiResponse successSalePartComplete(SaleMatchingResult saleMatchingResult) {
        return ApiResponse.success("일부 데이터만 판매 완료",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.PART_COMPLETE)
                        .remainData(saleMatchingResult.getRemain())
                        .build());
    }

    public static ApiResponse waitingSale() {
        return ApiResponse.success("입력한 가격이 최고 구매가보다 높아 예약 판매로 등록되었습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.WAITING)
                        .build());
    }

}
