package com.ureka.team3.utong_backend.price.controller;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.price.dto.PriceDto;
import com.ureka.team3.utong_backend.price.dto.WeeklyPriceDto;
import com.ureka.team3.utong_backend.price.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Price", description = "시세 관련 API")
public class PriceController {

    private final PriceService priceService;

    @GetMapping("/prices")
    @Operation(summary = "가격 정책 조회", description = "특정 ID의 가격 정책을 조회합니다.")
    public ResponseEntity<ApiResponse<PriceDto>> getPrice(
            @RequestParam(defaultValue = "903ee67c-71b3-432e-bbd1-aaf5d5043376") String id
    ) {

        return ResponseEntity.ok(priceService.getPrice(id));
    }

    @GetMapping("/prices/weekly")
    @Operation(summary = "주간 시세 조회", description = "지정된 데이터 코드의 최근 7일간 일별 평균 시세를 조회합니다. (오늘-8일 ~ 오늘-1일)")
    public ResponseEntity<ApiResponse<List<WeeklyPriceDto>>> getWeeklyPrices(
            @RequestParam String dataCode
    ) {
        return ResponseEntity.ok(priceService.getWeeklyPrices(dataCode));
    }
}
