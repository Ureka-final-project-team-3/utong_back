package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.datatrade.dto.TradeExecutionDto;
import com.ureka.team3.utong_backend.datatrade.entity.Contract;

public interface ContractService {
    Contract save(TradeExecutionDto tradeExecutionDto);
}
