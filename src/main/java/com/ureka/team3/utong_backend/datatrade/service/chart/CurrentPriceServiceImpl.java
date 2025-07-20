package com.ureka.team3.utong_backend.datatrade.service.chart;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentPriceServiceImpl implements CurrentPriceService {
    private final ContractHourlyAvgPriceRedisRepository contractHourlyAvgPriceRedisRepository;

    @Override
    public List<AvgPerHour> getInitData(String dataCode) {
        return contractHourlyAvgPriceRedisRepository.getAllData(dataCode);
    }
}
