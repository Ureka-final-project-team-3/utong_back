package com.ureka.team3.utong_backend.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradePriceResponse {
    private String dataType;
    private List<PriceDataDto> priceData;
    
    public static TradePriceResponse of(String dataType, List<PriceDataDto> priceData) {
        return TradePriceResponse.builder()
                .dataType(dataType)
                .priceData(priceData)
                .build();
    }
}