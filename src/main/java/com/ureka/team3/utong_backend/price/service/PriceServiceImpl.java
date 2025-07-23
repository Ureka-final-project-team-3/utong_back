package com.ureka.team3.utong_backend.price.service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.common.exception.business.PriceNotFoundException;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRepository;
import com.ureka.team3.utong_backend.price.dto.PriceDto;
import com.ureka.team3.utong_backend.price.dto.WeeklyPriceDto;
import com.ureka.team3.utong_backend.price.entity.Price;
import com.ureka.team3.utong_backend.price.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;
    private final ContractHourlyAvgPriceRepository contractHourlyAvgPriceRepository;

    @Override
    public ApiResponse<PriceDto> getPrice(String id) {
        try {
            Price price = priceRepository.findById(id)
                    .orElseThrow(PriceNotFoundException::new);

            return ApiResponse.success(PriceDto.from(price));
        } catch (Exception e) {
            log.info("조회 중 오류가 발생하였습니다. {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ApiResponse<List<WeeklyPriceDto>> getWeeklyPrices(String dataCode) {
        try {
            List<Object[]> weeklyData = contractHourlyAvgPriceRepository.findWeeklyAvgPricesByDataCode(dataCode);
            
            List<WeeklyPriceDto> weeklyPrices = weeklyData.stream()
                    .map(row -> WeeklyPriceDto.builder()
                            .date(((Date) row[0]).toLocalDate())
                            .avgPrice(((BigDecimal) row[1]).longValue())
                            .dataCode((String) row[2])
                            .build())
                    .collect(Collectors.toList());

            return ApiResponse.success(weeklyPrices);
        } catch (Exception e) {
            log.info("주간 시세 조회 중 오류가 발생하였습니다. {}", e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
