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

        Long initialPreviousPrice = getPreviousPrice(today.minusDays(7).toLocalDate(), dataCode, results);
        Long currentPreviousPrice = initialPreviousPrice;

        for (int i = 7; i >= 1; i--) {
            LocalDate targetDate = today.minusDays(i).toLocalDate();
            Optional<Object[]> dayData = findDataForDate(targetDate, results);

            if (dayData.isPresent()) {
                Object[] result = dayData.get();
                Long actualPrice = ((Number) result[1]).longValue();
                dailyCharts.add(createDailyChart(targetDate, actualPrice));
                currentPreviousPrice = actualPrice;
            } else {
                buildChartByDataCode(dataCode, dailyCharts, targetDate, currentPreviousPrice);
            }
        }

        return dailyCharts;
    }

    private void buildChartByDataCode(String dataCode, List<DailyChartDto> dailyCharts, LocalDate targetDate, Long previousPrice) {
        switch (dataCode) {
            case "001" : dailyCharts.add(createDailyChart(targetDate, previousPrice == 0L ? 4000L : previousPrice)); break;
            case "002" : dailyCharts.add(createDailyChart(targetDate, previousPrice == 0L ? 5000L : previousPrice)); break;
        }
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

        return recentPrice.orElseGet(() -> contractRepository.findLatestAvgPriceBeforeDate(targetDate.atStartOfDay(), dataCode));

    }
}