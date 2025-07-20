package com.ureka.team3.utong_backend.datatrade.service.chart;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;

import java.util.List;

public interface CurrentPriceService {
    List<AvgPerHour> getInitData(String dataCode);
}
