package com.ureka.team3.utong_backend.datatrade.messaging.publisher.dto;

import com.ureka.team3.utong_backend.datatrade.dto.trade.ContractDto;
import com.ureka.team3.utong_backend.datatrade.domain.result.TradeMatch;
import com.ureka.team3.utong_backend.datatrade.enums.RequestType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TradeExecutedMessage {
    private String dataCode;
    private String requestOrderId;
    private RequestType requestType;
    private Long remain;
    private Long requestPrice;
    private List<TradeMatch> matchedList;
    private List<ContractDto> newContracts;
}
