package com.ureka.team3.utong_backend.datatrade.utils;

import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.price.entity.Price;
import com.ureka.team3.utong_backend.price.repository.PriceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TradeCalculator {
    private final PriceRepository priceRepository;
    private Float tax;
    private Long minimumPrice;
    private Float minimumRate;

    public Long calculateTotalCoastForConsumer(DataTradeDto.BuyDataRequestDto dto) {
        return dto.getPrice() * dto.getDataAmount();
    }

    public Long calculateTotalIncomeForSeller(Long pricePerUnit, Long amount) {
        long gross = pricePerUnit * amount;
        float taxAmount = gross * tax / 100;
        return (long) (gross - taxAmount);
    }


    public Long calculateCanSellAmount(Long planData, Long sell) {
        return planData - sell;
    }

    @PostConstruct
    private void init() {
        Price price = priceRepository.findAll().get(0);
        this.tax = price.getTax();
        this.minimumPrice = price.getMinimumPrice();
        this.minimumRate = price.getMinimumRate();
    }

}
