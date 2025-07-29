package com.ureka.team3.utong_backend.datatrade.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersQueueDto {
    @Setter
    private String dataCode;
    private Map<Long,Long> buyOrderQuantity;
    private Map<Long,Long> sellOrderQuantity;
    private List<ContractDto> recentContracts;
}
