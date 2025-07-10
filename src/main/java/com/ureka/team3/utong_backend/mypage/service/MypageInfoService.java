package com.ureka.team3.utong_backend.mypage.service;

import com.ureka.team3.utong_backend.mypage.dto.MyInfoResponseDto;

public interface MypageInfoService {
    MyInfoResponseDto getMyInfo(String accountId);
}
