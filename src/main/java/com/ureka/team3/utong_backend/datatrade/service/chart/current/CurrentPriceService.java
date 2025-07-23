package com.ureka.team3.utong_backend.datatrade.service.chart.current;

import com.ureka.team3.utong_backend.datatrade.dto.ChartDataDto;

import java.util.List;

public interface CurrentPriceService {
    ChartDataDto getInitData(String dataCode);

    List<ChartDataDto> getAllInitData();
}
