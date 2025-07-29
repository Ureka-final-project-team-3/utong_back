package com.ureka.team3.utong_backend.datatrade.utils;

import com.ureka.team3.utong_backend.datatrade.dto.DataTradeDto;
import com.ureka.team3.utong_backend.price.entity.Price;
import com.ureka.team3.utong_backend.price.repository.PriceRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TradeCalculator {

    private final PriceRepository priceRepository;

    private double tax;               // Float → double
    private Long minimumPrice;
    private double minimumRate;       // Float → double

    /**
     * 소비자 기준 총 비용 (단가 * 수량)
     */
    public Long calculateTotalCoastForConsumer(DataTradeDto.DataTradeRequestDto dto) {
        return dto.getPrice() * dto.getDataAmount();
    }

    /**
     * 수수료 차감 금액 계산
     */
    public Long calculateSubtractFee(Long charged) {
        long fee = Math.round(charged * tax);
        return fee;
    }

    /**
     * 판매자가 얻는 실제 수익 계산
     */
    public Long calculateTotalIncomeForSeller(Long pricePerUnit, Long amount) {
        long gross = pricePerUnit * amount;
        double taxAmount = gross * tax;
        return Math.round(gross - taxAmount);
    }

    /**
     * 판매 가능한 데이터 양 계산
     */
    public Long calculateCanSellAmount(Long remaining, Long canSale, Long alreadySold) {

        return Math.min(remaining, canSale - alreadySold);
    }

    public Long calculatePayPoint(Long remaining, Long price) {
        return remaining * price;
    }

    public boolean isHundredUnit(Long price){
        return price%100==0;
    }

    /**
     * 수수료, 최소금액, 최소비율 초기값 설정
     */
    @PostConstruct
    private void init() {
        List<Price> prices = priceRepository.findAll();
        if (prices.isEmpty()) {
            throw new IllegalStateException("가격 정보가 DB에 없습니다.");
        }

        Price price = prices.get(0);
        this.tax = price.getTax();                   // float → double 자동 변환
        this.minimumPrice = price.getMinimumPrice();
        this.minimumRate = price.getMinimumRate();   // float → double 자동 변환
    }


}
