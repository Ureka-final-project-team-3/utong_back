package com.ureka.team3.utong_backend.user.service;

import com.ureka.team3.utong_backend.auth.entity.Account;
import com.ureka.team3.utong_backend.user.dto.MyInfoResponseDto;

public interface UserService {
    MyInfoResponseDto getMyInfo(Account account);
}
