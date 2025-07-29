package com.ureka.team3.utong_backend.datatrade.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ureka.team3.utong_backend.datatrade.dto.ContractDto;
import com.ureka.team3.utong_backend.datatrade.utils.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ContractQueueRepositoryImpl implements ContractQueueRepository{

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<ContractDto> getAllCachedContracts(String dataCode) {
        try {
            String key = RedisKeyUtil.buildContractListKey(dataCode);

            List<String> contractJsonList = stringRedisTemplate.opsForList().range(key, 0, -1);

            if (contractJsonList == null || contractJsonList.isEmpty()) {
                log.info("캐시된 계약 데이터가 없습니다. dataCode: {}", dataCode);
                return new ArrayList<>();
            }

            List<ContractDto> contracts = new ArrayList<>();

            for (String json : contractJsonList) {
                try {
                    ContractDto contract = objectMapper.readValue(json, ContractDto.class);
                    contracts.add(contract);
                } catch (JsonProcessingException e) {
                    log.error("계약 데이터 파싱 실패 - dataCode: {}, json: {}, error: {}", dataCode, json, e.getMessage());
                }
            }

            log.info("캐시된 계약 데이터 조회 완료 - dataCode: {}, size: {}", dataCode, contracts.size());
            return contracts;
        } catch (Exception e) {
            log.error("캐시된 계약 데이터 조회 실패 - dataCode: {}, error: {}", dataCode, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public ContractDto getRecentContract(String dataCode) {
        try {
            String key = RedisKeyUtil.buildContractListKey(dataCode);

            List<String> contractJsonList = stringRedisTemplate.opsForList().range(key, 0, 0);

            if (contractJsonList == null || contractJsonList.isEmpty()) {
                log.info("캐시된 계약 데이터가 없습니다. dataCode: {}", dataCode);
                return null;
            }

            String json = contractJsonList.get(0);
            try {
                ContractDto contract = objectMapper.readValue(json, ContractDto.class);
                log.info("최근 계약 데이터 조회 완료 - dataCode: {}", dataCode);
                return contract;
            } catch (JsonProcessingException e) {
                log.error("계약 데이터 파싱 실패 - dataCode: {}, json: {}, error: {}", dataCode, json, e.getMessage());
                return null;
            }
        } catch (Exception e) {
            log.error("캐시된 계약 데이터 조회 실패 - dataCode: {}, error: {}", dataCode, e.getMessage(), e);
            return null;
        }
    }

}
