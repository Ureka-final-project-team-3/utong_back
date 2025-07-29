package com.ureka.team3.utong_backend.datatrade.service.queue;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.OrdersQueueDto;

import java.util.List;

public interface OrderQueueStatusService {
    OrdersQueueDto getInitData(String dataCode);

    List<OrdersQueueDto> getAllInitData();

    ApiResponse getCurrentAllQueue();
}
