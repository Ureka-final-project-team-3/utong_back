package com.ureka.team3.utong_backend.datatrade.utils;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.BuyMatchingResult;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.SaleMatchingResult;
import com.ureka.team3.utong_backend.datatrade.enums.BuyOrderResult;
import com.ureka.team3.utong_backend.datatrade.enums.SaleOrderResult;

public class TradeResponseFactory {

    public static ApiResponse needDefaultLine() {
        return ApiResponse.success("데이터 거래에 필요한 기본 회선 선택 필수",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.NEED_DEFAULT_LINE)
                        .build());
    }

    public static ApiResponse unlimitedBuyNotAllowed() {
        return ApiResponse.success("무제한 요금제는 데이터를 구매할 수 없습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.BORDERLESS)
                        .build());
    }

    public static ApiResponse unlimitedSaleNotAllowed() {
        return ApiResponse.success("무제한 요금제는 데이터를 판매할 수 없습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.BORDERLESS)
                        .build());
    }

    public static ApiResponse existSaleRequest() {
        return ApiResponse.success("이미 판매 대기중인 데이터가 있습니다. 취소 후 다시 이용해주세요",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.EXIST_SALE_REQUEST)
                        .build());
    }

    public static ApiResponse existBuyRequest() {
        return ApiResponse.success("이미 구매 대기중인 데이터가 있습니다. 취소 후 다시 이용해주세요",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.EXIST_BUY_REQUEST)
                        .build());
    }

    public static ApiResponse insufficientPoint() {
        return ApiResponse.success("포인트 부족",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.INSUFFICIENT_POINT)
                        .build());
    }

    public static ApiResponse exceedSaleLimit() {
        return ApiResponse.success("판매 요청한 데이터가 판매 가능량을 초과했습니다.",
                DataTradeDto.SaleDataResponseDto.builder()
                        .result(SaleOrderResult.EXCEED_SALE_LIMIT)
                        .build());
    }

    public static ApiResponse successPurchaseComplete() {
        return ApiResponse.success("데이터 구매 완료",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.ALL_COMPLETE)
                        .build());
    }

    public static ApiResponse successPurchasePartComplete(BuyMatchingResult buyMatchingResult) {
        return ApiResponse.success("일부 데이터만 구매 완료",
                DataTradeDto.BuyDataResponseDto.builder()
                        .result(BuyOrderResult.PART_COMPLETE)
                        .remainData(buyMatchingResult.getRemain())
                        .build());
    }

    public static ApiResponse waitingPurchase() {
        return ApiResponse.success("입력한 가격이 최고 구매가보다 높아 예약 판매로 등록되었습니다.",
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
