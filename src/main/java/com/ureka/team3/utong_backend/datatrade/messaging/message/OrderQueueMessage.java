package com.ureka.team3.utong_backend.datatrade.messaging.message;

import com.ureka.team3.utong_backend.datatrade.dto.trade.OrdersQueueDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderQueueMessage {
    private String status;

    private LocalDateTime publishedAt;

    private String message;

    private Map<String, OrdersQueueDto> dataMap;

}
