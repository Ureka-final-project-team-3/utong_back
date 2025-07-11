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
public class TradeEventDto {
    private String contractId;
    private String dataCode; 
    private Long price;
    private Long quantity;
    private LocalDateTime completedAt;
    
    public String getDataTypeName() {
        return "001".equals(dataCode) ? "LTE" : "5G";
    }
}