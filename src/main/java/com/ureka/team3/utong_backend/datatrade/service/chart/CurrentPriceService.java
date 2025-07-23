package com.ureka.team3.utong_backend.datatrade.service.chart;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.dto.ChartDataDto;
import org.apache.kafka.common.metrics.stats.Avg;

import java.util.List;

public interface CurrentPriceService {
    ChartDataDto getInitData(String dataCode);

    List<ChartDataDto> getAllInitData();
}
