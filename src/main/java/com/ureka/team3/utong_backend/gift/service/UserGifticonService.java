package com.ureka.team3.utong_backend.gift.service;

import com.ureka.team3.utong_backend.gift.dto.UserGifticonDetailResponseDto;
import com.ureka.team3.utong_backend.gift.dto.UserGifticonResponseDto;

import java.util.List;

public interface UserGifticonService {

    // 기프티콘 목록
    List<UserGifticonResponseDto> getMyGifticons(String userId);

    // 기프티콘 상세
    UserGifticonDetailResponseDto getGifticonDetail(String Id, String userId);


}