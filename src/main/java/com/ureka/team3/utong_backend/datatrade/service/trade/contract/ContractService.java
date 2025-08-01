package com.ureka.team3.utong_backend.datatrade.service.trade.contract;

import com.ureka.team3.utong_backend.datatrade.dto.trade.TradeExecutionDto;
import com.ureka.team3.utong_backend.datatrade.domain.entity.Contract;

public interface ContractService {
    Contract save(TradeExecutionDto tradeExecutionDto);
}
