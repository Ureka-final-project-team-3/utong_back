package com.ureka.team3.utong_backend.gift.service;

import com.ureka.team3.utong_backend.gift.dto.MyGifticonDetailResponseDto;
import com.ureka.team3.utong_backend.gift.dto.MyGifticonResponseDto;

import java.util.List;

public interface MyGifticonService {

    // 기프티콘 목록
    List<MyGifticonResponseDto> getMyGifticons(String userId);

    // 기프티콘 상세
    MyGifticonDetailResponseDto getGifticonDetail(String Id, String userId);


}