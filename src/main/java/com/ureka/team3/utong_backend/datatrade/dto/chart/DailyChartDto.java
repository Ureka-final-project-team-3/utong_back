package com.ureka.team3.utong_backend.datatrade.dto.chart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyChartDto {

    private LocalDate date;

    private Long avgPrice;

}
