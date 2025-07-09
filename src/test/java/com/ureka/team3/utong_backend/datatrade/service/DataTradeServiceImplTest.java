package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderRedisDto;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRedisRepository;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DataTradeServiceImplTest {

    @InjectMocks
    private DataTradeServiceImpl dataTradeService;

    @Mock
    private SaleDataRequestRepository saleDataRequestRepository;

    @Mock
    private BuyDataRequestRepository buyDataRequestRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OrderRedisRepository orderRedisRepository;

    @Test
    void requestSale_정상동작() {
        // given
        String username = "test-user";
        DataTradeDto.SaleDataRequestDto dto = DataTradeDto.SaleDataRequestDto.builder()
                .price(1000L)
                .dataAmount(5L)
                .dataCode("001")
                .build();

        Account account = Account.builder().id(username).build();

        SaleDataRequest saved = SaleDataRequest.builder()
//                .id("1L")
                .price(dto.getPrice())
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .account(account)
                .build();
        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(saved, "expiredAt", LocalDateTime.now().plusDays(1));

        when(accountRepository.findById(username)).thenReturn(Optional.of(account));
        when(saleDataRequestRepository.save(any())).thenReturn(saved);

        // when
        ApiResponse response = dataTradeService.requestSale(username, dto);

        // then
        assertNotNull(response);
        assertEquals("판매 등록 완료", response.getMessage());
        verify(orderRedisRepository).saveSellOrder(any(OrderRedisDto.class));
    }
}
