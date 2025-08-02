package com.ureka.team3.utong_backend.datatrade.service.chart.weekly;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.chart.WeeklyChartDto;

public interface WeeklyPriceService {

    ApiResponse<WeeklyChartDto> getWeeklyPrice(String dataCode);

}
