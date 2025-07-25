package com.ureka.team3.utong_backend.datatrade.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.auth.repository.AccountRepository;
import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.datatrade.dto.OrderDto;
import com.ureka.team3.utong_backend.datatrade.entity.BuyDataRequest;
import com.ureka.team3.utong_backend.datatrade.entity.SaleDataRequest;
import com.ureka.team3.utong_backend.datatrade.facade.DataTradeFacadeImpl;
import com.ureka.team3.utong_backend.datatrade.repository.BuyDataRequestRepository;
import com.ureka.team3.utong_backend.datatrade.repository.OrderRepositoryImpl;
import com.ureka.team3.utong_backend.datatrade.repository.SaleDataRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DataTradeFacadeImplTest {

    @InjectMocks
    private DataTradeFacadeImpl dataTradeService;

    @Mock
    private SaleDataRequestRepository saleDataRequestRepository;

    @Mock
    private BuyDataRequestRepository buyDataRequestRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private OrderRepositoryImpl orderRepositoryImpl;

    @Test
    void requestBuy_정상동작() {
        // given
        String username = "test-user";
        DataTradeDto.DataTradeRequestDto dto = DataTradeDto.DataTradeRequestDto.builder()
                .price(2000L)
                .dataAmount(3L)
                .dataCode("XYZ")
                .build();

        Account account = Account.builder().id(username).build();

        BuyDataRequest saved = BuyDataRequest.builder()
                .price(dto.getPrice())
                .quantity(dto.getDataAmount())
                .dataCode(dto.getDataCode())
                .account(account)
                .build();

        // createdAt, expiredAt 수동 세팅
        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(saved, "expiredAt", LocalDateTime.now().plusHours(2));
        ReflectionTestUtils.setField(saved, "id", "123"); // ID도 세팅

        when(buyDataRequestRepository.save(any())).thenReturn(saved);

        // when
        ApiResponse response = dataTradeService.requestBuy(account, dto);

        // then
        assertNotNull(response);
        assertEquals("구매 등록 완료", response.getMessage());
        assertEquals("123", response.getData());
        verify(orderRepositoryImpl).savePurchaseOrder(any(OrderDto.class));
    }

    @Test
    void requestSale_정상동작() {
        // given
        String username = "test-user";
        DataTradeDto.DataTradeRequestDto dto = DataTradeDto.DataTradeRequestDto.builder()
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

        when(saleDataRequestRepository.save(any())).thenReturn(saved);

        // when
        ApiResponse response = dataTradeService.requestSale(account, dto);

        // then
        assertNotNull(response);
        assertEquals("판매 등록 완료", response.getMessage());
        verify(orderRepositoryImpl).saveSellOrder(any(OrderDto.class));
    }
}
