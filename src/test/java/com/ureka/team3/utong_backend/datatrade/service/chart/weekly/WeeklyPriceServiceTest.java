package com.ureka.team3.utong_backend.datatrade.service.chart.weekly;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.datatrade.dto.chart.DailyChartDto;
import com.ureka.team3.utong_backend.datatrade.dto.chart.WeeklyChartDto;
import com.ureka.team3.utong_backend.datatrade.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class WeeklyPriceServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private WeeklyPriceServiceImpl weeklyPriceService;

    private String dataCode;
    private LocalDateTime today;
    private LocalDateTime eightDaysAgo;
    private List<Object[]> mockResults;

    @BeforeEach
    void setUp() {
        dataCode = "001";
        today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        eightDaysAgo = today.minusDays(8);

        mockResults = new ArrayList<>();
    }

    @Nested
    @DisplayName("주간 가격 조회 성공 테스트")
    class GetWeeklyPriceSuccess {

        @Test
        @DisplayName("성공 - 모든 날짜에 데이터가 있는 경우")
        void getWeeklyPrice_성공_모든날짜데이터있음_test() {
            // given
            setupMockResultsWithAllDays();
            given(contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode))
                    .willReturn(mockResults);

            // when
            ApiResponse<WeeklyChartDto> response = weeklyPriceService.getWeeklyPrice(dataCode);

            // then
            assertThat(response.getData().getDataCode()).isEqualTo(dataCode);
            assertThat(response.getData().getDailyChartDtoList()).hasSize(7);

            // 각 날짜별 데이터 검증 (첫 번째는 7일 전, 마지막은 1일 전)
            List<DailyChartDto> dailyCharts = response.getData().getDailyChartDtoList();
            for (int i = 0; i < 7; i++) {
                DailyChartDto chart = dailyCharts.get(i);
                assertThat(chart.getDate()).isEqualTo(today.minusDays(7-i).toLocalDate());
                assertThat(chart.getAvgPrice()).isEqualTo(1000L + ((7-i-1) * 100L)); // 실제 mock 데이터와 일치
            }

            then(contractRepository).should()
                    .findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode);
        }

        @Test
        @DisplayName("성공 - 일부 날짜에만 데이터가 있는 경우 (dataCode: 001)")
        void getWeeklyPrice_성공_일부날짜데이터있음_001_test() {
            // given
            setupMockResultsWithPartialDays();
            given(contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode))
                    .willReturn(mockResults);
            given(contractRepository.findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode)))
                    .willReturn(0L); // 과거 데이터 없음

            // when
            ApiResponse<WeeklyChartDto> response = weeklyPriceService.getWeeklyPrice(dataCode);

            // then
            assertThat(response.getData().getDataCode()).isEqualTo(dataCode);
            assertThat(response.getData().getDailyChartDtoList()).hasSize(7);

            // 실제 로직에 따라 검증 - initialPreviousPrice가 0L이므로 데이터가 없는 날은 4000L
            List<DailyChartDto> dailyCharts = response.getData().getDailyChartDtoList();

            // 7일 전 데이터 없음 -> 4000L (001 기본값)
            DailyChartDto day7Chart = dailyCharts.get(0);
            assertThat(day7Chart.getAvgPrice()).isEqualTo(4000L);

            // 6일 전 데이터 없음 -> 4000L (currentPreviousPrice 유지)
            DailyChartDto day6Chart = dailyCharts.get(1);
            assertThat(day6Chart.getAvgPrice()).isEqualTo(4000L);

            // 5일 전 데이터 있음 -> 2500L
            DailyChartDto day5Chart = dailyCharts.get(2);
            assertThat(day5Chart.getAvgPrice()).isEqualTo(2500L);

            then(contractRepository).should()
                    .findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode);
            then(contractRepository).should()
                    .findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode));
        }

        @Test
        @DisplayName("성공 - 일부 날짜에만 데이터가 있는 경우 (dataCode: 002)")
        void getWeeklyPrice_성공_일부날짜데이터있음_002_test() {
            // given
            dataCode = "002";
            setupMockResultsWithPartialDays();
            given(contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode))
                    .willReturn(mockResults);
            given(contractRepository.findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode)))
                    .willReturn(0L); // 과거 데이터 없음

            // when
            ApiResponse<WeeklyChartDto> response = weeklyPriceService.getWeeklyPrice(dataCode);

            // then
            assertThat(response.getData().getDataCode()).isEqualTo(dataCode);

            // 데이터가 없는 날짜는 5000L(002 기본값)로 설정되어야 함
            List<DailyChartDto> dailyCharts = response.getData().getDailyChartDtoList();
            DailyChartDto firstChart = dailyCharts.get(0); // 7일 전, 데이터 없음
            assertThat(firstChart.getAvgPrice()).isEqualTo(5000L);

            then(contractRepository).should()
                    .findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode);
        }

        @Test
        @DisplayName("성공 - 과거 가격이 존재하는 경우")
        void getWeeklyPrice_성공_과거가격존재_test() {
            // given
            setupEmptyMockResults(); // 현재 기간에는 데이터 없음
            given(contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode))
                    .willReturn(mockResults);
            given(contractRepository.findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode)))
                    .willReturn(3500L); // 과거 데이터 존재

            // when
            ApiResponse<WeeklyChartDto> response = weeklyPriceService.getWeeklyPrice(dataCode);

            // 과거 가격이 0이 아니므로 그 값을 사용해야 함 (3500L)
            List<DailyChartDto> dailyCharts = response.getData().getDailyChartDtoList();

            // 모든 날짜가 데이터 없으므로 buildChartByDataCode 호출
            // previousPrice = 3500L (0이 아님) -> 그대로 3500L 사용
            for (DailyChartDto chart : dailyCharts) {
                assertThat(chart.getAvgPrice()).isEqualTo(3500L);
            }

            then(contractRepository).should()
                    .findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode));
        }

        @Test
        @DisplayName("성공 - 빈 결과 리스트")
        void getWeeklyPrice_성공_빈결과리스트_test() {
            // given
            setupEmptyMockResults();
            given(contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode))
                    .willReturn(mockResults);
            given(contractRepository.findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode)))
                    .willReturn(0L);

            // when
            ApiResponse<WeeklyChartDto> response = weeklyPriceService.getWeeklyPrice(dataCode);

            // then
            assertThat(response.getData().getDailyChartDtoList()).hasSize(7);

            // initialPreviousPrice = 0L이므로 모든 날짜에 4000L (001 기본값) 적용
            List<DailyChartDto> dailyCharts = response.getData().getDailyChartDtoList();
            for (DailyChartDto chart : dailyCharts) {
                assertThat(chart.getAvgPrice()).isEqualTo(4000L);
            }

            then(contractRepository).should()
                    .findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode);
        }

        @Test
        @DisplayName("성공 - 데이터 없는 날 이후 실제 가격 업데이트")
        void getWeeklyPrice_성공_가격업데이트_test() {
            // given
            setupMockResultsWithLaterDays(); // 뒤쪽 날짜에만 데이터
            given(contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode))
                    .willReturn(mockResults);
            given(contractRepository.findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode)))
                    .willReturn(0L);

            // when
            ApiResponse<WeeklyChartDto> response = weeklyPriceService.getWeeklyPrice(dataCode);

            // then
            List<DailyChartDto> dailyCharts = response.getData().getDailyChartDtoList();

            // 처음 3일은 데이터 없음 -> 4000L
            assertThat(dailyCharts.get(0).getAvgPrice()).isEqualTo(4000L); // 7일 전
            assertThat(dailyCharts.get(1).getAvgPrice()).isEqualTo(4000L); // 6일 전
            assertThat(dailyCharts.get(2).getAvgPrice()).isEqualTo(4000L); // 5일 전

            // 4일 전부터 실제 데이터 -> currentPreviousPrice 업데이트됨
            assertThat(dailyCharts.get(3).getAvgPrice()).isEqualTo(1100L); // 4일 전 실제 데이터
            assertThat(dailyCharts.get(4).getAvgPrice()).isEqualTo(1200L); // 3일 전 실제 데이터
            assertThat(dailyCharts.get(5).getAvgPrice()).isEqualTo(1300L); // 2일 전 실제 데이터
            assertThat(dailyCharts.get(6).getAvgPrice()).isEqualTo(1400L); // 1일 전 실제 데이터
        }
    }

    @Nested
    @DisplayName("주간 가격 조회 실패 테스트")
    class GetWeeklyPriceFailure {

        @Test
        @DisplayName("실패 - Repository에서 예외 발생")
        void getWeeklyPrice_실패_Repository예외_test() {
            // given
            given(contractRepository.findDailyAvgPricesForLastDays(any(LocalDateTime.class), any(LocalDateTime.class), anyString()))
                    .willThrow(new RuntimeException("Database error"));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                weeklyPriceService.getWeeklyPrice(dataCode);
            });

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);

            then(contractRepository).should()
                    .findDailyAvgPricesForLastDays(any(LocalDateTime.class), any(LocalDateTime.class), eq(dataCode));
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCases {

        @Test
        @DisplayName("알 수 없는 dataCode로 요청")
        void getWeeklyPrice_알수없는DataCode_test() {
            // given
            String unknownDataCode = "999";
            setupEmptyMockResults();
            given(contractRepository.findDailyAvgPricesForLastDays(any(LocalDateTime.class), any(LocalDateTime.class), eq(unknownDataCode)))
                    .willReturn(mockResults);
            given(contractRepository.findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(unknownDataCode)))
                    .willReturn(0L);

            // when
            ApiResponse<WeeklyChartDto> response = weeklyPriceService.getWeeklyPrice(unknownDataCode);

            // then
            assertThat(response.getData().getDataCode()).isEqualTo(unknownDataCode);

            // 알 수 없는 dataCode의 경우 buildChartByDataCode에서 switch문에 해당 없음
            // 따라서 add가 호출되지 않아 빈 리스트가 될 수 있음
            List<DailyChartDto> dailyCharts = response.getData().getDailyChartDtoList();
            assertThat(dailyCharts).isEmpty();
        }

        @Test
        @DisplayName("null 반환하는 findLatestAvgPriceBeforeDate")
        void getWeeklyPrice_null반환_test() {
            // given
            setupEmptyMockResults();
            given(contractRepository.findDailyAvgPricesForLastDays(eightDaysAgo, today, dataCode))
                    .willReturn(mockResults);
            given(contractRepository.findLatestAvgPriceBeforeDate(any(LocalDateTime.class), eq(dataCode)))
                    .willReturn(null); // null 반환

            // when & then
            BusinessException exception = assertThrows(BusinessException.class, () -> {
                weeklyPriceService.getWeeklyPrice(dataCode);
            });

            // null로 인한 NullPointerException이 BusinessException으로 래핑됨
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // Helper methods for setting up mock data
    private void setupMockResultsWithAllDays() {
        mockResults.clear();
        for (int i = 1; i <= 7; i++) {
            LocalDate date = today.minusDays(i).toLocalDate();
            Long price = 1000L + ((i-1) * 100L); // 7일 전: 1000L, 6일 전: 1100L, ...
            mockResults.add(new Object[]{Date.valueOf(date), price});
        }
    }

    private void setupMockResultsWithPartialDays() {
        mockResults.clear();
        // 7일 중 3일만 데이터 있음 (5, 3, 1일 전)
        int[] daysWithData = {5, 3, 1};
        for (int day : daysWithData) {
            LocalDate date = today.minusDays(day).toLocalDate();
            Long price = 2000L + (day * 100L); // 5일 전: 2500L, 3일 전: 2300L, 1일 전: 2100L
            mockResults.add(new Object[]{Date.valueOf(date), price});
        }
    }

    private void setupMockResultsWithLaterDays() {
        mockResults.clear();
        // 뒤쪽 4일만 데이터 있음 (4, 3, 2, 1일 전)
        for (int i = 4; i >= 1; i--) {
            LocalDate date = today.minusDays(i).toLocalDate();
            Long price = 1000L + ((5-i) * 100L); // 4일 전: 1100L, 3일 전: 1200L, 2일 전: 1300L, 1일 전: 1400L
            mockResults.add(new Object[]{Date.valueOf(date), price});
        }
    }

    private void setupEmptyMockResults() {
        mockResults.clear();
    }
}