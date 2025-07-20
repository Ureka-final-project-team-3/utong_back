package com.ureka.team3.utong_backend.datatrade.service.chart;

import java.time.LocalDateTime;

public interface CurrentPriceService {

    void updateRedisCache(LocalDateTime aggregatedAt);

    void broadCastToSseClients();
}
