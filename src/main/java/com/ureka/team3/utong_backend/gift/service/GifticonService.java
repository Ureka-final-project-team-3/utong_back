package com.ureka.team3.utong_backend.gift.service;

import com.ureka.team3.utong_backend.common.dto.ApiResponse;
import com.ureka.team3.utong_backend.gift.dto.GifticonResponseDto;

import java.util.List;

public interface GifticonService {

    ApiResponse<List<GifticonResponseDto>> getGifticonList();

    ApiResponse<GifticonResponseDto> getGifticonDetail(String gifticonId);

    ApiResponse<Long> getGifticonCount();

    ApiResponse<Void> exchangeGifticon(String gifticonId, String accountId);

}
