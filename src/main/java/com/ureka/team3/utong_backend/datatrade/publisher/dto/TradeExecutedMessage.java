package com.ureka.team3.utong_backend.datatrade.publisher.dto;

import com.ureka.team3.utong_backend.datatrade.dto.ContractDto;
import com.ureka.team3.utong_backend.datatrade.dto.TradeMatch;
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
