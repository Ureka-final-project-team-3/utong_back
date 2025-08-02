package com.ureka.team3.utong_backend.datatrade.service.trade.queue;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.trade.OrdersQueueDto;

import java.util.List;

public interface QueueStatusService {
    OrdersQueueDto getInitData(String dataCode);

    List<OrdersQueueDto> getAllInitData();

    ApiResponse getCurrentAllQueue();
}
