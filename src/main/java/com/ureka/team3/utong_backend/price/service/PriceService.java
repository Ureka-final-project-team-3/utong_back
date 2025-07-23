package com.ureka.team3.utong_backend.price.service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.price.dto.PriceDto;
import com.ureka.team3.utong_backend.price.dto.WeeklyPriceDto;

import java.util.List;

public interface PriceService {

    ApiResponse<PriceDto> getPrice(String id);

    ApiResponse<List<WeeklyPriceDto>> getWeeklyPrices(String dataCode);

}
