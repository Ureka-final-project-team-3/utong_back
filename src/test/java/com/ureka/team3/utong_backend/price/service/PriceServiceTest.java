package com.ureka.team3.utong_backend.price.service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.common.exception.BusinessException;
import com.ureka.team3.utong_backend.common.exception.ErrorCode;
import com.ureka.team3.utong_backend.datatrade.repository.ContractHourlyAvgPriceRepository;
import com.ureka.team3.utong_backend.price.dto.PriceDto;
import com.ureka.team3.utong_backend.price.dto.WeeklyPriceDto;
import com.ureka.team3.utong_backend.price.entity.Price;
import com.ureka.team3.utong_backend.price.repository.PriceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @InjectMocks
    private PriceServiceImpl priceService;

    @Mock
    private PriceRepository priceRepository;

    @Mock
    private ContractHourlyAvgPriceRepository contractHourlyAvgPriceRepository;

    @Test
    void getPrice_성공_test() {
        // given
        String id = UUID.randomUUID().toString();
        PriceDto priceDto = PriceDto.builder()
                .id(id)
                .minimumPrice(5000L)
                .minimumRate(30.0F)
                .tax(2.5F)
                .build();

        Price price = Price.of(priceDto);

        when(priceRepository.findById(id)).thenReturn(Optional.of(price));

        // when
        ApiResponse<PriceDto> response = priceService.getPrice(id);

        // then
        assertThat(response.getResultCode()).isEqualTo(200);
        assertThat(response.getData().getMinimumPrice()).isEqualTo(5000L);
    }

    @Test
    void getPrice_실패_test() {
        // given
        String id = UUID.randomUUID().toString();

        when(priceRepository.findById(id)).thenReturn(Optional.empty());

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            priceService.getPrice(id);
        });

        // then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR);
        verify(priceRepository, times(1)).findById(id);
    }

    @Test
    void getWeeklyPrices_성공_test() {
        // given
        String dataCode = "TEST";
        Object[] mockData1 = {Date.valueOf("2024-01-01"), new BigDecimal("1000"), "TEST"};
        Object[] mockData2 = {Date.valueOf("2024-01-02"), new BigDecimal("1100"), "TEST"};
        
        List<Object[]> mockWeeklyData = Arrays.asList(mockData1, mockData2);
        
        when(contractHourlyAvgPriceRepository.findWeeklyAvgPricesByDataCode(dataCode))
                .thenReturn(mockWeeklyData);

        // when
        ApiResponse<List<WeeklyPriceDto>> response = priceService.getWeeklyPrices(dataCode);

        // then
        assertThat(response.getResultCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(response.getData().get(0).getAvgPrice()).isEqualTo(1000L);
        assertThat(response.getData().get(0).getDataCode()).isEqualTo("TEST");
        verify(contractHourlyAvgPriceRepository, times(1)).findWeeklyAvgPricesByDataCode(dataCode);
    }

    @Test
    void getWeeklyPrices_빈_데이터_test() {
        // given
        String dataCode = "EMPTY";
        List<Object[]> emptyData = Arrays.asList();
        
        when(contractHourlyAvgPriceRepository.findWeeklyAvgPricesByDataCode(dataCode))
                .thenReturn(emptyData);

        // when
        ApiResponse<List<WeeklyPriceDto>> response = priceService.getWeeklyPrices(dataCode);

        // then
        assertThat(response.getResultCode()).isEqualTo(200);
        assertThat(response.getData()).isEmpty();
        verify(contractHourlyAvgPriceRepository, times(1)).findWeeklyAvgPricesByDataCode(dataCode);
    }
}