package com.ureka.team3.utong_backend.price.dto;

import com.ureka.team3.utong_backend.price.entity.Price;
import lombok.*;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PriceDto {

    private String id;

    private Long minimumPrice;

    private Float minimumRate;

    private Float tax;

    public static PriceDto from(Price price) {
        PriceDto priceDto = new PriceDto();

        priceDto.id = price.getId();
        priceDto.minimumPrice = price.getMinimumPrice();
        priceDto.minimumRate = price.getMinimumRate();
        priceDto.tax = price.getTax();

        return priceDto;
    }

}
