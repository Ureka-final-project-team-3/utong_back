package com.ureka.team3.utong_backend.datatrade.service.chart;

import com.ureka.team3.utong_backend.common.entity.Code;
import com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy;
import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.entity.ContractHourlyAvgPrice;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRedisRepository;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.ureka.team3.utong_backend.datatrade.config.DataTradePolicy.CHART_LIST_SIZE;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentPriceServiceImpl implements CurrentPriceService {

    private final SseService sseService;
    private final ContractHourlyAvgPriceRepository contractHourlyAvgPriceRepository;
    private final ContractHourlyAvgPriceRedisRepository contractHourlyAvgPriceRedisRepository;
    private final DataTradePolicy dataTradePolicy;

    @Override
    public void updateRedisCache(LocalDateTime aggregatedAt) {
        for (String dataCode : getDataTypeCodes()) {
            updateByDataCode(dataCode, aggregatedAt);
        }
    }

    private void updateByDataCode(String dataCode, LocalDateTime aggregatedAt) {
        ContractHourlyAvgPrice latest = contractHourlyAvgPriceRepository
                .findLatestByDataCodeBeforeTime(dataCode, aggregatedAt, 1)
                .get(0);

        AvgPerHour avgPerHour = AvgPerHour.of(latest);
        contractHourlyAvgPriceRedisRepository.addDataWithSizeLimit(dataCode, avgPerHour, CHART_LIST_SIZE);
    }

    @Override
    public void broadCastToSseClients() {
        for (String dataCode : getDataTypeCodes()) {
            List<AvgPerHour> dataList = contractHourlyAvgPriceRedisRepository.getAllData(dataCode);
            sseService.broadcastForCurrentPrice(dataList);
        }
    }

    @PostConstruct
    public void init() {
        log.info("레디스 캐시 초기화 : 최근 {}시간 평균가 레디스 저장", CHART_LIST_SIZE);

        for (String dataCode : getDataTypeCodes()) {
            List<AvgPerHour> sortedList = contractHourlyAvgPriceRepository
                    .findLatestByDataCodeBeforeTime(dataCode, LocalDateTime.now(), CHART_LIST_SIZE).stream()
                    .map(AvgPerHour::of)
                    .sorted(Comparator.comparing(AvgPerHour::getAggregatedAt))
                    .toList();

            contractHourlyAvgPriceRedisRepository.initializeData(dataCode, sortedList);
        }
    }

    private List<String> getDataTypeCodes() {
        return dataTradePolicy.getDataTypeCodeList().stream()
                .map(Code::getCode)
                .toList();
    }
}
