package com.ureka.team3.utong_backend.datatrade.dto;

import com.ureka.team3.utong_backend.datatrade.entity.ContractHourlyAvgPrice;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AvgPerHour {

    @Setter
    private String dataCode;

    private Long avgPrice;

    private LocalDateTime aggregatedAt;

    public static AvgPerHour of(ContractHourlyAvgPrice contractHourlyAvgPrice) {
        AvgPerHour avgPerHour = new AvgPerHour();

        avgPerHour.dataCode = contractHourlyAvgPrice.getDataCode();
        avgPerHour.avgPrice = contractHourlyAvgPrice.getAvgPrice();
        avgPerHour.aggregatedAt = contractHourlyAvgPrice.getAggregatedAt();

        return avgPerHour;
    }

}
