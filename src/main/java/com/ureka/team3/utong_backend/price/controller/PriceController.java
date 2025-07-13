package com.ureka.team3.utong_backend.price.controller;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.price.dto.PriceDto;
import com.ureka.team3.utong_backend.price.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class PriceController {

    private final PriceService priceService;

    @GetMapping("/prices")
    public ResponseEntity<ApiResponse<PriceDto>> getPrice(
            @RequestParam(defaultValue = "903ee67c-71b3-432e-bbd1-aaf5d5043376") String id
    ) {

        return ResponseEntity.ok(priceService.getPrice(id));
    }
}
