package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.datatrade.dto.AvgPerHour;
import com.ureka.team3.utong_backend.datatrade.entity.ContractHourlyAvgPrice;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRedisRepository;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrentPriceServiceImpl implements CurrentPriceService {

    private final SseService sseService;
    private final ContractHourlyAvgPriceRepository contractHourlyAvgPriceRepository;
    private final ContractHourlyAvgPriceRedisRepository contractHourlyAvgPriceRedisRepository;

    private static final int MAX_REDIS_LIST_SIZE = 8;


    @Override
    public void updateRedisCache(LocalDateTime aggregatedAt) {
        // LTE
        ContractHourlyAvgPrice avgLtePrice =
                contractHourlyAvgPriceRepository.
                        findLatestByDataCodeBeforeTime("001", aggregatedAt, 1).get(0);

        AvgPerHour avgLtePerHour = AvgPerHour.of(avgLtePrice);

        contractHourlyAvgPriceRedisRepository.addDataWithSizeLimit("001", avgLtePerHour,  MAX_REDIS_LIST_SIZE);

        // 5G
        ContractHourlyAvgPrice avg5gPrice =
                contractHourlyAvgPriceRepository
                        .findLatestByDataCodeBeforeTime("002", aggregatedAt, 1).get(0);

        AvgPerHour avg5gPerHour = AvgPerHour.of(avg5gPrice);

        contractHourlyAvgPriceRedisRepository.addDataWithSizeLimit("002", avg5gPerHour,  MAX_REDIS_LIST_SIZE);
    }

    @Override
    public void broadCastToSseClients() {
        List<AvgPerHour> avgLtePerHourList = contractHourlyAvgPriceRedisRepository.getAllData("001");
        List<AvgPerHour> avg5gPerHourList = contractHourlyAvgPriceRedisRepository.getAllData("002");

        sseService.broadcast(avgLtePerHourList);
        sseService.broadcast(avg5gPerHourList);
    }

    @PostConstruct
    public void init() {
        log.info("레디스 캐시 초기화 : 최근 {}시간 평균가 레디스 저장", MAX_REDIS_LIST_SIZE);

        List<AvgPerHour> avgLtePerHourList = contractHourlyAvgPriceRepository.
            findLatestByDataCodeBeforeTime("001", LocalDateTime.now(), MAX_REDIS_LIST_SIZE).stream()
            .map(AvgPerHour::of)
            .sorted((a, b) -> a.getAggregatedAt().compareTo(b.getAggregatedAt())) // ← 시간 순 정렬 추가
            .toList();

        List<AvgPerHour> avg5gPerHourList = contractHourlyAvgPriceRepository.
                findLatestByDataCodeBeforeTime("002", LocalDateTime.now(), MAX_REDIS_LIST_SIZE).stream()
                .map(AvgPerHour::of)
                .sorted((a, b) -> a.getAggregatedAt().compareTo(b.getAggregatedAt())) // ← 시간 순 정렬 추가
                .toList();

        contractHourlyAvgPriceRedisRepository.initializeData("001", avgLtePerHourList);
        contractHourlyAvgPriceRedisRepository.initializeData("002", avg5gPerHourList);

    }
}
