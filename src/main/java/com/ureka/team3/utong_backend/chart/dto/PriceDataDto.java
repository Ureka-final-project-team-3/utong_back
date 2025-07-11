package com.ureka.team3.utong_backend.chart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceDataDto {
    private String timestamp;
    private Long price;     
    private Integer volume;  
    
    public static PriceDataDto of(LocalDateTime timestamp, Long price, Integer volume) {
        return PriceDataDto.builder()
                .timestamp(timestamp.toString())
                .price(price)
                .volume(volume)
                .build();
    }
}