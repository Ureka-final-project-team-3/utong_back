package com.ureka.team3.utong_backend.price.service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.price.dto.PriceDto;

public interface PriceService {

    ApiResponse<PriceDto> getPrice(String id);

}
