package com.ureka.team3.utong_backend.datatrade.dto.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistoryResponseDto {
    private List<PurchaseResponseDto> purchases;
}
