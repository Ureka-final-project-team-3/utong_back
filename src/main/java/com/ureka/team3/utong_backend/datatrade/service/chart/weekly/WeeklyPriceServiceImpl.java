package com.ureka.team3.utong_backend.datatrade.service.chart.weekly;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.datatrade.dto.DailyChartDto;
import com.ureka.team3.utong_backend.datatrade.dto.WeeklyChartDto;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeeklyPriceServiceImpl implements WeeklyPriceService {

    private final ContractRepository contractRepository;

    @Override
    public ApiResponse<WeeklyChartDto> getWeeklyPrice(String dataCode) {
        try {
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime eightDaysAgo = today.minusDays(8);

            List<Object[]> results = contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode);
            List<DailyChartDto> dailyCharts = buildDailyCharts(today, results, dataCode);

            WeeklyChartDto weeklyChart = WeeklyChartDto.builder()
                    .dataCode(dataCode)
                    .dailyChartDtoList(dailyCharts)
                    .build();

            return ApiResponse.success(weeklyChart);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private List<DailyChartDto> buildDailyCharts(LocalDateTime today, List<Object[]> results, String dataCode) {
        List<DailyChartDto> dailyCharts = new ArrayList<>();

        for (int i = 7; i >= 1; i--) {
            LocalDate targetDate = today.minusDays(i).toLocalDate();
            Optional<Object[]> dayData = findDataForDate(targetDate, results);

            if (dayData.isPresent()) {
                Object[] result = dayData.get();
                dailyCharts.add(createDailyChart(targetDate, ((Number) result[1]).longValue()));
            } else {
                Long previousPrice = getPreviousPrice(targetDate, dataCode, results);
                dailyCharts.add(createDailyChart(targetDate, previousPrice != null ? previousPrice : 0L));
            }
        }

        return dailyCharts;
    }

    private Optional<Object[]> findDataForDate(LocalDate targetDate, List<Object[]> results) {
        return results.stream()
                .filter(result -> ((Date) result[0]).toLocalDate().equals(targetDate))
                .findFirst();
    }

    private DailyChartDto createDailyChart(LocalDate date, Long price) {
        return DailyChartDto.builder()
                .date(date)
                .avgPrice(price)
                .build();
    }

    private Long getPreviousPrice(LocalDate targetDate, String dataCode, List<Object[]> existingResults) {
        Optional<Long> recentPrice = existingResults.stream()
                .filter(result -> ((Date) result[0]).toLocalDate().isBefore(targetDate))
                .map(result -> ((Number) result[1]).longValue())
                .findFirst();

        if (recentPrice.isPresent()) {
            return recentPrice.get();
        }

        return contractRepository.findLatestAvgPriceBeforeDate(targetDate.atStartOfDay(), dataCode);
    }
}