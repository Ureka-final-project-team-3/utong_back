package com.ureka.team3.utong_backend.price.controller;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.price.dto.PriceDto;
import com.ureka.team3.utong_backend.price.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "가격 조회 API", description = "데이터 코드 ID를 기준으로 현재 가격 정보를 조회합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PriceController {

    private final PriceService priceService;

    @Operation(
            summary = "데이터 가격 조회",
            description = "지정된 데이터 코드 ID에 대한 현재 가격 정보를 조회합니다. ID를 생략할 경우 기본 ID가 사용됩니다."
    )
    @GetMapping("/prices")
    public ResponseEntity<ApiResponse<PriceDto>> getPrice(
            @RequestParam(defaultValue = "903ee67c-71b3-432e-bbd1-aaf5d5043376") String id
    ) {

        return ResponseEntity.ok(priceService.getPrice(id));
    }
}
