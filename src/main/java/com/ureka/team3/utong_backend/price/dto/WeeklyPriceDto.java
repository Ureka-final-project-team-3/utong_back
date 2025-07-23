package com.ureka.team3.utong_backend.price.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyPriceDto {

    private LocalDate date;
    
    private Long avgPrice;
    
    private String dataCode;

}