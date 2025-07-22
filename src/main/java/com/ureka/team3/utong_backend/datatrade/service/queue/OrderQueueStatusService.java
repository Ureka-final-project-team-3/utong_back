package com.ureka.team3.utong_backend.datatrade.service.queue;

import com.ureka.team3.utong_backend.datatrade.dto.OrdersQueueDto;

public interface OrderQueueStatusService {
    OrdersQueueDto getInitData(String dataCode);
}
