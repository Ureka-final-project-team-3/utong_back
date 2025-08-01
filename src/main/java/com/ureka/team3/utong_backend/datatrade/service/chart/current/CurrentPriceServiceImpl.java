package com.ureka.team3.utong_backend.datatrade.service.chart.current;

import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.chart.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.dto.chart.ChartDataDto;
import com.ureka.team3.utong_backend.datatrade.repository.cache.ContractHourlyAvgPriceRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentPriceServiceImpl implements CurrentPriceService {

    private final ContractHourlyAvgPriceRedisRepository contractHourlyAvgPriceRedisRepository;
    private final DataTradePolicy dataTradePolicy;

    @Override
    public ChartDataDto getInitData(String dataCode) {
        return ChartDataDto.builder()
                .dataCode(dataCode)
                .avgPerHourList(contractHourlyAvgPriceRedisRepository.getAllData(dataCode))
                .build();
    }

    @Override
    public List<ChartDataDto> getAllInitData() {
        List<ChartDataDto> chartDataDtoList = new ArrayList<>();

        for(Code code : dataTradePolicy.getDataTypeCodeList()) {
            String dataCode = code.getCode();
            List<AvgPerHour> avgPerHourList = contractHourlyAvgPriceRedisRepository.getAllData(dataCode);

            ChartDataDto chartDataDto = ChartDataDto.builder()
                    .dataCode(dataCode)
                    .avgPerHourList(avgPerHourList)
                    .build();

            chartDataDtoList.add(chartDataDto);
        }

        return chartDataDtoList;
    }
}
