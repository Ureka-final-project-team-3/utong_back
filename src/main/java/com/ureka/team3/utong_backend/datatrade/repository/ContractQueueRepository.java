package com.ureka.team3.utong_backend.datatrade.repository;

import com.ureka.team3.utong_backend.datatrade.dto.trade.ContractDto;

import java.util.List;

public interface ContractQueueRepository {

    List<ContractDto> getAllCachedContracts(String dataCode);

    ContractDto getRecentContract(String dataCode);
}
